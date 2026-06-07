package com.example.persona.migration.dto;

import com.example.persona.migration.enums.MigrationJobStatus;
import com.example.persona.migration.enums.MigrationJobType;
import com.example.persona.migration.model.MigrationJob;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

/**
 * Response for migration job submission and status check.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class MigrationJobResponse {

    private String jobId;
    private String customerKey;
    private String jobType;
    private String stageCode;
    private MigrationJobStatus jobStatus;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private Integer completedStages;
    private Integer totalStages;
    private Double progressPercentage;
    private String errorMessage;
    private String errorCode;
    private String message;

    public static MigrationJobResponse fromEntity(MigrationJob job) {
        double progress = 0.0;
        if (job.getTotalStages() != null && job.getTotalStages() > 0 && job.getCompletedStages() != null) {
            progress = (job.getCompletedStages() * 100.0) / job.getTotalStages();
        }

        return MigrationJobResponse.builder()
                .jobId(job.getJobId())
                .customerKey(job.getCustomerKey())
                .jobType(job.getJobType() != null ? job.getJobType().name() : null)
                .stageCode(job.getStageCode())
                .jobStatus(job.getJobStatus())
                .startedAt(job.getStartedAt())
                .completedAt(job.getCompletedAt())
                .completedStages(job.getCompletedStages())
                .totalStages(job.getTotalStages())
                .progressPercentage(progress)
                .errorMessage(job.getErrorMessage())
                .errorCode(job.getErrorCode())
                .build();
    }

    public static MigrationJobResponse accepted(String jobId, String customerKey, MigrationJobType jobType) {
        return MigrationJobResponse.builder()
                .jobId(jobId)
                .customerKey(customerKey)
                .jobType(jobType != null ? jobType.name() : null)
                .jobStatus(MigrationJobStatus.ACCEPTED)
                .message("Migration job accepted and queued for processing")
                .build();
    }

    public static MigrationJobResponse alreadyCompleted(String customerKey) {
        return MigrationJobResponse.builder()
                .customerKey(customerKey)
                .jobStatus(MigrationJobStatus.COMPLETED)
                .progressPercentage(100.0)
                .message("Migration already completed for this customer")
                .build();
    }
}
