package com.example.persona.search.dto;

import com.example.persona.dto.CustomerBaseRequest;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
public class SearchRequestDTO extends CustomerBaseRequest {
    @NotBlank(message = "Search query cannot be empty")
    private String query;

    @NotBlank(message = "Customer number is required")
    private String customerNo;

    @Builder.Default
    private int limit = 10;

    private String sortBy;
    private String sortDirection;
    private List<String> filters;
}
