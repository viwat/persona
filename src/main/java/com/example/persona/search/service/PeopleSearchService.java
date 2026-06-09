package com.example.persona.search.service;

import com.example.persona.search.dto.PaginatedResult;
import com.example.persona.search.dto.PeopleSearchResult;
import com.example.persona.search.dto.SearchCriteria;
import com.example.persona.search.dto.request.PeopleSearch;
import com.example.persona.search.queries.PeopleQueryBuilder;
import com.meilisearch.sdk.Client;
import com.meilisearch.sdk.Index;
import com.meilisearch.sdk.SearchRequest;
import com.meilisearch.sdk.exceptions.MeilisearchException;
import com.meilisearch.sdk.model.SearchResultPaginated;
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
public class PeopleSearchService {
    private final Optional<Client> client;
    private final ObjectMapper objectMapper;
    private final String INDEX_FORMAT = "people_%s";
    private final String INDEX_NAME = "apps";

    public PaginatedResult<PeopleSearchResult> searchPeople(
            String customerId, SearchCriteria criteria, int page, int pageSize) {
        if (client.isEmpty()) {
            log.warn("Meilisearch is not available. Returning empty results.");
            return PaginatedResult.of(0, pageSize, 0, 0, List.of());
        }
        try {
            SearchRequest request = PeopleQueryBuilder.buildQuery(criteria, page, pageSize);
            Index index = client.get().index(String.format(INDEX_FORMAT, INDEX_NAME));
            SearchResultPaginated response = (SearchResultPaginated) index.search(request);
            List<PeopleSearchResult> results = response.getHits().stream()
                    .map(hit -> objectMapper.convertValue(hit, PeopleSearchResult.class))
                    .collect(Collectors.toList());
            return PaginatedResult.of(
                    response.getPage(),
                    response.getHitsPerPage(),
                    response.getTotalHits(),
                    response.getTotalPages(),
                    results);
        } catch (Exception e) {
            log.error("Error searching people in Meilisearch", e);
            return PaginatedResult.of(0, pageSize, 0, 0, List.of());
        }
    }

    @Async
    public CompletableFuture<Void> addPerson(PeopleSearch peopleSearch) {
        if (client.isEmpty()) {
            log.debug("Meilisearch is not available. Skipping indexing.");
            return CompletableFuture.completedFuture(null);
        }
        log.info("Adding person to index: {}", peopleSearch);
        if (peopleSearch != null && peopleSearch._id == null) {
            peopleSearch._id = peopleSearch.getCustomerKey();
        }
        return CompletableFuture.runAsync(() -> {
            try {
                Index index = client.get().index(String.format(INDEX_FORMAT, INDEX_NAME));

                try {
                    index.getSettings();
                } catch (Exception e) {
                    log.info("Index doesn't exist, setting up filterable attributes");
                    String[] filterableAttributes = {"customer_key", "name", "service_name", "description"};
                    index.updateFilterableAttributesSettings(filterableAttributes);
                    String[] sortableAttributes = {"create_timestamp"};
                    index.updateSortableAttributesSettings(sortableAttributes);
                }

                index.addDocuments(objectMapper.writeValueAsString(peopleSearch));
            } catch (JacksonException | MeilisearchException e) {
                log.error("Error adding person to index", e);
            } catch (Exception e) {
                log.error("Unexpected error adding person to index", e);
            }
        });
    }
}
