package com.example.persona.search.service;

import com.example.persona.exception.BusinessException;
import com.example.persona.search.dto.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {

    private final ServiceSearchService serviceSearchService;
    private final PeopleSearchService peopleSearchService;
    private final TransactionSearchService transactionSearchService;
    private final LocationSearchService locationSearchService;
    private final BillerSearchService billerSearchService;

    private final ExecutorService executorService = Executors.newFixedThreadPool(4);

    @Transactional(readOnly = true)
    public SearchResponse search(SearchCriteria request) {
        log.debug("Performing search for customer: {} with query: {}", request.getCustomerNo(), request.getQuery());

        if (!StringUtils.hasText(request.getQuery())) {
            throw new BusinessException("Search query cannot be empty");
        }

        // Perform parallel searches for better performance
        CompletableFuture<SearchResultSection> servicesFuture =
                CompletableFuture.supplyAsync(() -> searchServices(request), executorService);

        CompletableFuture<SearchResultSection> peopleFuture =
                CompletableFuture.supplyAsync(() -> searchPeople(request), executorService);

        CompletableFuture<SearchResultSection> transactionsFuture =
                CompletableFuture.supplyAsync(() -> searchTransactions(request), executorService);

        // Wait for all searches to complete
        CompletableFuture.allOf(servicesFuture, peopleFuture, transactionsFuture)
                .join();

        try {
            SearchResultSection services = servicesFuture.get();
            SearchResultSection people = peopleFuture.get();
            SearchResultSection transactions = transactionsFuture.get();

            // Combine all results for the "All" section
            List<Object> allResults = new ArrayList<>();
            allResults.addAll(services.getResults());
            allResults.addAll(people.getResults());
            allResults.addAll(transactions.getResults());

            SearchResultSection allSection = SearchResultSection.builder()
                    .sectionType("ALL")
                    .totalCount(allResults.size())
                    .results(allResults)
                    .build();

            return SearchResponse.builder()
                    .query(request.getQuery())
                    .services(services)
                    .people(people)
                    .transactions(transactions)
                    .all(allSection)
                    .build();

        } catch (Exception e) {
            log.error("Error performing search", e);
            throw new BusinessException("Error performing search", e);
        }
    }

    public SearchResponse searchOnlyPeople(SearchCriteria request) {
        PaginatedResult<PeopleSearchResult> results =
                peopleSearchService.searchPeople(request.getCustomerKey(), request, 0, 10);
        SearchResultSection section = SearchResultSection.builder()
                .sectionType("PEOPLE")
                .totalCount(results.getSize())
                .results(results.getData())
                .build();

        return SearchResponse.builder()
                .query(request.getQuery())
                .people(section)
                .build();
    }

    public SearchResponse searchOnlyServices(SearchCriteria request) {
        PaginatedResult<ServiceSearchResult> results =
                serviceSearchService.searchServices(request.getCustomerKey(), request, 0, 10);
        SearchResultSection section = SearchResultSection.builder()
                .sectionType("SERVICES")
                .totalCount(results.getSize())
                .results(results.getData())
                .build();

        return SearchResponse.builder()
                .query(request.getQuery())
                .services(section)
                .build();
    }

    public SearchResponse searchOnlyTransactions(SearchCriteria request) {
        PaginatedResult<TransactionSearchResult> results =
                transactionSearchService.searchCustomerTransactions(request.getCustomerKey(), request, 0, 10);
        SearchResultSection section = SearchResultSection.builder()
                .sectionType("TRANSACTIONS")
                .totalCount(results.getSize())
                .results(results.getData())
                .build();

        return SearchResponse.builder()
                .query(request.getQuery())
                .transactions(section)
                .build();
    }

    private SearchResultSection searchServices(SearchCriteria request) {
        PaginatedResult<ServiceSearchResult> results =
                serviceSearchService.searchServices(request.getCustomerKey(), request, 0, 10);
        return SearchResultSection.builder()
                .sectionType("SERVICES")
                .totalCount(results.getSize())
                .results(results.getData())
                .build();
    }

    private SearchResultSection searchPeople(SearchCriteria request) {
        PaginatedResult<PeopleSearchResult> results =
                peopleSearchService.searchPeople(request.getCustomerKey(), request, 0, 10);

        return SearchResultSection.builder()
                .sectionType("PEOPLE")
                .totalCount(results.getSize())
                .results(results.getData())
                .build();
    }

    private SearchResultSection searchTransactions(SearchCriteria request) {
        PaginatedResult<TransactionSearchResult> results =
                transactionSearchService.searchCustomerTransactions(request.getCustomerKey(), request, 0, 10);

        return SearchResultSection.builder()
                .sectionType("TRANSACTIONS")
                .totalCount(results.getSize())
                .results(results.getData())
                .build();
    }

    public SearchResponse searchLocations(SearchCriteria request) {
        PaginatedResult<LocationSearchResult> results = locationSearchService.searchLocations(request, 0, 10);
        SearchResultSection section = SearchResultSection.builder()
                .sectionType("LOCATIONS")
                .totalCount(results.getSize())
                .results(results.getData())
                .build();

        return SearchResponse.builder()
                .query(request.getQuery())
                .locations(section)
                .build();
    }

    public SearchResponse searchLocationsNearby(SearchCriteria request) {
        PaginatedResult<LocationSearchResult> results = locationSearchService.searchLocationsNearby(request, 0, 10);
        SearchResultSection section = SearchResultSection.builder()
                .sectionType("LOCATIONS")
                .totalCount(results.getSize())
                .results(results.getData())
                .build();

        return SearchResponse.builder()
                .query(request.getQuery())
                .locations(section)
                .build();
    }

    public SearchResponse searchBillers(SearchCriteria request) {
        PaginatedResult<BillerSearchResult> results =
                billerSearchService.searchBillers(request.getCustomerKey(), request, 0, 10);
        SearchResultSection section = SearchResultSection.builder()
                .sectionType("BILLERS")
                .totalCount(results.getSize())
                .results(results.getData())
                .build();

        return SearchResponse.builder()
                .query(request.getQuery())
                .billers(section)
                .build();
    }
}
