package com.example.persona.pinless.model.entity;

import com.example.persona.pinless.model.enums.BreachAction;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import lombok.*;

@Entity
@Table(name = "pinless_config", indexes = @Index(name = "idx_pinless_config_customer", columnList = "customer_no"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PinlessConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_no", nullable = false, unique = true, length = 50)
    private String customerNo;

    @Column(name = "channel", nullable = false, length = 20)
    @Builder.Default
    private String channel = "MOBILE";

    @Column(name = "currency", nullable = false, length = 3)
    @Builder.Default
    private String currency = "USD";

    @Column(name = "is_enabled", nullable = false)
    @Builder.Default
    private boolean enabled = false;

    // ── Threshold (slider value from UI) ─────────────────────────────────────
    // threshold_enabled = true  → apply threshold check
    // txn <= threshold_amount   → below_threshold_steps (e.g. [FACE_ID])
    // txn >  threshold_amount   → above_threshold_steps (e.g. [FACE_ID, PIN])
    // Customer cannot set threshold_amount > system cap (validated at API layer)

    @Column(name = "threshold_enabled", nullable = false)
    @Builder.Default
    private boolean thresholdEnabled = false;

    @Column(name = "threshold_amount", precision = 18, scale = 2)
    private BigDecimal thresholdAmount;

    // ── Daily count limit ─────────────────────────────────────────────────────

    @Column(name = "daily_count_enabled", nullable = false)
    @Builder.Default
    private boolean dailyCountEnabled = false;

    @Column(name = "daily_count_limit")
    private Integer dailyCountLimit;

    // ── Monthly count limit ───────────────────────────────────────────────────

    @Column(name = "monthly_count_enabled", nullable = false)
    @Builder.Default
    private boolean monthlyCountEnabled = false;

    @Column(name = "monthly_count_limit")
    private Integer monthlyCountLimit;

    // ── Daily amount limit ────────────────────────────────────────────────────

    @Column(name = "daily_amount_enabled", nullable = false)
    @Builder.Default
    private boolean dailyAmountEnabled = false;

    @Column(name = "daily_amount_limit", precision = 18, scale = 2)
    private BigDecimal dailyAmountLimit;

    // ── Monthly amount limit ──────────────────────────────────────────────────

    @Column(name = "monthly_amount_enabled", nullable = false)
    @Builder.Default
    private boolean monthlyAmountEnabled = false;

    @Column(name = "monthly_amount_limit", precision = 18, scale = 2)
    private BigDecimal monthlyAmountLimit;

    // ── Breach behavior ───────────────────────────────────────────────────────

    @Enumerated(EnumType.STRING)
    @Column(name = "breach_action", nullable = false, length = 20)
    @Builder.Default
    private BreachAction breachAction = BreachAction.ESCALATE_TO_PIN;

    // ── Metadata ──────────────────────────────────────────────────────────────

    @Column(name = "effective_date", nullable = false)
    @Builder.Default
    private LocalDate effectiveDate = LocalDate.now();

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
