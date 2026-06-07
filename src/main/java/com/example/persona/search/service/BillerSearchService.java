package com.example.persona.search.service;

import com.meilisearch.sdk.Client;
import com.meilisearch.sdk.Index;
import com.meilisearch.sdk.SearchRequest;
import com.meilisearch.sdk.exceptions.MeilisearchException;
import com.meilisearch.sdk.model.SearchResultPaginated;
import com.example.persona.search.dto.BillerSearchResult;
import com.example.persona.search.dto.PaginatedResult;
import com.example.persona.search.dto.SearchCriteria;
import com.example.persona.search.dto.request.BillerSearch;
import com.example.persona.search.queries.BillerQueryBuilder;
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
public class BillerSearchService {

    private final Optional<Client> client;
    private final ObjectMapper objectMapper;
    private final String INDEX_FORMAT = "billers_%s";
    private final String INDEX_NAME = "apps";

    public PaginatedResult<BillerSearchResult> searchBillers(
            String customerId, SearchCriteria criteria, int page, int pageSize) {
        if (client.isEmpty()) {
            log.warn("Meilisearch is not available. Returning empty results.");
            return PaginatedResult.of(0, pageSize, 0, 0, List.of());
        }
        try {
            SearchRequest request = BillerQueryBuilder.buildQuery(criteria, page, pageSize);
            Index index = client.get().index(String.format(INDEX_FORMAT, INDEX_NAME));
            SearchResultPaginated response = (SearchResultPaginated) index.search(request);
            List<BillerSearchResult> results = response.getHits().stream()
                    .map(hit -> objectMapper.convertValue(hit, BillerSearchResult.class))
                    .collect(Collectors.toList());
            return PaginatedResult.of(
                    response.getPage(),
                    response.getHitsPerPage(),
                    response.getTotalHits(),
                    response.getTotalPages(),
                    results);
        } catch (Exception e) {
            log.error("Error searching billers in Meilisearch", e);
            return PaginatedResult.of(0, pageSize, 0, 0, List.of());
        }
    }

    @Async
    public CompletableFuture<Void> addBiller(BillerSearch billerSearch) {
        if (client.isEmpty()) {
            log.debug("Meilisearch is not available. Skipping indexing.");
            return CompletableFuture.completedFuture(null);
        }
        log.info("Adding biller to index: {}", billerSearch);
        if (billerSearch != null && billerSearch._id == null) {
            billerSearch._id = billerSearch.getCode();
        }
        return CompletableFuture.runAsync(() -> {
            try {
                Index index = client.get().index(String.format(INDEX_FORMAT, INDEX_NAME));

                try {
                    index.getSettings();
                } catch (Exception e) {
                    log.info("Index doesn't exist, setting up filterable attributes");
                    String[] filterableAttributes = {
                        "code", "name", "name_km", "name_zh", "category", "tags", "metadata"
                    };
                    index.updateFilterableAttributesSettings(filterableAttributes);
                }

                index.addDocuments(objectMapper.writeValueAsString(billerSearch));
            } catch (JacksonException | MeilisearchException e) {
                log.error("Error adding biller to index", e);
            } catch (Exception e) {
                log.error("Unexpected error adding biller to index", e);
            }
        });
    }
}
