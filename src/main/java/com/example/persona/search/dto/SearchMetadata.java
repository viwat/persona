package com.example.persona.search.dto;

import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SearchMetadata {
    private long totalResults;
    private int currentPage;
    private int totalPages;
    private String nextCursor;
    private Map<String, Long> facets;
}
