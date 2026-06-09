package com.example.persona.location.model;

import lombok.Builder;
import lombok.Value;

/**
 * Address value object with Cambodia-specific administrative divisions.
 * Supports search by province / district / commune.
 */
@Value
@Builder
public class Address {

    String street;
    String commune;
    String district;
    String province;
    String country;

    public String fullAddress() {
        StringBuilder sb = new StringBuilder();
        if (street != null && !street.isBlank()) sb.append(street).append(", ");
        if (commune != null && !commune.isBlank()) sb.append(commune).append(", ");
        if (district != null && !district.isBlank()) sb.append(district).append(", ");
        sb.append(province).append(", ").append(country);
        return sb.toString();
    }
}
