package com.example.persona.location.exception;

/** Raised when a location category or tag cannot be found by its code. */
public class CategoryNotFoundException extends RuntimeException {

    private CategoryNotFoundException(String message) {
        super(message);
    }

    public static CategoryNotFoundException category(String code) {
        return new CategoryNotFoundException("Category not found: " + code);
    }

    public static CategoryNotFoundException tag(String code) {
        return new CategoryNotFoundException("Tag not found: " + code);
    }
}
