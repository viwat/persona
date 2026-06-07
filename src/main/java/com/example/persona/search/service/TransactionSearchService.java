package com.example.persona.search.service;

import com.meilisearch.sdk.Client;
import com.meilisearch.sdk.Index;
import com.meilisearch.sdk.SearchRequest;
import com.meilisearch.sdk.exceptions.MeilisearchException;
import com.meilisearch.sdk.model.SearchResultPaginated;
import com.example.persona.search.dto.PaginatedResult;
import com.example.persona.search.dto.SearchCriteria;
import com.example.persona.search.dto.TransactionSearchResult;
import com.example.persona.search.dto.request.TransactionSearch;
import com.example.persona.search.queries.TransactionQueryBuilder;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Service
public class TransactionSearchService {

    private final Client meilisearchClient;
    private final ObjectMapper objectMapper;
    private final String INDEX_FORMAT = "transactions_%s_%s";

    @Autowired
    public TransactionSearchService(@Autowired(required = false) Client meilisearchClient, ObjectMapper objectMapper) {
        this.meilisearchClient = meilisearchClient;
        this.objectMapper = objectMapper;
        log.info("TransactionSearchService initialized - Meilisearch client present: {}", meilisearchClient != null);
    }

    public PaginatedResult<TransactionSearchResult> searchCustomerTransactions(
            String customerId, SearchCriteria criteria, int page, int pageSize) {
        if (meilisearchClient == null) {
            log.warn("Meilisearch is not available. Returning empty results.");
            return PaginatedResult.of(page, pageSize, 0, 0, List.of());
        }
        // Use monthly indices to balance between performance and management
        String currentMonth = getCurrentMonthIndex();
        List<String> indexNames = getLastThreeMonthIndices(currentMonth);

        List<TransactionSearchResult> allResults = new ArrayList<>();
        int totalHits = 0;
        int totalPages = 0;

        for (String indexName : indexNames) {
            try {
                Index index = meilisearchClient.index(String.format(INDEX_FORMAT, customerId, indexName));
                SearchRequest request = TransactionQueryBuilder.buildQuery(criteria, page, pageSize);

                SearchResultPaginated response = (SearchResultPaginated) index.search(request);
                List<TransactionSearchResult> results = response.getHits().stream()
                        .map(hit -> objectMapper.convertValue(hit, TransactionSearchResult.class))
                        .toList();

                allResults.addAll(results);
                totalHits += response.getTotalHits();
                totalPages = Math.max(totalPages, response.getTotalPages());
            } catch (Exception e) {
                // Handle case where index doesn't exist (new customer or no transactions)
                log.debug("Index doesn't exist or error searching: {}", e.getMessage());
                continue;
            }
        }

        // Sort and paginate combined results
        List<TransactionSearchResult> paginatedResults = paginateResults(allResults, page, pageSize);

        return PaginatedResult.of(page, pageSize, totalHits, totalPages, paginatedResults);
    }

    @Async
    public CompletableFuture<Void> addTransaction(TransactionSearch transaction) {

        if (transaction == null) {
            log.warn("TransactionSearch object is null");
            return CompletableFuture.completedFuture(null);
        }

        if (meilisearchClient == null) {
            log.debug("Meilisearch is not available. Skipping indexing.");
            return CompletableFuture.completedFuture(null);
        }
        log.info("Adding transaction to index: {}", transaction);
        if (transaction._id == null) {
            transaction._id = transaction.getTransactionId();
        }
        return CompletableFuture.runAsync(() -> {
            try {
                Index index = meilisearchClient.index(String.format(
                        INDEX_FORMAT, transaction.getCustomerKey(), transaction.getYear() + transaction.getMonth()));

                try {
                    index.getSettings();
                } catch (Exception e) {
                    log.info("Index doesn't exist, setting up filterable attributes");
                    String[] filterableAttributes = {
                        "account_no",
                        "transaction_id",
                        "title",
                        "service_name",
                        "description",
                        "tags",
                        "location",
                        "amount",
                        "currency"
                    };
                    index.updateFilterableAttributesSettings(filterableAttributes);
                    String[] sortableAttributes = {"transaction_timestamp"};
                    index.updateSortableAttributesSettings(sortableAttributes);
                }

                index.addDocuments(objectMapper.writeValueAsString(transaction), "transaction_id");
                log.info("Document indexed for transaction: {}", transaction);
            } catch (JacksonException | MeilisearchException e) {
                log.error("Error adding transaction to index", e);
            } catch (Exception e) {
                log.error("Unexpected error adding transaction to index", e);
            }
        });
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
}
