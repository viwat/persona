package com.example.persona.i18n.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark fields that require translation. The translation will be
 * fetched from TranslationService using the specified code.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface Translatable {
    /**
     * The translation code to fetch from TranslationService
     */
    String value();
}
