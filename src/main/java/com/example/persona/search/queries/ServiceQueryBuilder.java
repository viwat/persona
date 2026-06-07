package com.example.persona.search.queries;

import com.meilisearch.sdk.SearchRequest;
import com.example.persona.search.dto.SearchCriteria;
import java.util.ArrayList;
import java.util.List;

public class ServiceQueryBuilder {
    public static SearchRequest buildQuery(SearchCriteria criteria, int page, int size) {

        return SearchRequest.builder()
                .q(criteria.getQuery())
                .page(page + 1)
                .offset(size * page)
                .filter(buildFilters(criteria))
                .build();
    }

    private static String[] buildFilters(SearchCriteria criteria) {
        List<String> filters = new ArrayList<>();
        return filters.toArray(new String[0]);
    }
}
