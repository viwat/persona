package com.example.persona.migration.model;

import com.example.persona.migration.enums.MigrationStageStatus;
import com.example.persona.model.BaseModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Tracks each customer's migration progress per stage. Each customer has one
 * record per stage.
 */
@Entity
@Table(
        name = "dgtl_customer_migration_stage",
        uniqueConstraints = @UniqueConstraint(columnNames = {"customer_key", "stage_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class CustomerMigrationStage extends BaseModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_key", nullable = false)
    private String customerKey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stage_id", nullable = false)
    private MigrationStage stage;

    @Enumerated(EnumType.STRING)
    @Column(name = "stage_status", nullable = false)
    private MigrationStageStatus stageStatus;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "retry_count")
    private Integer retryCount;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    @Column(name = "error_code", length = 50)
    private String errorCode;

    @Column(name = "metadata", length = 1000)
    private String metadata;
}
