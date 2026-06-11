package com.example.persona.location.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * JPA entity — infrastructure concern only.
 * Never leaks into domain or application layers.
 */
@Entity
@Table(
        name = "locations",
        indexes = {
            @Index(name = "idx_location_type", columnList = "type"),
            @Index(name = "idx_location_status", columnList = "status"),
            @Index(name = "idx_location_province", columnList = "province"),
            @Index(name = "idx_location_district", columnList = "district"),
            @Index(name = "idx_location_commune", columnList = "commune"),
            @Index(name = "idx_location_created_at", columnList = "created_at")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LocationEntity {

    @Id
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, length = 50)
    private String type;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(nullable = false)
    private double latitude;

    @Column(nullable = false)
    private double longitude;

    // ── Address ──────────────────────────────────────────────────────────────

    @Column(length = 500)
    private String street;

    @Column(length = 100)
    private String commune;

    @Column(length = 100)
    private String district;

    @Column(nullable = false, length = 100)
    private String province;

    @Column(length = 100)
    private String country;

    // ── Contact ──────────────────────────────────────────────────────────────

    @Column(length = 50)
    private String phone;

    @Column(length = 255)
    private String email;

    @Column(length = 500)
    private String website;

    @Column(name = "google_maps_url", length = 500)
    private String googleMapsUrl;

    @Column(name = "facebook_url", length = 500)
    private String facebookUrl;

    // ── Images ───────────────────────────────────────────────────────────────

    @Column(name = "logo_url", length = 1000)
    private String logoUrl;

    @Column(name = "cover_url", length = 1000)
    private String coverUrl;

    @Column(name = "image_url", length = 1000)
    private String imageUrl;

    // ── Source-system identifiers (FR-03) ─────────────────────────────────────

    @Column(name = "branch_code", length = 50)
    private String branchCode;

    @Column(name = "branch_name", length = 255)
    private String branchName;

    @Column(name = "atm_serial", length = 100)
    private String atmSerial;

    // ── Presentation metadata (FR-03) ──────────────────────────────────────────

    @Column(name = "avg_rating")
    private Double avgRating;

    @Column(name = "action_label", length = 255)
    private String actionLabel;

    @Column(name = "action_url", length = 1000)
    private String actionUrl;

    // ── Operating status ─────────────────────────────────────────────────────

    @Column(name = "temporarily_closed", nullable = false)
    private boolean temporarilyClosed;

    @Column(name = "closed_until")
    private Instant closedUntil;

    // ── Actor tracking ────────────────────────────────────────────────────────

    @Column(name = "created_by", length = 255)
    private String createdBy;

    @Column(name = "updated_by", length = 255)
    private String updatedBy;

    // ── Opening hours & services (stored as JSONB) ───────────────────────────

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "opening_hours", columnDefinition = "jsonb")
    private OpeningHoursJson openingHours;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "available_services", columnDefinition = "jsonb")
    private List<String> availableServices;

    // ── Audit ─────────────────────────────────────────────────────────────────

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    private Long version;

    // ── Embedded JSON types ───────────────────────────────────────────────────

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OpeningHoursJson {
        private java.util.Map<String, DayScheduleJson> schedule;
        private String specialNotes;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DayScheduleJson {
        private String openTime;
        private String closeTime;
    }
}
