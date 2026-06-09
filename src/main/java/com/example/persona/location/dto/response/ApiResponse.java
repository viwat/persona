package com.example.persona.location.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.List;
import lombok.Builder;

/**
 * Standard API response envelope — consistent across all endpoints.
 */
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(boolean success, T data, String message, ErrorDetail error, Instant timestamp) {

    public static <T> ApiResponse<T> ok(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .timestamp(Instant.now())
                .build();
    }

    public static <T> ApiResponse<T> ok(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .message(message)
                .timestamp(Instant.now())
                .build();
    }

    public static <T> ApiResponse<T> error(String code, String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .error(new ErrorDetail(code, message, null))
                .timestamp(Instant.now())
                .build();
    }

    public static <T> ApiResponse<T> validationError(List<FieldError> fieldErrors) {
        return ApiResponse.<T>builder()
                .success(false)
                .error(new ErrorDetail("VALIDATION_ERROR", "Request validation failed", fieldErrors))
                .timestamp(Instant.now())
                .build();
    }

    public record ErrorDetail(String code, String message, List<FieldError> fieldErrors) {}

    public record FieldError(String field, String message) {}
}
