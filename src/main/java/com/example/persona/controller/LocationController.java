package com.example.persona.controller;

import com.example.persona.enums.StatusType;
import com.example.persona.location.dto.request.LocationAvailableCreateRequest;
import com.example.persona.location.dto.request.LocationCreateRequest;
import com.example.persona.location.dto.request.LocationDetailCreateRequest;
import com.example.persona.location.dto.request.LocationModifyRequest;
import com.example.persona.location.dto.request.LocationNearByRequest;
import com.example.persona.location.dto.request.LocationOperatingHourCreateRequest;
import com.example.persona.location.dto.request.LocationOperatingHourModifyRequest;
import com.example.persona.location.dto.request.LocationOperatingHoursCreateRequest;
import com.example.persona.location.dto.request.LocationTypeCreateRequest;
import com.example.persona.location.dto.response.LocationAvailableServiceResponse;
import com.example.persona.location.dto.response.LocationOperatingHourResponse;
import com.example.persona.location.dto.response.LocationResponse;
import com.example.persona.location.dto.response.LocationTypeResponse;
import com.example.persona.location.service.LocationService;
import com.example.persona.search.dto.PaginatedResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/locations")
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(name = "Location API", description = "APIs for managing location data")
public class LocationController {

    private final LocationService locationService;

    @PostMapping("/create")
    public ResponseEntity<@NonNull LocationResponse> createLocation(@Valid @RequestBody LocationCreateRequest request) {
        LocationResponse location = locationService.createLocation(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(location);
    }

    @PutMapping("/{locationId}/modify")
    public ResponseEntity<@NonNull LocationResponse> modifyLocation(
            @PathVariable Long locationId, @Valid @RequestBody LocationModifyRequest request) {
        LocationResponse location = locationService.modifyLocation(locationId, request);
        return ResponseEntity.status(HttpStatus.OK).body(location);
    }

    @PatchMapping("/{locationId}/modify-status")
    public ResponseEntity<@NonNull LocationResponse> modifyLocationStatus(
            @PathVariable Long locationId, @RequestParam StatusType statusType) {
        LocationResponse location = locationService.modifyLocationStatus(locationId, statusType);
        return ResponseEntity.status(HttpStatus.OK).body(location);
    }

    @PostMapping("/create-with-detail")
    public ResponseEntity<@NonNull LocationResponse> createLocationWithDetail(
            @Valid @RequestBody LocationDetailCreateRequest request) {
        LocationResponse location = locationService.createLocationWithDetail(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(location);
    }

    @PostMapping("/types/create")
    public ResponseEntity<@NonNull LocationTypeResponse> createLocationType(
            @Valid @RequestBody LocationTypeCreateRequest request) {
        LocationTypeResponse typeResponse = locationService.createLocationType(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(typeResponse);
    }

    @PostMapping("/{locationId}/available-services/create")
    public ResponseEntity<@NonNull LocationAvailableServiceResponse> createLocationAvailable(
            @PathVariable Long locationId, @Valid @RequestBody LocationAvailableCreateRequest request) {
        LocationAvailableServiceResponse response = locationService.createLocationAvailable(locationId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{locationId}/available-services/{availableServiceId}/delete")
    public ResponseEntity<@NonNull LocationAvailableServiceResponse> deleteLocationAvailableService(
            @PathVariable Long locationId, @PathVariable Long availableServiceId) {
        LocationAvailableServiceResponse response =
                locationService.deleteLocationAvailableService(locationId, availableServiceId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/{locationId}/operating-hours/create")
    public ResponseEntity<@NonNull LocationOperatingHourResponse> createLocationOperatingHour(
            @PathVariable Long locationId, @Valid @RequestBody LocationOperatingHourCreateRequest request) {
        LocationOperatingHourResponse response = locationService.createLocationOperatingHour(locationId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{locationId}/operating-hours/{operatingHourId}/modify")
    public ResponseEntity<@NonNull LocationOperatingHourResponse> modifyLocationOperatingHour(
            @PathVariable Long locationId,
            @PathVariable Long operatingHourId,
            @Valid @RequestBody LocationOperatingHourModifyRequest request) {
        LocationOperatingHourResponse response =
                locationService.modifyLocationOperatingHour(locationId, operatingHourId, request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/{locationId}/operating-hours/{operatingHourId}/delete")
    public ResponseEntity<@NonNull LocationOperatingHourResponse> deleteLocationOperatingHour(
            @PathVariable Long locationId, @PathVariable Long operatingHourId) {
        LocationOperatingHourResponse response =
                locationService.deleteLocationOperatingHour(locationId, operatingHourId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/{locationId}/operating-hours/batch")
    public ResponseEntity<@NonNull List<LocationOperatingHourResponse>> batchCreateOperatingHours(
            @PathVariable Long locationId, @Valid @RequestBody LocationOperatingHoursCreateRequest request) {
        List<LocationOperatingHourResponse> response =
                locationService.createLocationOperatingHours(locationId, request.getRequests());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{locationId}/detail")
    public ResponseEntity<@NonNull LocationResponse> findLocationDetail(@PathVariable Long locationId) {
        LocationResponse response = locationService.findLocationById(locationId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/near-by")
    @Operation(
            summary = "Search locations by type and coordinates",
            description = "Retrieve locations filtered by type and sorted by distance from given coordinates")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved locations")
    public ResponseEntity<@NonNull PaginatedResult<LocationResponse>> searchLocations(
            @RequestBody LocationNearByRequest request, Pageable pageable) {
        String locationType = request.getLocationType();
        Double latitude = request.getLatitude();
        Double longitude = request.getLongitude();
        Double radiusKm = request.getRadiusKm();
        PaginatedResult<LocationResponse> locations =
                locationService.findLocationsNearby(locationType, latitude, longitude, radiusKm, pageable);
        return ResponseEntity.ok(locations);
    }

    @PostMapping("/reload-meilisearch")
    public ResponseEntity<@NonNull String> reloadMeilisearch() {
        locationService.reloadMeilisearch();
        return ResponseEntity.status(HttpStatus.CREATED).body("Successfully reloaded meilisearch");
    }

    @PostMapping("/reload-meilisearch-by-location-id")
    public ResponseEntity<@NonNull String> reloadMeilisearchByLocationId(@RequestParam Long locationId) {
        locationService.reloadMeilisearchByLocationId(locationId);
        return ResponseEntity.status(HttpStatus.CREATED).body("Successfully reloaded meilisearch by locationId");
    }

    @PostMapping("/reload-meilisearch-by-location-type-id")
    public ResponseEntity<@NonNull String> reloadMeilisearchByLocationTypeId(@RequestParam Long locationTypeId) {
        locationService.reloadMeilisearchByLocationTypeId(locationTypeId);
        return ResponseEntity.status(HttpStatus.CREATED).body("Successfully reloaded meilisearch by locationTypeId");
    }
}
