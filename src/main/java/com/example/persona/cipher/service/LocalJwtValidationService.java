package com.example.persona.cipher.service;

import kh.com.wingbank.cipher.token.service.JwtValidationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * Local-profile implementation of {@link JwtValidationService} that bypasses JWT
 * validation so the app can run without the real wing-token library.
 *
 * <p><strong>Never use in production.</strong>
 */
@Slf4j
@Service
@Profile("local")
public class LocalJwtValidationService implements JwtValidationService {

    @Override
    public boolean validateJwtAssertionHeader(Object context) {
        log.debug("[LOCAL] Zero-trust validation bypassed");
        return true;
    }
}
