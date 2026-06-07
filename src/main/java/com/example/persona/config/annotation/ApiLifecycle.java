package com.example.persona.config.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for tracking API lifecycle and versioning. Supports semantic
 * versioning (major.minor.patch).
 *
 * Usage:
 *
 * <pre>
 * {@code @ApiLifecycle(since = "1.0.0", deprecated = "2.1.0")}
 * {@code @GetMapping("/api/users")}
 * public ResponseEntity<?> getUsers() { ... }
 * </pre>
 *
 * @since 1.0.0
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiLifecycle {

    /**
     * Version when this API was introduced. Format: major.minor.patch (e.g.,
     * "1.0.0", "2.1.3")
     *
     * @return the version since this API is available
     */
    String since() default "1.0.0";

    /**
     * Version when this API was deprecated. Format: major.minor.patch (e.g.,
     * "2.1.0", "3.0.0") Empty string means API is not deprecated.
     *
     * @return the version when API was deprecated, empty if not deprecated
     */
    String deprecated() default "";

    /**
     * Version when this API was removed. Format: major.minor.patch (e.g., "3.0.0",
     * "4.1.0") Empty string means API is still available.
     *
     * @return the version when API was removed, empty if still available
     */
    String removed() default "";

    /**
     * Optional message explaining deprecation or removal.
     *
     * @return deprecation/removal message
     */
    String message() default "";

    /**
     * Alternative API endpoint to use instead (for deprecated APIs).
     *
     * @return alternative endpoint path
     */
    String alternative() default "";
}
