package com.example.persona.search.dto;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SearchResultSection {
    private String sectionType;
    private int totalCount;
    private List<?> results;
    private SearchMetadata metadata;
}
