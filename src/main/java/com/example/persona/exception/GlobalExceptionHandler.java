package com.example.persona.exception;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.example.persona.enums.ErrorCode;
import com.example.persona.utils.TraceIdUtils;
import io.micrometer.tracing.Tracer;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.json.JsonParseException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final Tracer tracer;

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ProblemDetail> handleBaseException(BaseException ex, WebRequest request) {
        log.error("Base exception occurred:", ex);
        return buildProblemDetail(
                ex.getMessage(),
                ex.getErrorCode(),
                ex.getErrors(),
                httpStatusForBaseException(ex.getErrorCode()),
                request,
                ex.getMessage(),
                ex.getMetadata());
    }

    private static HttpStatus httpStatusForBaseException(ErrorCode errorCode) {
        if (errorCode == null) {
            return HttpStatus.BAD_REQUEST;
        }
        if (errorCode == ErrorCode.ERR_AUTH_INVALID) {
            return HttpStatus.FORBIDDEN;
        }
        return HttpStatus.BAD_REQUEST;
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<@NonNull ProblemDetail> handleDatabaseErrors(
            DataIntegrityViolationException ex, WebRequest request) {
        String message = ex.getMostSpecificCause().getMessage();
        return buildProblemDetail(
                message, ErrorCode.DATABASE_INSERT_ERROR, null, HttpStatus.CONFLICT, request, null, null);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ProblemDetail> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex, WebRequest request) {

        Map<String, String> errors = new HashMap<>();

        String supported = Arrays.toString(ex.getSupportedMethods());

        errors.put("method", ex.getMethod());
        errors.put("supportedMethods", supported);

        return buildProblemDetail(
                "HTTP method not supported",
                ErrorCode.METHOD_NOT_ALLOWED,
                errors,
                HttpStatus.METHOD_NOT_ALLOWED,
                request,
                null,
                null);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ProblemDetail> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException ex, WebRequest request) {

        Map<String, String> errors = new HashMap<>();

        String parameterName = ex.getParameterName();
        String parameterType = ex.getParameterType();

        errors.put(parameterName, "Parameter of type " + parameterType + " is required");

        return buildProblemDetail(
                "Missing required request parameter",
                ErrorCode.VALIDATION_ERROR,
                errors,
                HttpStatus.BAD_REQUEST,
                request,
                null,
                null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidationErrors(
            MethodArgumentNotValidException ex, WebRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult()
                .getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

        return buildProblemDetail(
                "Validation failed", ErrorCode.VALIDATION_ERROR, errors, HttpStatus.BAD_REQUEST, request, null, null);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ProblemDetail> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException ex, WebRequest request) {
        Map<String, String> errors = new HashMap<>();

        String fieldName = ex.getName(); // parameter name
        Object rejectedValue = ex.getValue(); // bad value sent by client
        Class<?> requiredType = ex.getRequiredType(); // expected type

        String message = String.format(
                "Parameter '%s' must be of type %s. Value '%s' is invalid.",
                fieldName, requiredType != null ? requiredType.getSimpleName() : "unknown", rejectedValue);

        errors.put(fieldName, message);

        return buildProblemDetail(
                "Validation failed", ErrorCode.VALIDATION_ERROR, errors, HttpStatus.BAD_REQUEST, request, null, null);
    }

    @ExceptionHandler(InvalidFormatException.class)
    public ResponseEntity<ProblemDetail> handleInvalidFormatException(InvalidFormatException ex, WebRequest request) {
        Map<String, String> errors = new HashMap<>();

        // Get field name from JSON path
        String fieldName = ex.getPath().stream()
                .map(JsonMappingException.Reference::getFieldName)
                .filter(Objects::nonNull)
                .collect(Collectors.joining("."));

        Object rejectedValue = ex.getValue();
        Class<?> targetType = ex.getTargetType();

        String message = String.format(
                "Field '%s' must be of type %s. Value '%s' is invalid.",
                fieldName, targetType != null ? targetType.getSimpleName() : "unknown", rejectedValue);

        errors.put(fieldName, message);

        return buildProblemDetail(
                "Validation failed", ErrorCode.VALIDATION_ERROR, errors, HttpStatus.BAD_REQUEST, request, null, null);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex, WebRequest request) {

        Map<String, String> errors = new HashMap<>();

        Throwable cause = ex.getCause();

        switch (cause) {
            case InvalidFormatException ife -> {
                String fieldName = ife.getPath().stream()
                        .map(JsonMappingException.Reference::getFieldName)
                        .filter(Objects::nonNull)
                        .collect(Collectors.joining("."));

                Object rejectedValue = ife.getValue();
                Class<?> targetType = ife.getTargetType();

                String message = String.format(
                        "Field '%s' must be of type %s. Value '%s' is invalid.",
                        fieldName, targetType != null ? targetType.getSimpleName() : "unknown", rejectedValue);

                errors.put(fieldName, message);
            }
            case JsonParseException ignored -> errors.put("request", "Malformed JSON syntax");
            case MismatchedInputException mie -> {
                String fieldName = mie.getPath().stream()
                        .map(JsonMappingException.Reference::getFieldName)
                        .filter(Objects::nonNull)
                        .collect(Collectors.joining("."));

                errors.put(fieldName.isEmpty() ? "request" : fieldName, "Missing or invalid value in request body");
            }
            case null, default -> errors.put("request", "Unreadable HTTP message");
        }

        return buildProblemDetail(
                "Validation failed", ErrorCode.VALIDATION_ERROR, errors, HttpStatus.BAD_REQUEST, request, null, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleAllUncaughtException(Exception ex, WebRequest request) {
        log.error("Unknown error occurred:", ex);
        String devMessage = ex.getCause() != null ? ex.getCause().getMessage() : null;
        return buildProblemDetail(
                "An unexpected error occurred",
                ErrorCode.INTERNAL_SERVER_ERROR,
                null,
                HttpStatus.INTERNAL_SERVER_ERROR,
                request,
                devMessage,
                null);
    }

    private ResponseEntity<ProblemDetail> buildProblemDetail(
            String message,
            ErrorCode errorCode,
            Map<String, String> errors,
            HttpStatus status,
            WebRequest request,
            String devMessage,
            Map<String, String> metadata) {

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, message);
        problem.setTitle(message);
        String path = request.getDescription(false);
        if (!path.isEmpty()) {
            String uri = path.replace("uri=", "").trim();
            if (!uri.isEmpty()) {
                try {
                    problem.setInstance(java.net.URI.create(uri));
                } catch (Exception ignored) {
                }
            }
        }

        problem.setProperty("timestamp", LocalDateTime.now().toString());
        problem.setProperty("code", errorCode != null ? errorCode.getCode() : "UNKNOWN");
        problem.setProperty("message", message);
        if (devMessage != null) {
            problem.setProperty("dev_message", devMessage);
        }
        if (errors != null && !errors.isEmpty()) {
            problem.setProperty("errors", new HashMap<>(errors));
        }
        if (metadata != null && !metadata.isEmpty()) {
            problem.setProperty("metadata", new HashMap<>(metadata));
        }
        problem.setProperty("trace_id", TraceIdUtils.getTraceId(tracer));
        problem.setProperty("span_id", TraceIdUtils.getSpanId(tracer));

        return new ResponseEntity<>(problem, status);
    }
}
