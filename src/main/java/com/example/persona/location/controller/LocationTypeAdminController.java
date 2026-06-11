package com.example.persona.location.controller;

import com.example.persona.location.dto.request.LocationTypeRequest.LocationTypeUpsertRequest;
import com.example.persona.location.dto.request.LocationTypeRequest.TagUpsertRequest;
import com.example.persona.location.dto.response.ApiResponse;
import com.example.persona.location.dto.response.LocationTypeResponse;
import com.example.persona.location.dto.response.TagResponse;
import com.example.persona.location.service.LocationTypeAdminService;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Admin CRUD for location types and tags (FR-03 Website Locator Management). */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "Location - Admin", description = "Manage location types and tags (admin only)")
public class LocationTypeAdminController {

    private final LocationTypeAdminService locationTypeAdminService;

    // ── Location Types ──────────────────────────────────────────────────────────

    @PostMapping("/categories")
    @Operation(summary = "Create a location type")
    @Timed(value = "http.admin.category.create")
    public ResponseEntity<ApiResponse<LocationTypeResponse>> createLocationType(
            @RequestBody @Valid LocationTypeUpsertRequest request,
            @RequestHeader(value = "X-Actor", defaultValue = "system") String actor) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(locationTypeAdminService.createLocationType(request, actor), "Location type created"));
    }

    @PutMapping("/categories/{code}")
    @Operation(summary = "Update a location type")
    @Timed(value = "http.admin.category.update")
    public ResponseEntity<ApiResponse<LocationTypeResponse>> updateLocationType(
            @PathVariable String code,
            @RequestBody @Valid LocationTypeUpsertRequest request,
            @RequestHeader(value = "X-Actor", defaultValue = "system") String actor) {
        return ResponseEntity.ok(
                ApiResponse.ok(
                        locationTypeAdminService.updateLocationType(code, request, actor), "Location type updated"));
    }

    @DeleteMapping("/categories/{code}")
    @Operation(summary = "Delete a location type")
    @Timed(value = "http.admin.category.delete")
    public ResponseEntity<ApiResponse<Void>> deleteLocationType(
            @PathVariable String code,
            @RequestHeader(value = "X-Actor", defaultValue = "system") String actor) {
        locationTypeAdminService.deleteLocationType(code, actor);
        return ResponseEntity.ok(ApiResponse.ok(null, "Location type deleted"));
    }

    // ── Tags ──────────────────────────────────────────────────────────────────────

    @PostMapping("/tags")
    @Operation(summary = "Create a tag")
    @Timed(value = "http.admin.tag.create")
    public ResponseEntity<ApiResponse<TagResponse>> createTag(
            @RequestBody @Valid TagUpsertRequest request,
            @RequestHeader(value = "X-Actor", defaultValue = "system") String actor) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(locationTypeAdminService.createTag(request, actor), "Tag created"));
    }

    @PutMapping("/tags/{code}")
    @Operation(summary = "Update a tag")
    @Timed(value = "http.admin.tag.update")
    public ResponseEntity<ApiResponse<TagResponse>> updateTag(
            @PathVariable String code,
            @RequestBody @Valid TagUpsertRequest request,
            @RequestHeader(value = "X-Actor", defaultValue = "system") String actor) {
        return ResponseEntity.ok(
                ApiResponse.ok(locationTypeAdminService.updateTag(code, request, actor), "Tag updated"));
    }

    @DeleteMapping("/tags/{code}")
    @Operation(summary = "Delete a tag")
    @Timed(value = "http.admin.tag.delete")
    public ResponseEntity<ApiResponse<Void>> deleteTag(
            @PathVariable String code,
            @RequestHeader(value = "X-Actor", defaultValue = "system") String actor) {
        locationTypeAdminService.deleteTag(code, actor);
        return ResponseEntity.ok(ApiResponse.ok(null, "Tag deleted"));
    }
}
