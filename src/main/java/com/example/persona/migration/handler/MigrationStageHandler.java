package com.example.persona.migration.handler;

import com.example.persona.migration.dto.MigrationResult;
import com.example.persona.migration.model.CustomerMigrationStage;

/**
 * Interface for migration stage handlers. Each stage (MASTER_ACCOUNT,
 * DEVICE, ACCOUNT, CARD) should have its own handler implementation.
 */
public interface MigrationStageHandler {

    /**
     * Get the stage code this handler handles.
     *
     * @return stage code (e.g., "MASTER_ACCOUNT")
     */
    String getStageCode();

    /**
     * Execute the migration logic for this stage.
     *
     * @param customerMigrationStage
     *            the customer's stage record
     * @return MigrationResult with success/failure status
     */
    MigrationResult execute(CustomerMigrationStage customerMigrationStage);
}
