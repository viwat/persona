package com.example.persona.location.controller;

import com.example.persona.location.dto.response.ApiResponse;
import com.example.persona.location.model.LocationType;
import com.example.persona.location.service.LocationSearchService;
import com.example.persona.location.service.LocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/search")
@RequiredArgsConstructor
@Tag(name = "Location - Admin", description = "Admin utilities")
public class SearchAdminController {

    private final LocationService locationService;
    private final LocationSearchService locationSearchService;

    @PostMapping("/reindex")
    @Operation(summary = "Re-index all active locations in Meilisearch")
    public ResponseEntity<ApiResponse<String>> reindex() {
        log.info("Starting full Meilisearch reindex...");
        var allLocations = locationService.findAll(Arrays.asList(LocationType.values()));
        locationSearchService.reindexAll(allLocations);
        String message = String.format("Reindexed %d locations", allLocations.size());
        log.info(message);
        return ResponseEntity.ok(ApiResponse.ok(message));
    }
}
