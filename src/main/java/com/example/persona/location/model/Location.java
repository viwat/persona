package com.example.persona.location.model;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

/**
 * Location aggregate root — the central domain object.
 * Pure Java: no JPA, no framework annotations.
 */
@Getter
@Builder(toBuilder = true)
public class Location {

    private final UUID id;
    private final String name;
    private final LocationType type;
    private final Coordinate coordinate;
    private final Address address;
    private final ContactInfo contactInfo;
    private final OpeningHours openingHours;
    private final List<String> availableServices;
    private final String logoUrl;
    private final String coverUrl;
    private final LocationStatus status;
    /** True when the location is temporarily closed (still ACTIVE but not serving customers). */
    private final boolean temporarilyClosed;
    /** When null, the closure has no scheduled end date. */
    private final Instant closedUntil;

    private final String createdBy;
    private final String updatedBy;
    private final Instant createdAt;
    private final Instant updatedAt;

    // ── Factory ─────────────────────────────────────────────────────────────

    public static Location create(
            String name,
            LocationType type,
            Coordinate coordinate,
            Address address,
            ContactInfo contactInfo,
            OpeningHours openingHours,
            List<String> availableServices,
            String logoUrl,
            String coverUrl,
            String actor) {
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(coordinate, "coordinate must not be null");
        Objects.requireNonNull(address, "address must not be null");

        Instant now = Instant.now();
        return Location.builder()
                .id(UUID.randomUUID())
                .name(name.trim())
                .type(type)
                .coordinate(coordinate)
                .address(address)
                .contactInfo(contactInfo)
                .openingHours(openingHours)
                .availableServices(
                        availableServices != null
                                ? Collections.unmodifiableList(availableServices)
                                : Collections.emptyList())
                .logoUrl(logoUrl)
                .coverUrl(coverUrl)
                .status(LocationStatus.ACTIVE)
                .temporarilyClosed(false)
                .closedUntil(null)
                .createdBy(actor)
                .updatedBy(actor)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    // ── Behaviour ────────────────────────────────────────────────────────────

    public Location update(
            String name,
            Coordinate coordinate,
            Address address,
            ContactInfo contactInfo,
            OpeningHours openingHours,
            List<String> availableServices,
            String logoUrl,
            String coverUrl,
            String actor) {
        return this.toBuilder()
                .name(name != null ? name.trim() : this.name)
                .coordinate(coordinate != null ? coordinate : this.coordinate)
                .address(address != null ? address : this.address)
                .contactInfo(contactInfo != null ? contactInfo : this.contactInfo)
                .openingHours(openingHours != null ? openingHours : this.openingHours)
                .availableServices(
                        availableServices != null
                                ? Collections.unmodifiableList(availableServices)
                                : this.availableServices)
                .logoUrl(logoUrl != null ? logoUrl : this.logoUrl)
                .coverUrl(coverUrl != null ? coverUrl : this.coverUrl)
                .updatedBy(actor)
                .updatedAt(Instant.now())
                .build();
    }

    public Location deactivate(String actor) {
        return this.toBuilder()
                .status(LocationStatus.INACTIVE)
                .updatedBy(actor)
                .updatedAt(Instant.now())
                .build();
    }

    public Location activate(String actor) {
        return this.toBuilder()
                .status(LocationStatus.ACTIVE)
                .updatedBy(actor)
                .updatedAt(Instant.now())
                .build();
    }

    /** Mark temporarily closed without changing ACTIVE status. */
    public Location closeTemporarily(Instant closedUntil, String actor) {
        return this.toBuilder()
                .temporarilyClosed(true)
                .closedUntil(closedUntil)
                .updatedBy(actor)
                .updatedAt(Instant.now())
                .build();
    }

    /** Clear temporary closure. */
    public Location reopen(String actor) {
        return this.toBuilder()
                .temporarilyClosed(false)
                .closedUntil(null)
                .updatedBy(actor)
                .updatedAt(Instant.now())
                .build();
    }

    public boolean isActive() {
        return LocationStatus.ACTIVE == this.status;
    }

    /**
     * Distance in km using Haversine formula.
     */
    public double distanceTo(Coordinate other) {
        return coordinate.distanceTo(other);
    }
}
