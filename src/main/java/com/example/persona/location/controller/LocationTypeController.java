package com.example.persona.location.controller;

import com.example.persona.location.dto.response.ApiResponse;
import com.example.persona.location.dto.response.LocationTypeResponse;
import com.example.persona.location.service.LocationTypeService;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Public location type catalogue for the locator filters (FR-01 / FR-03). */
@Slf4j
@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Tag(name = "Location - Categories", description = "Location types (categories) and tags for locator filters")
public class LocationTypeController {

    private final LocationTypeService locationTypeService;

    @GetMapping
    @Operation(
            summary = "List location types",
            description =
                    "Returns active location types (with tags, multilingual names, and marker icons) for locator filters.")
    @Timed(value = "http.location.categories.list")
    public ResponseEntity<ApiResponse<List<LocationTypeResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.ok(locationTypeService.listLocationTypes()));
    }

    @GetMapping("/{code}")
    @Operation(summary = "Get a single location type by code")
    @Timed(value = "http.location.categories.get")
    public ResponseEntity<ApiResponse<LocationTypeResponse>> get(@PathVariable String code) {
        return ResponseEntity.ok(ApiResponse.ok(locationTypeService.getByCode(code)));
    }
}
