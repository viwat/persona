package com.example.persona.location.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(
        name = "audit_events",
        indexes = {
            @Index(name = "idx_audit_events_location_id", columnList = "location_id"),
            @Index(name = "idx_audit_events_occurred_at", columnList = "occurred_at")
        })
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditEventEntity {

    @Id
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "location_id", nullable = false, updatable = false)
    private UUID locationId;

    @Column(nullable = false, length = 50, updatable = false)
    private String action;

    @Column(length = 255, updatable = false)
    private String actor;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", updatable = false)
    private String snapshot;

    @Column(nullable = false, updatable = false)
    private Instant occurredAt;
}
