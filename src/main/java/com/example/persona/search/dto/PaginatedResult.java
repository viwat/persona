package com.example.persona.search.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class PaginatedResult<T> {

    private int page;
    private int size;
    private long totalElements;
    private long totalPages;
    private List<T> data;

    public static <T> PaginatedResult<T> of(int page, int pageSize, long total, long totalPages, List<T> data) {
        return new PaginatedResult<>(page, pageSize, total, totalPages, data);
    }
}
