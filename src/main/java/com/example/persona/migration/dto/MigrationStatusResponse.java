package com.example.persona.migration.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Overall migration status for a customer.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MigrationStatusResponse {

    private String customerKey;
    private boolean migrationCompleted;
    private boolean isFinalStage;
    private int completedStagesCount;
    private int totalStagesCount;
    private double progressPercentage;
    private CustomerMigrationStageResponse currentStage;
    private CustomerMigrationStageResponse nextPendingStage;
    private List<CustomerMigrationStageResponse> allStages;
}
