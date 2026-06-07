package com.example.persona.pinless.model.entity;

import com.example.persona.pinless.model.enums.ConfigAction;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.Map;
import lombok.*;
import org.hibernate.annotations.Type;

@Entity
@Table(
        name = "pinless_config_history",
        indexes = {
            @Index(name = "idx_pinless_history_customer", columnList = "customer_no"),
            @Index(name = "idx_pinless_history_config", columnList = "pinless_config_id"),
            @Index(name = "idx_pinless_history_changed", columnList = "changed_at DESC")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PinlessConfigHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pinless_config_id", nullable = false)
    private Long pinlessConfigId;

    @Column(name = "customer_no", nullable = false, length = 50)
    private String customerNo;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 20)
    private ConfigAction action;

    // JSONB snapshots — full old/new state
    @Type(JsonBinaryType.class)
    @Column(name = "old_value", columnDefinition = "jsonb")
    private Map<String, Object> oldValue;

    @Type(JsonBinaryType.class)
    @Column(name = "new_value", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> newValue;

    @Column(name = "changed_by", nullable = false, length = 100)
    private String changedBy;

    @Column(name = "changed_at", nullable = false)
    @Builder.Default
    private Instant changedAt = Instant.now();

    @Column(name = "change_reason", length = 255)
    private String changeReason;
}
