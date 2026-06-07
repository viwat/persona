package com.example.persona.migration.dto;

import com.example.persona.migration.enums.MigrationStageStatus;
import com.example.persona.migration.model.CustomerMigrationStage;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerMigrationStageResponse {

    private Long id;
    private String customerKey;
    private MigrationStageResponse stage;
    private MigrationStageStatus stageStatus;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private Integer retryCount;
    private String errorMessage;
    private String errorCode;

    public static CustomerMigrationStageResponse fromEntity(CustomerMigrationStage cms) {
        return CustomerMigrationStageResponse.builder()
                .id(cms.getId())
                .customerKey(cms.getCustomerKey())
                .stage(MigrationStageResponse.fromEntity(cms.getStage()))
                .stageStatus(cms.getStageStatus())
                .startedAt(cms.getStartedAt())
                .completedAt(cms.getCompletedAt())
                .retryCount(cms.getRetryCount())
                .errorMessage(cms.getErrorMessage())
                .errorCode(cms.getErrorCode())
                .build();
    }
}
