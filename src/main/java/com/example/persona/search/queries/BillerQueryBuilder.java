package com.example.persona.search.queries;

import com.meilisearch.sdk.SearchRequest;
import com.example.persona.search.dto.SearchCriteria;
import java.util.ArrayList;
import java.util.List;

public class BillerQueryBuilder {
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
        billerCategoryFilter(criteria, filters);
        return filters.toArray(new String[0]);
    }

    private static void billerCategoryFilter(SearchCriteria criteria, List<String> filters) {
        if (criteria.getType() != null) {
            filters.add(String.format("category = %s", criteria.getType()));
        }
    }
}
