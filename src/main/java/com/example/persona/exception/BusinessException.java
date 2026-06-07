package com.example.persona.exception;

import com.example.persona.enums.ErrorCode;
import java.util.Map;
import lombok.Getter;

@Getter
public class BusinessException extends BaseException {
    public BusinessException(String message) {
        super(message, ErrorCode.BUSINESS_ERROR);
    }

    public BusinessException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }

    public BusinessException(String message, Map<String, String> errors) {
        super(message, ErrorCode.BUSINESS_ERROR, errors);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, ErrorCode.BUSINESS_ERROR, cause);
    }
}
