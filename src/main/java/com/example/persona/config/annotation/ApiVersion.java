package com.example.persona.config.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for API versioning. Can be used at class or method level to
 * specify API version.
 *
 * Usage:
 *
 * <pre>
 * {@code @ApiVersion("1.0")}
 * {@code @RestController}
 * public class UserController { ... }
 * </pre>
 *
 * @since 1.0.0
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiVersion {

    /**
     * API version string. Format: major.minor (e.g., "1.0", "2.1")
     *
     * @return the API version
     */
    String value();
}
