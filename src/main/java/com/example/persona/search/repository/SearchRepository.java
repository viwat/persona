package com.example.persona.search.repository;

import com.meilisearch.sdk.Client;
import com.meilisearch.sdk.Index;
import com.meilisearch.sdk.SearchRequest;
import com.meilisearch.sdk.model.SearchResultPaginated;
import com.example.persona.search.dto.LocationSearchResult;
import com.example.persona.search.dto.PaginatedResult;
import com.example.persona.search.dto.PeopleSearchResult;
import com.example.persona.search.dto.SearchCriteria;
import com.example.persona.search.dto.ServiceSearchResult;
import com.example.persona.search.dto.TransactionSearchResult;
import com.example.persona.search.queries.PropertyQueryBuilder;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import tools.jackson.databind.ObjectMapper;

@Repository
@RequiredArgsConstructor
public class SearchRepository {
    private final Optional<Client> client;
    private final ObjectMapper objectMapper;

    public PaginatedResult<ServiceSearchResult> search(SearchCriteria criteria) {
        if (client.isEmpty()) {
            return PaginatedResult.of(0, 10, 0, 0, List.of());
        }
        try {
            Index index = client.get().index("services");
            SearchRequest request = PropertyQueryBuilder.buildQuery(criteria, 0, 10);
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
            return PaginatedResult.of(0, 10, 0, 0, List.of());
        }
    }

    public PaginatedResult<TransactionSearchResult> searchCustomerTransactions(
            String customerId, SearchCriteria criteria, int page, int pageSize) {
        if (client.isEmpty()) {
            return PaginatedResult.of(0, 10, 0, 0, List.of());
        }
        // Use monthly indices to balance between performance and management
        String currentMonth = getCurrentMonthIndex();
        List<String> indexNames = getLastThreeMonthIndices(currentMonth);

        List<TransactionSearchResult> allResults = new ArrayList<>();
        int totalHits = 0;
        int totalPages = 0;

        for (String indexName : indexNames) {
            try {
                Index index = client.get().index(String.format("transactions_%s_%s", customerId, indexName));
                SearchRequest request = PropertyQueryBuilder.buildQuery(criteria, 0, 10);

                SearchResultPaginated response = (SearchResultPaginated) index.search(request);
                List<TransactionSearchResult> results = response.getHits().stream()
                        .map(hit -> objectMapper.convertValue(hit, TransactionSearchResult.class))
                        .collect(Collectors.toList());

                allResults.addAll(results);
                totalHits += response.getTotalHits();
                totalPages = Math.max(totalPages, response.getTotalPages());
            } catch (Exception e) {
                // Handle case where index doesn't exist (new customer or no transactions)
                continue;
            }
        }

        // Sort and paginate combined results
        List<TransactionSearchResult> paginatedResults = paginateResults(allResults, page, pageSize);

        return PaginatedResult.of(0, 10, totalHits, totalPages, paginatedResults);
    }

    private String getCurrentMonthIndex() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
    }

    private List<String> getLastThreeMonthIndices(String currentMonth) {
        List<String> indices = new ArrayList<>();
        YearMonth ym = YearMonth.parse(currentMonth, DateTimeFormatter.ofPattern("yyyyMM"));

        for (int i = 0; i < 3; i++) {
            indices.add(ym.minusMonths(i).format(DateTimeFormatter.ofPattern("yyyyMM")));
        }
        return indices;
    }

    private List<TransactionSearchResult> paginateResults(
            List<TransactionSearchResult> results, int page, int pageSize) {
        int start = page * pageSize;
        int end = Math.min(start + pageSize, results.size());
        return results.subList(start, end);
    }

    public PaginatedResult<PeopleSearchResult> searchPeople(SearchCriteria criteria) {
        if (client.isEmpty()) {
            return PaginatedResult.of(0, 10, 0, 0, List.of());
        }
        try {
            Index index = client.get().index("people");
            SearchRequest request = PropertyQueryBuilder.buildQuery(criteria, 0, 10);
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
            return PaginatedResult.of(0, 10, 0, 0, List.of());
        }
    }

    public PaginatedResult<LocationSearchResult> searchLocations(SearchCriteria criteria) {
        if (client.isEmpty()) {
            return PaginatedResult.of(0, 10, 0, 0, List.of());
        }
        try {
            Index index = client.get().index("locations");
            SearchRequest request = PropertyQueryBuilder.buildQuery(criteria, 0, 10);
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
            return PaginatedResult.of(0, 10, 0, 0, List.of());
        }
    }
}
