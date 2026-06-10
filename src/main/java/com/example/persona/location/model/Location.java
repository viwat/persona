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
    /** Primary display image for the location card (FR-03: Image). */
    private final String imageUrl;

    private final LocationStatus status;
    /** True when the location is temporarily closed (still ACTIVE but not serving customers). */
    private final boolean temporarilyClosed;
    /** When null, the closure has no scheduled end date. */
    private final Instant closedUntil;

    // ── Source-system identifiers (FR-03) ─────────────────────────────────────
    /** Core-banking branch code (FR-03: Branch Code). */
    private final String branchCode;
    /** Branch name as held in source systems, distinct from display {@link #name} (FR-03: Branch Name). */
    private final String branchName;
    /** ATM serial number for ATM/CRM locations (FR-03: ATM Serial). */
    private final String atmSerial;

    // ── Presentation metadata (FR-03) ──────────────────────────────────────────
    /** Category code linking to a {@code LocationCategory} (FR-03: Category). */
    private final String categoryCode;
    /** Average customer rating, 0.0–5.0 (FR-03: Avg Rating). May be null when unrated. */
    private final Double avgRating;
    /** Optional call-to-action button (FR-03: Action Label / Action URL). */
    private final ActionLink action;

    private final String createdBy;
    private final String updatedBy;
    private final Instant createdAt;
    private final Instant updatedAt;

    /**
     * User-supplied attributes for {@link #create} / {@link #update}. Grouping these
     * keeps the factory and mutation methods readable as the field set grows, and keeps
     * the system-managed fields (id, status, audit, timestamps) out of callers' hands.
     * On update, null components mean "leave unchanged".
     */
    @Builder(builderClassName = "DraftBuilder")
    public record Draft(
            String name,
            LocationType type,
            Coordinate coordinate,
            Address address,
            ContactInfo contactInfo,
            OpeningHours openingHours,
            List<String> availableServices,
            String logoUrl,
            String coverUrl,
            String imageUrl,
            String branchCode,
            String branchName,
            String atmSerial,
            String categoryCode,
            Double avgRating,
            ActionLink action) {}

    // ── Factory ─────────────────────────────────────────────────────────────

    public static Location create(Draft draft, String actor) {
        Objects.requireNonNull(draft, "draft must not be null");
        Objects.requireNonNull(draft.name(), "name must not be null");
        Objects.requireNonNull(draft.type(), "type must not be null");
        Objects.requireNonNull(draft.coordinate(), "coordinate must not be null");
        Objects.requireNonNull(draft.address(), "address must not be null");

        Instant now = Instant.now();
        return Location.builder()
                .id(UUID.randomUUID())
                .name(draft.name().trim())
                .type(draft.type())
                .coordinate(draft.coordinate())
                .address(draft.address())
                .contactInfo(draft.contactInfo())
                .openingHours(draft.openingHours())
                .availableServices(immutableOrEmpty(draft.availableServices()))
                .logoUrl(draft.logoUrl())
                .coverUrl(draft.coverUrl())
                .imageUrl(draft.imageUrl())
                .branchCode(draft.branchCode())
                .branchName(draft.branchName())
                .atmSerial(draft.atmSerial())
                .categoryCode(draft.categoryCode())
                .avgRating(draft.avgRating())
                .action(draft.action())
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

    /** Applies a partial patch — every null component in {@code patch} leaves the current value untouched. */
    public Location update(Draft patch, String actor) {
        return this.toBuilder()
                .name(patch.name() != null ? patch.name().trim() : this.name)
                .coordinate(patch.coordinate() != null ? patch.coordinate() : this.coordinate)
                .address(patch.address() != null ? patch.address() : this.address)
                .contactInfo(patch.contactInfo() != null ? patch.contactInfo() : this.contactInfo)
                .openingHours(patch.openingHours() != null ? patch.openingHours() : this.openingHours)
                .availableServices(
                        patch.availableServices() != null
                                ? immutableOrEmpty(patch.availableServices())
                                : this.availableServices)
                .logoUrl(patch.logoUrl() != null ? patch.logoUrl() : this.logoUrl)
                .coverUrl(patch.coverUrl() != null ? patch.coverUrl() : this.coverUrl)
                .imageUrl(patch.imageUrl() != null ? patch.imageUrl() : this.imageUrl)
                .branchCode(patch.branchCode() != null ? patch.branchCode() : this.branchCode)
                .branchName(patch.branchName() != null ? patch.branchName() : this.branchName)
                .atmSerial(patch.atmSerial() != null ? patch.atmSerial() : this.atmSerial)
                .categoryCode(patch.categoryCode() != null ? patch.categoryCode() : this.categoryCode)
                .avgRating(patch.avgRating() != null ? patch.avgRating() : this.avgRating)
                .action(patch.action() != null ? patch.action() : this.action)
                .updatedBy(actor)
                .updatedAt(Instant.now())
                .build();
    }

    private static List<String> immutableOrEmpty(List<String> values) {
        return values != null ? Collections.unmodifiableList(values) : Collections.emptyList();
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
