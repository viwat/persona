package kh.com.wingbank.cipher.token.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Stub — replaced by the real wing-token library in deployed environments.
 * Marks a controller class so that user context is resolved from the JWT assertion header.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface TrackUserContext {}
