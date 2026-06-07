package com.example.persona.controller;

import com.example.persona.cipher.annotation.ZeroTrust;
import com.example.persona.dto.ApiResponse;
import com.example.persona.search.dto.SearchCriteria;
import com.example.persona.search.dto.SearchResponse;
import com.example.persona.search.service.SearchService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kh.com.wingbank.cipher.token.annotation.TrackUserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@TrackUserContext
@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
@Tag(name = "Search API", description = "APIs for managing search data")
public class SearchController {

    private final SearchService searchService;

    @ZeroTrust
    @PostMapping("/all")
    public ResponseEntity<ApiResponse<SearchResponse>> search(@Valid @RequestBody SearchCriteria request) {
        return ResponseEntity.ok(ApiResponse.success(searchService.search(request)));
    }

    @ZeroTrust
    @PostMapping("/people")
    public ResponseEntity<ApiResponse<SearchResponse>> searchPeople(@Valid @RequestBody SearchCriteria request) {
        return ResponseEntity.ok(ApiResponse.success(searchService.searchOnlyPeople(request)));
    }

    @ZeroTrust
    @PostMapping("/services")
    public ResponseEntity<ApiResponse<SearchResponse>> searchServices(@Valid @RequestBody SearchCriteria request) {
        return ResponseEntity.ok(ApiResponse.success(searchService.searchOnlyServices(request)));
    }

    @ZeroTrust
    @PostMapping("/transactions")
    public ResponseEntity<ApiResponse<SearchResponse>> searchTransactions(@Valid @RequestBody SearchCriteria request) {
        return ResponseEntity.ok(ApiResponse.success(searchService.searchOnlyTransactions(request)));
    }

    @ZeroTrust
    @PostMapping("/locations")
    public ResponseEntity<ApiResponse<SearchResponse>> searchLocations(@Valid @RequestBody SearchCriteria request) {
        return ResponseEntity.ok(ApiResponse.success(searchService.searchLocations(request)));
    }

    @ZeroTrust
    @PostMapping("/locations/nearby")
    public ResponseEntity<ApiResponse<SearchResponse>> searchLocationsNearby(
            @Valid @RequestBody SearchCriteria request) {
        return ResponseEntity.ok(ApiResponse.success(searchService.searchLocationsNearby(request)));
    }

    @ZeroTrust
    @PostMapping("/billers")
    public ResponseEntity<ApiResponse<SearchResponse>> searchBillers(@Valid @RequestBody SearchCriteria request) {
        return ResponseEntity.ok(ApiResponse.success(searchService.searchBillers(request)));
    }
}
