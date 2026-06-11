package com.example.persona.location.controller;

import com.example.persona.location.dto.response.ApiResponse;
import com.example.persona.location.dto.response.LocationResponse;
import com.example.persona.location.dto.response.PageResponse;
import com.example.persona.location.exception.LocationNotFoundException;
import com.example.persona.location.model.Coordinate;
import com.example.persona.location.service.LocationService;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/locations")
@RequiredArgsConstructor
@Validated
@Tag(name = "Location - Public", description = "Browse, search, and explore Wing Bank locations")
public class LocationPublicController {

    /** FR-05.2 defaults: the 5 nearest locations within a 4 km radius. Both are overridable via query params. */
    private static final String DEFAULT_NEARBY_RADIUS_KM = "4.0";

    private static final String DEFAULT_NEARBY_LIMIT = "5";
    private static final double DEFAULT_NEARBY_RADIUS_KM_VALUE = 4.0;
    private static final int DEFAULT_NEARBY_LIMIT_VALUE = 5;

    private final LocationService locationService;

    @GetMapping
    @Operation(
            summary = "Browse all locations (paginated)",
            description = "Returns active locations ordered by name. Supports pagination and optional type filter.")
    @Timed(value = "http.location.list")
    public ResponseEntity<ApiResponse<PageResponse<LocationResponse>>> getAll(
            @RequestParam(required = false) List<String> types,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "50") @Min(1) @Max(100) int size) {
        LocationService.SearchResult result = locationService.browse(types, page, size);
        List<LocationResponse> responses =
                result.locations().stream().map(LocationResponse::from).toList();
        return ResponseEntity.ok(
                ApiResponse.ok(PageResponse.of(responses, result.page(), result.size(), result.totalHits())));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get location detail")
    @Timed(value = "http.location.getById")
    public ResponseEntity<ApiResponse<LocationResponse>> getById(@PathVariable UUID id) {
        LocationResponse body = locationService
                .findById(id)
                .map(LocationResponse::from)
                .orElseThrow(() -> new LocationNotFoundException(id));
        return ResponseEntity.ok(ApiResponse.ok(body));
    }

    @GetMapping("/search")
    @Operation(
            summary = "Search locations",
            description = "Full-text search via Meilisearch with fallback to PostgreSQL.")
    @Timed(value = "http.location.search")
    public ResponseEntity<ApiResponse<PageResponse<LocationResponse>>> search(
            @RequestParam(required = false) @Size(max = 200) String q,
            @RequestParam(required = false) List<String> types,
            @RequestParam(required = false) @Size(max = 100) String province,
            @RequestParam(required = false) @Size(max = 100) String district,
            @RequestParam(required = false) @Size(max = 100) String commune,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        LocationService.SearchResult result = locationService.search(q, types, province, district, commune, page, size);
        List<LocationResponse> responses =
                result.locations().stream().map(LocationResponse::from).toList();
        return ResponseEntity.ok(
                ApiResponse.ok(PageResponse.of(responses, result.page(), result.size(), result.totalHits())));
    }

    @GetMapping("/nearby")
    @Operation(summary = "Find nearby locations")
    @Timed(value = "http.location.nearby")
    public ResponseEntity<ApiResponse<List<NearbyLocationResponse>>> findNearby(
            @RequestParam @DecimalMin("-90.0") @DecimalMax("90.0") double lat,
            @RequestParam @DecimalMin("-180.0") @DecimalMax("180.0") double lon,
            @RequestParam(defaultValue = DEFAULT_NEARBY_RADIUS_KM) @DecimalMin("0.1") @DecimalMax("50.0") double radius,
            @RequestParam(required = false) List<String> types,
            @RequestParam(defaultValue = DEFAULT_NEARBY_LIMIT) @Min(1) @Max(100) int limit) {
        List<NearbyLocationResponse> results =
                locationService.findNearby(new Coordinate(lat, lon), radius, types, limit).stream()
                        .map(r -> new NearbyLocationResponse(
                                LocationResponse.from(r.location()), Math.round(r.distanceKm() * 100.0) / 100.0))
                        .toList();
        return ResponseEntity.ok(ApiResponse.ok(results));
    }

    @PostMapping("/nearby")
    @Operation(
            summary = "Find nearby locations (current location)",
            description = "Same as GET /nearby but accepts a JSON body.")
    @Timed(value = "http.location.nearby.post")
    public ResponseEntity<ApiResponse<List<NearbyLocationResponse>>> findNearbyFromBody(
            @Valid @RequestBody NearbyRequest request) {
        List<NearbyLocationResponse> results = locationService
                .findNearby(
                        new Coordinate(request.lat(), request.lon()),
                        request.radiusKm() != null ? request.radiusKm() : DEFAULT_NEARBY_RADIUS_KM_VALUE,
                        request.types(),
                        request.limit() != null ? request.limit() : DEFAULT_NEARBY_LIMIT_VALUE)
                .stream()
                .map(r -> new NearbyLocationResponse(
                        LocationResponse.from(r.location()), Math.round(r.distanceKm() * 100.0) / 100.0))
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(results));
    }

    public record NearbyRequest(
            @NotNull @DecimalMin("-90.0") @DecimalMax("90.0")
            Double lat,

            @NotNull @DecimalMin("-180.0") @DecimalMax("180.0")
            Double lon,

            @DecimalMin("0.1") @DecimalMax("50.0") Double radiusKm,
            List<String> types,
            @Min(1) @Max(100) Integer limit) {}

    public record NearbyLocationResponse(LocationResponse location, double distanceKm) {}
}
