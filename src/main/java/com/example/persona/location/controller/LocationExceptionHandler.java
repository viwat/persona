package com.example.persona.location.controller;

import com.example.persona.location.dto.response.ApiResponse;
import com.example.persona.location.exception.LocationDuplicateException;
import com.example.persona.location.exception.LocationNotFoundException;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice(basePackages = "com.example.persona.location.controller")
public class LocationExceptionHandler {

    @ExceptionHandler(LocationDuplicateException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicate(LocationDuplicateException ex) {
        log.debug("Duplicate location: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error("LOCATION_DUPLICATE", ex.getMessage()));
    }

    @ExceptionHandler(LocationNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(LocationNotFoundException ex) {
        log.debug("Location not found: {}", ex.getLocationId());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("LOCATION_NOT_FOUND", ex.getMessage()));
    }

    // @RequestBody validation failures (@Valid on request body DTOs)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
        List<ApiResponse.FieldError> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> new ApiResponse.FieldError(e.getField(), e.getDefaultMessage()))
                .toList();

        log.debug("Validation failed: {} field error(s)", fieldErrors.size());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.validationError(fieldErrors));
    }

    // @RequestParam / @PathVariable validation failures (@Min, @Max, @DecimalMin, etc.)
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(ConstraintViolationException ex) {
        List<ApiResponse.FieldError> fieldErrors = ex.getConstraintViolations().stream()
                .map(cv -> {
                    String path = cv.getPropertyPath().toString();
                    // Strip the method+param prefix: "findNearby.lat" → "lat"
                    String field = path.contains(".") ? path.substring(path.lastIndexOf('.') + 1) : path;
                    return new ApiResponse.FieldError(field, cv.getMessage());
                })
                .toList();

        log.debug("Constraint violation: {} error(s)", fieldErrors.size());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.validationError(fieldErrors));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String message = String.format("Invalid value '%s' for parameter '%s'", ex.getValue(), ex.getName());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error("INVALID_PARAMETER", message));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error("BAD_REQUEST", ex.getMessage()));
    }

    // Jackson wraps IllegalArgumentException from record compact constructors here
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotReadable(HttpMessageNotReadableException ex) {
        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
        String raw = cause.getMessage() != null ? cause.getMessage() : "Invalid request body";
        String message = raw.contains(", problem: ")
                ? raw.substring(raw.indexOf(", problem: ") + 11).split("\n")[0].trim()
                : raw.split("\n")[0].trim();
        log.debug("Request body not readable: {}", message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error("BAD_REQUEST", message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("INTERNAL_ERROR", "An unexpected error occurred"));
    }
}
