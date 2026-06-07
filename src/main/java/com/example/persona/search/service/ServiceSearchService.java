package com.example.persona.search.service;

import com.meilisearch.sdk.Client;
import com.meilisearch.sdk.Index;
import com.meilisearch.sdk.SearchRequest;
import com.meilisearch.sdk.exceptions.MeilisearchException;
import com.meilisearch.sdk.model.SearchResultPaginated;
import com.example.persona.search.dto.PaginatedResult;
import com.example.persona.search.dto.SearchCriteria;
import com.example.persona.search.dto.ServiceSearchResult;
import com.example.persona.search.dto.request.ServiceSearch;
import com.example.persona.search.queries.ServiceQueryBuilder;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Service
@Slf4j
@RequiredArgsConstructor
public class ServiceSearchService {

    private final Optional<Client> client;
    private final ObjectMapper objectMapper;
    private final String INDEX_FORMAT = "services_%s";
    private final String INDEX_NAME = "apps";

    public PaginatedResult<ServiceSearchResult> searchServices(
            String customerId, SearchCriteria criteria, int page, int pageSize) {
        if (client.isEmpty()) {
            log.warn("Meilisearch is not available. Returning empty results.");
            return PaginatedResult.of(0, pageSize, 0, 0, List.of());
        }
        try {
            SearchRequest request = ServiceQueryBuilder.buildQuery(criteria, page, pageSize);
            Index index = client.get().index(String.format(INDEX_FORMAT, INDEX_NAME));
            SearchResultPaginated response = (SearchResultPaginated) index.search(request);
            List<ServiceSearchResult> results = response.getHits().stream()
                    .map(hit -> objectMapper.convertValue(hit, ServiceSearchResult.class))
                    .collect(Collectors.toList());
            return PaginatedResult.of(
                    response.getPage(),
                    response.getHitsPerPage(),
                    response.getTotalHits(),
                    response.getTotalPages(),
                    results);
        } catch (Exception e) {
            log.error("Error searching services in Meilisearch", e);
            return PaginatedResult.of(0, pageSize, 0, 0, List.of());
        }
    }

    @Async
    public CompletableFuture<Void> addService(ServiceSearch serviceSearch) {
        if (client.isEmpty()) {
            log.debug("Meilisearch is not available. Skipping indexing.");
            return CompletableFuture.completedFuture(null);
        }
        log.info("Adding service to index: {}", serviceSearch);
        if (serviceSearch != null && serviceSearch._id == null) {
            serviceSearch._id = serviceSearch.getCode();
        }
        return CompletableFuture.runAsync(() -> {
            try {
                Index index = client.get().index(String.format(INDEX_FORMAT, INDEX_NAME));

                try {
                    index.getSettings();
                } catch (Exception e) {
                    log.info("Index doesn't exist, setting up filterable attributes");
                    String[] filterableAttributes = {"name", "name_en", "name_zh", "tags", "metadata"};
                    index.updateFilterableAttributesSettings(filterableAttributes);
                    String[] sortableAttributes = {"display_order"};
                    index.updateSortableAttributesSettings(sortableAttributes);
                }

                index.addDocuments(objectMapper.writeValueAsString(serviceSearch));
            } catch (JacksonException | MeilisearchException e) {
                log.error("Error adding service to index", e);
            } catch (Exception e) {
                log.error("Unexpected error adding service to index", e);
            }
        });
    }
}
