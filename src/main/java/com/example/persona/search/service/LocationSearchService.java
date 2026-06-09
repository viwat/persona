package com.example.persona.search.service;

import com.example.persona.location.dto.response.LocationResponse;
import com.example.persona.search.dto.LocationSearchResult;
import com.example.persona.search.dto.PaginatedResult;
import com.example.persona.search.dto.SearchCriteria;
import com.example.persona.search.dto.request.LocationSearch;
import com.example.persona.search.queries.LocationQueryBuilder;
import com.meilisearch.sdk.Client;
import com.meilisearch.sdk.Index;
import com.meilisearch.sdk.SearchRequest;
import com.meilisearch.sdk.exceptions.MeilisearchException;
import com.meilisearch.sdk.model.SearchResult;
import com.meilisearch.sdk.model.SearchResultPaginated;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
@Slf4j
public class LocationSearchService {
    private final Client meilisearchClient;
    private final ObjectMapper objectMapper;

    @Autowired
    public LocationSearchService(@Autowired(required = false) Client meilisearchClient, ObjectMapper objectMapper) {
        this.meilisearchClient = meilisearchClient;
        this.objectMapper = objectMapper;
    }

    private final String INDEX_NAME = "locations";

    private final String[] FILTERS =
            new String[] {"_id", "status", "location_type_name", "location_type_status", "tags", "_geo"};

    public PaginatedResult<LocationSearchResult> searchLocations(SearchCriteria criteria, int page, int pageSize) {
        if (meilisearchClient == null) {
            log.warn("Meilisearch is not available. Returning empty results.");
            return PaginatedResult.of(0, pageSize, 0, 0, List.of());
        }
        try {
            SearchRequest request = LocationQueryBuilder.buildQuery(criteria, page, pageSize);
            Index index = meilisearchClient.index(INDEX_NAME);
            SearchResultPaginated response = (SearchResultPaginated) index.search(request);
            List<LocationSearchResult> results = response.getHits().stream()
                    .map(hit -> objectMapper.convertValue(hit, LocationSearchResult.class))
                    .collect(Collectors.toList());
            return PaginatedResult.of(
                    response.getPage(),
                    response.getHitsPerPage(),
                    response.getTotalHits(),
                    response.getTotalPages(),
                    results);
        } catch (Exception e) {
            log.error("Error searching locations in Meilisearch", e);
            return PaginatedResult.of(0, pageSize, 0, 0, List.of());
        }
    }

    public PaginatedResult<LocationSearchResult> searchLocationsNearby(
            SearchCriteria criteria, int page, int pageSize) {
        if (meilisearchClient == null) {
            log.warn("Meilisearch is not available. Returning empty results.");
            return PaginatedResult.of(0, pageSize, 0, 0, List.of());
        }
        try {
            SearchRequest request = LocationQueryBuilder.buildNearbyQuery(criteria, page, pageSize);
            Index index = meilisearchClient.index(INDEX_NAME);
            SearchResultPaginated response = (SearchResultPaginated) index.search(request);
            List<LocationSearchResult> results = response.getHits().stream()
                    .map(hit -> objectMapper.convertValue(hit, LocationSearchResult.class))
                    .collect(Collectors.toList());
            return PaginatedResult.of(
                    response.getPage(),
                    response.getHitsPerPage(),
                    response.getTotalHits(),
                    response.getTotalPages(),
                    results);
        } catch (Exception e) {
            log.error("Error searching nearby locations in Meilisearch", e);
            return PaginatedResult.of(0, pageSize, 0, 0, List.of());
        }
    }

    public LocationResponse searchLocationById(Long locationId) {
        if (meilisearchClient == null || locationId == null) {
            return null;
        }
        try {
            String indexUid = String.format(INDEX_NAME);
            Index index = meilisearchClient.index(indexUid);
            SearchRequest request = SearchRequest.builder()
                    .filter(new String[] {"_id = " + locationId})
                    .build();
            request.setLimit(1);
            SearchResult response = (SearchResult) index.search(request);
            if (response.getHits() == null || response.getHits().isEmpty()) {
                return null;
            }
            return objectMapper.convertValue(response.getHits().getFirst(), LocationResponse.class);
        } catch (Exception e) {
            log.error("Search location by id {} failed: {}", locationId, e.getMessage(), e);
            return null;
        }
    }

