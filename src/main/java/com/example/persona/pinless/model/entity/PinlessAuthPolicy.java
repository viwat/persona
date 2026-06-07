package com.example.persona.pinless.model.entity;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.List;
import lombok.*;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "pinless_auth_policy", indexes = @Index(name = "idx_auth_policy_tier", columnList = "tier"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PinlessAuthPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Customer tier this policy applies to.
     * "DEFAULT" = global fallback for all customers with no specific tier.
     * Future: "PREMIUM", etc.
     */
    @Column(name = "tier", nullable = false, unique = true, length = 50)
    @Builder.Default
    private String tier = "DEFAULT";

    /**
     * Ordered list of auth steps when txn amount <= customer threshold.
     * Current: ["FACE_ID"]
     */
    @Type(JsonBinaryType.class)
    @Column(name = "below_threshold_steps", nullable = false, columnDefinition = "jsonb")
    private List<String> belowThresholdSteps;

    /**
     * Ordered list of auth steps when txn amount > customer threshold.
     * Current: ["FACE_ID", "PIN"]
     */
    @Type(JsonBinaryType.class)
    @Column(name = "above_threshold_steps", nullable = false, columnDefinition = "jsonb")
    private List<String> aboveThresholdSteps;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(name = "created_by", nullable = false, length = 100)
    private String createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        this.createdAt = Instant.now();
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = Instant.now();
    }
}
