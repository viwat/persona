package com.example.persona.location.exception;

/** Raised when a location type or tag cannot be found by its code. */
public class LocationTypeNotFoundException extends RuntimeException {

    private LocationTypeNotFoundException(String message) {
        super(message);
    }

    public static LocationTypeNotFoundException locationType(String code) {
        return new LocationTypeNotFoundException("Location type not found: " + code);
    }

    public static LocationTypeNotFoundException tag(String code) {
        return new LocationTypeNotFoundException("Tag not found: " + code);
    }
}