    public PaginatedResult<LocationResponse> searchLocationsNearby(
            Double latitude, Double longitude, Double radiusKm, String agentType, Pageable pageable) {
        if (meilisearchClient == null) {
            return null;
        }
        try {
            SearchCriteria criteria = SearchCriteria.builder()
                    .radius(radiusKm)
                    .type(agentType)
                    .location(new Double[] {latitude, longitude})
                    .build();

            SearchRequest request =
                    LocationQueryBuilder.buildNearbyQuery(criteria, pageable.getPageNumber(), pageable.getPageSize());

            String indexUid = String.format(INDEX_NAME);
            Index index = meilisearchClient.index(indexUid);

            SearchResultPaginated response = (SearchResultPaginated) index.search(request);

            List<LocationResponse> results = response.getHits().stream()
                    .map(hit -> objectMapper.convertValue(hit, LocationResponse.class))
                    .collect(Collectors.toList());

            return PaginatedResult.of(
                    response.getPage(),
                    response.getHitsPerPage(),
                    response.getTotalHits(),
                    response.getTotalPages(),
                    results);

        } catch (Exception e) {
            log.error("Search failed with error {}", e.getMessage(), e);
            return PaginatedResult.of(0, pageable.getPageSize(), 0, 0, List.of());
        }
    }

    @Async
    public CompletableFuture<Void> addLocation(LocationSearch locationSearch) {
        if (meilisearchClient == null || locationSearch == null) {
            log.debug("Meilisearch is not available. Skipping indexing.");
            return CompletableFuture.completedFuture(null);
        }

        if (locationSearch.get_id() == null) {
            locationSearch.set_id(String.valueOf(locationSearch.getLocationId()));
        }

        return CompletableFuture.runAsync(() -> {
            try {
                String indexUid = String.format(INDEX_NAME);
                Index index = meilisearchClient.index(indexUid);
                index.updateFilterableAttributesSettings(FILTERS);
                index.addDocuments(objectMapper.writeValueAsString(locationSearch), "_id");
                log.info("Indexed location {}", locationSearch.getLocationId());
            } catch (Exception e) {
                log.error("Error indexing location", e);
            }
        });
    }

    @Async
    public CompletableFuture<Void> modifyLocation(LocationSearch locationSearch) {
        if (meilisearchClient == null || locationSearch == null) {
            log.debug("Meilisearch not available. Skipping update.");
            return CompletableFuture.completedFuture(null);
        }

        if (locationSearch._id == null) {
            locationSearch.set_id(String.valueOf(locationSearch.getLocationId()));
        }

        return CompletableFuture.runAsync(() -> {
            try {
                Index index = meilisearchClient.index(INDEX_NAME);
                index.updateDocuments(objectMapper.writeValueAsString(locationSearch), "_id");
                index.updateFilterableAttributesSettings(FILTERS);
                log.info("Successfully updated location {} in Meilisearch", locationSearch.getLocationId());
            } catch (MeilisearchException e) {
                log.error("Error updating location in Meilisearch", e);
            } catch (Exception e) {
                log.error("Unexpected error while updating", e);
            }
        });
    }

    public void clearAllLocations() {
        if (meilisearchClient == null) {
            log.debug("Meilisearch client not available. Skipping clear operation.");
            return;
        }

        try {
            Index index = meilisearchClient.index(INDEX_NAME);
            index.deleteAllDocuments();
            log.info("All documents cleared from Meilisearch index '{}'", INDEX_NAME);
        } catch (MeilisearchException e) {
            log.error("Error clearing documents in Meilisearch", e);
        }
    }
}
