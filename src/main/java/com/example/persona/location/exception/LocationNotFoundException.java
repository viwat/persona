package com.example.persona.location.exception;

import java.util.UUID;

public class LocationNotFoundException extends RuntimeException {

    private final UUID locationId;

    public LocationNotFoundException(UUID id) {
        super("Location not found: " + id);
        this.locationId = id;
    }

    public UUID getLocationId() {
        return locationId;
    }
}
