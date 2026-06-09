package com.example.persona.location.controller;

import com.example.persona.location.dto.request.LocationRequest;
import com.example.persona.location.dto.response.ApiResponse;
import com.example.persona.location.dto.response.LocationResponse;
import com.example.persona.location.model.AuditAction;
import com.example.persona.location.model.AuditEvent;
import com.example.persona.location.service.AuditService;
import com.example.persona.location.service.LocationService;
import com.example.persona.location.service.StorageService;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/locations")
@RequiredArgsConstructor
@Validated
@Tag(name = "Location - Admin", description = "Manage Wing Bank location data (admin only)")
public class LocationAdminController {

    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of("image/jpeg", "image/png", "image/webp");
    private static final long MAX_IMAGE_BYTES = 5 * 1024 * 1024;

    private final LocationService locationService;
    private final StorageService storageService;
    private final AuditService auditService;

    // ── CRUD ─────────────────────────────────────────────────────────────────

    @PostMapping
    @Operation(summary = "Create a new location")
    @Timed(value = "http.admin.location.create")
    public ResponseEntity<ApiResponse<LocationResponse>> create(
            @RequestBody @Valid LocationRequest.CreateLocationRequest request,
            @RequestHeader(value = "X-Actor", defaultValue = "system") String actor) {
        var location = locationService.create(request, actor);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(LocationResponse.from(location), "Location created"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing location")
    @Timed(value = "http.admin.location.update")
    public ResponseEntity<ApiResponse<LocationResponse>> update(
            @PathVariable UUID id,
            @RequestBody @Valid LocationRequest.UpdateLocationRequest request,
            @RequestHeader(value = "X-Actor", defaultValue = "system") String actor) {
        var location = locationService.update(id, request, actor);
        return ResponseEntity.ok(ApiResponse.ok(LocationResponse.from(location), "Location updated"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a location permanently")
    @Timed(value = "http.admin.location.delete")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable UUID id, @RequestHeader(value = "X-Actor", defaultValue = "system") String actor) {
        locationService.delete(id, actor);
        return ResponseEntity.ok(ApiResponse.ok(null, "Location deleted"));
    }

    // ── Status lifecycle ─────────────────────────────────────────────────────

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Activate a location")
    @Timed(value = "http.admin.location.activate")
    public ResponseEntity<ApiResponse<LocationResponse>> activate(
            @PathVariable UUID id, @RequestHeader(value = "X-Actor", defaultValue = "system") String actor) {
        return ResponseEntity.ok(
                ApiResponse.ok(LocationResponse.from(locationService.activate(id, actor)), "Location activated"));
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate a location")
    @Timed(value = "http.admin.location.deactivate")
    public ResponseEntity<ApiResponse<LocationResponse>> deactivate(
            @PathVariable UUID id, @RequestHeader(value = "X-Actor", defaultValue = "system") String actor) {
        return ResponseEntity.ok(
                ApiResponse.ok(LocationResponse.from(locationService.deactivate(id, actor)), "Location deactivated"));
    }

    @PatchMapping("/{id}/close")
    @Operation(
            summary = "Temporarily close a location",
            description =
                    "Location stays ACTIVE but shows as temporarily closed. Provide closedUntil for a scheduled re-open, or omit for indefinite closure.")
    @Timed(value = "http.admin.location.close")
    public ResponseEntity<ApiResponse<LocationResponse>> closeTemporarily(
            @PathVariable UUID id,
            @RequestBody(required = false) LocationRequest.CloseLocationRequest request,
            @RequestHeader(value = "X-Actor", defaultValue = "system") String actor) {
        var location = locationService.closeTemporarily(id, request != null ? request.closedUntil() : null, actor);
        return ResponseEntity.ok(ApiResponse.ok(LocationResponse.from(location), "Location temporarily closed"));
    }

    @PatchMapping("/{id}/reopen")
    @Operation(summary = "Reopen a temporarily closed location")
    @Timed(value = "http.admin.location.reopen")
    public ResponseEntity<ApiResponse<LocationResponse>> reopen(
            @PathVariable UUID id, @RequestHeader(value = "X-Actor", defaultValue = "system") String actor) {
        return ResponseEntity.ok(
                ApiResponse.ok(LocationResponse.from(locationService.reopen(id, actor)), "Location reopened"));
    }

    // ── Image upload ──────────────────────────────────────────────────────────

    @PostMapping(value = "/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Upload a logo or cover image",
            description = "Uploads to S3/MinIO and updates logoUrl or coverUrl. Accepted: jpeg/png/webp, max 5 MB.")
    @Timed(value = "http.admin.location.image.upload")
    public ResponseEntity<ApiResponse<ImageUploadResponse>> uploadImage(
            @PathVariable UUID id,
            @Parameter(description = "Image file (jpeg/png/webp, max 5 MB)") @RequestParam("file") MultipartFile file,
            @Parameter(description = "Which field to update: logo or cover") @RequestParam("type") ImageType type,
            @RequestHeader(value = "X-Actor", defaultValue = "system") String actor) {
        if (!locationService.existsById(id))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Location not found: " + id);

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType))
            throw new ResponseStatusException(
                    HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Unsupported image type. Allowed: " + ALLOWED_IMAGE_TYPES);
        if (file.getSize() > MAX_IMAGE_BYTES)
            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "Image exceeds 5 MB limit");

        String extension = contentType.split("/")[1].replace("jpeg", "jpg");
        String key = "locations/" + id + "/" + type.name().toLowerCase() + "." + extension;

        String url;
        try {
            url = storageService.upload(file.getInputStream(), contentType, key);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Image upload failed");
        }

        // Build a minimal update to patch only the URL field
        var existing = locationService
                .findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Location not found: " + id));

        var updateRequest = new LocationRequest.UpdateLocationRequest(
                null,
                null,
                null,
                null,
                null,
                null,
                type == ImageType.logo ? url : existing.getLogoUrl(),
                type == ImageType.cover ? url : existing.getCoverUrl());
        locationService.update(id, updateRequest, actor);

        auditService.record(AuditEvent.of(
                id, AuditAction.IMAGE_UPLOADED, actor, "{\"type\":\"" + type + "\",\"url\":\"" + url + "\"}"));

        log.info("Image uploaded for location {}: type={}, url={}", id, type, url);
        return ResponseEntity.ok(ApiResponse.ok(new ImageUploadResponse(url), "Image uploaded"));
    }

    // ── Audit trail ──────────────────────────────────────────────────────────

    @GetMapping("/{id}/audit")
    @Operation(summary = "Get audit trail for a location")
    @Timed(value = "http.admin.location.audit")
    public ResponseEntity<ApiResponse<List<AuditEntryResponse>>> getAuditTrail(@PathVariable UUID id) {
        var events = auditService.findByLocationId(id).stream()
                .map(e -> new AuditEntryResponse(e.id(), e.action().name(), e.actor(), e.occurredAt()))
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(events));
    }

    // ── Inner types ───────────────────────────────────────────────────────────

    public enum ImageType {
        logo,
        cover
    }

    public record ImageUploadResponse(String url) {}

    public record AuditEntryResponse(UUID eventId, String action, String actor, java.time.Instant occurredAt) {}
}
