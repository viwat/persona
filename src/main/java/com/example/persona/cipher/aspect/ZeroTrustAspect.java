package com.example.persona.cipher.aspect;

import com.example.persona.enums.ErrorCode;
import com.example.persona.exception.AppException;
import kh.com.wingbank.cipher.token.context.UserContextHolder;
import kh.com.wingbank.cipher.token.service.JwtValidationService;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class ZeroTrustAspect {

    private final JwtValidationService validationService;

    @Before("@annotation(com.example.persona.cipher.annotation.ZeroTrust)")
    public void zeroTrustValidation() {
        boolean isValid = validationService.validateJwtAssertionHeader(UserContextHolder.getCurrentContext());
        if (!isValid) {
            throw new AppException("Zero trust validation failed", ErrorCode.ERR_AUTH_INVALID);
        }
    }
}
