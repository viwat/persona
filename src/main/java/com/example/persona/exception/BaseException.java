package com.example.persona.exception;

import com.example.persona.enums.ErrorCode;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;

@Getter
public abstract class BaseException extends RuntimeException {
    private final ErrorCode errorCode;
    private final Map<String, String> errors;
    private final Map<String, String> metadata;

    protected BaseException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.errors = new HashMap<>();
        this.metadata = new HashMap<>();
    }

    protected BaseException(String message, ErrorCode errorCode, Map<String, String> errors) {
        super(message);
        this.errorCode = errorCode;
        this.errors = errors;
        this.metadata = new HashMap<>();
    }

    protected BaseException(
            String message, ErrorCode errorCode, Map<String, String> errors, Map<String, String> metadata) {
        super(message);
        this.errorCode = errorCode;
        this.errors = errors;
        this.metadata = metadata;
    }

    protected BaseException(String message, ErrorCode errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.errors = new HashMap<>();
        this.metadata = new HashMap<>();
    }
}
