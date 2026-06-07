package com.example.persona.search.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SearchResponse {
    private String query;
    private SearchResultSection services;
    private SearchResultSection people;
    private SearchResultSection transactions;
    private SearchResultSection locations;
    private SearchResultSection billers;
    private SearchResultSection all;
    private SearchMetadata metadata;
}
