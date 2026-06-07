package com.example.persona.search.queries;

import com.meilisearch.sdk.SearchRequest;
import com.example.persona.search.dto.SearchCriteria;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class PeopleQueryBuilder {
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
        customerFilter(criteria, filters);
        geoFilter(criteria, filters);

        return filters.toArray(new String[0]);
    }

    private static void geoFilter(SearchCriteria criteria, List<String> filters) {
        if (criteria.getLocation() != null && criteria.getLocation().length == 2 && criteria.getRadius() != null) {
            filters.add(String.format(
                    "_geoRadius(%f, %f, %f)",
                    criteria.getLocation()[0], criteria.getLocation()[1], criteria.getRadius()));

            return;
        }

        if (criteria.getGeoBounds() != null) {
            filters.add("_geoBoundingBox("
                    + Arrays.stream(criteria.getGeoBounds())
                            .map(bound -> String.format("[%f, %f]", bound[0], bound[1]))
                            .collect(Collectors.joining(","))
                    + ")");
        }
    }

    private static void customerFilter(SearchCriteria criteria, List<String> filters) {
        if (criteria.getCustomerKey() != null) {
            filters.add(String.format("customer_key = %s", criteria.getCustomerKey()));
        }
    }
}
