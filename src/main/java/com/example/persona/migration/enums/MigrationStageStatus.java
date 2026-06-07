package com.example.persona.migration.enums;

/**
 * Status for each migration stage per customer.
 */
public enum MigrationStageStatus {
    PENDING, // Stage not yet started
    IN_PROGRESS, // Stage currently being processed
    COMPLETED, // Stage completed successfully
    FAILED, // Stage failed, needs retry
    SKIPPED // Stage skipped (optional stage)
}
