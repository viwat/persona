package com.example.persona.location.model;

import lombok.Value;

/**
 * Immutable geographic coordinate value object.
 * Includes Haversine distance calculation — no external dependencies.
 */
@Value
public class Coordinate {

    double latitude;
    double longitude;

    private static final double EARTH_RADIUS_KM = 6371.0;

    public Coordinate(double latitude, double longitude) {
        if (latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("Latitude must be between -90 and 90, got: " + latitude);
        }
        if (longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("Longitude must be between -180 and 180, got: " + longitude);
        }
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /**
     * Haversine distance to another coordinate in kilometres.
     */
    public double distanceTo(Coordinate other) {
        double lat1 = Math.toRadians(this.latitude);
        double lat2 = Math.toRadians(other.latitude);
        double deltaLat = Math.toRadians(other.latitude - this.latitude);
        double deltaLon = Math.toRadians(other.longitude - this.longitude);

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
                + Math.cos(lat1) * Math.cos(lat2) * Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }

    public boolean isWithinRadius(Coordinate center, double radiusKm) {
        return distanceTo(center) <= radiusKm;
    }

    @Override
    public String toString() {
        return String.format("(%f, %f)", latitude, longitude);
    }
}
