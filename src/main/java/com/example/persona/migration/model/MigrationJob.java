package com.example.persona.migration.model;

import com.example.persona.migration.enums.MigrationJobStatus;
import com.example.persona.migration.enums.MigrationJobType;
import com.example.persona.model.BaseModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Tracks migration jobs for async processing. Each job has a UUID for
 * reference.
 */
@Entity
@Table(
        name = "dgtl_migration_job",
        indexes = {@Index(name = "idx_mj_customer_status", columnList = "customer_key, job_status")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class MigrationJob extends BaseModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "job_id", nullable = false, unique = true, length = 36)
    private String jobId;

    @Column(name = "customer_key", nullable = false)
    private String customerKey;

    @Column(name = "job_name", length = 50)
    private String jobName;

    @Enumerated(EnumType.STRING)
    @Column(name = "job_type", length = 50)
    private MigrationJobType jobType;

    @Column(name = "stage_code", length = 50)
    private String stageCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "job_status", nullable = false)
    private MigrationJobStatus jobStatus;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    @Column(name = "error_code", length = 50)
    private String errorCode;

    @Column(name = "metadata", length = 1000)
    private String metadata;

    @Column(name = "completed_stages")
    private Integer completedStages;

    @Column(name = "total_stages")
    private Integer totalStages;

    /**
     * Generate a new UUID for job_id.
     */
    public static String generateJobId() {
        return UUID.randomUUID().toString();
    }
}
