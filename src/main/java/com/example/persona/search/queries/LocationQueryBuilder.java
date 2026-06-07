package com.example.persona.search.queries;

import com.meilisearch.sdk.SearchRequest;
import com.example.persona.enums.StatusType;
import com.example.persona.search.dto.SearchCriteria;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class LocationQueryBuilder {
    public static SearchRequest buildQuery(SearchCriteria criteria, int page, int size) {

        return SearchRequest.builder()
                .q(criteria.getQuery())
                .page(page + 1)
                .offset(size * page)
                .filter(buildFilters(criteria))
                .build();
    }

    public static SearchRequest buildNearbyQuery(SearchCriteria criteria, int page, int size) {
        return SearchRequest.builder()
                .page(page + 1)
                .offset(size * page)
                .filter(buildFilters(criteria))
                .build();
    }

    private static String[] buildFilters(SearchCriteria criteria) {
        List<String> filters = new ArrayList<>();
        locationTypeFilter(criteria, filters);
        geoFilter(criteria, filters);
        return filters.toArray(new String[0]);
    }

    private static void locationTypeFilter(SearchCriteria criteria, List<String> filters) {
        String type = criteria.getType();

        if (type != null && !"ALL".equalsIgnoreCase(type)) {
            filters.add(String.format("location_type_name = %s", type));
        }
        filters.add(String.format("status = %s", StatusType.ACTIVE));
        filters.add(String.format("location_type_status = %s", StatusType.ACTIVE));
    }

    private static void geoFilter(SearchCriteria criteria, List<String> filters) {
        if (criteria.getLocation() != null && criteria.getLocation().length == 2 && criteria.getRadius() != null) {

            filters.add(String.format(
                    Locale.US,
                    "_geoRadius(%f, %f, %f)",
                    criteria.getLocation()[0],
                    criteria.getLocation()[1],
                    criteria.getRadius()));
            return;
        }

        if (criteria.getGeoBounds() != null) {
            filters.add("_geoBoundingBox("
                    + Arrays.stream(criteria.getGeoBounds())
                            .map(b -> String.format(Locale.US, "[%f, %f]", b[0], b[1]))
                            .collect(Collectors.joining(","))
                    + ")");
        }
    }
}
