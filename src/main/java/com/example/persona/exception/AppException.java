package com.example.persona.exception;

import com.example.persona.enums.ErrorCode;
import lombok.Getter;

@Getter
public class AppException extends BaseException {
    public AppException(String message) {
        super(message, ErrorCode.INTERNAL_SERVER_ERROR);
    }

    public AppException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }

    public AppException(String message, ErrorCode errorCode, Throwable cause) {
        super(message, errorCode, cause);
    }
}
