package com.example.persona.migration.enums;

/**
 * Status for migration jobs.
 */
public enum MigrationJobStatus {
    ACCEPTED, // Job accepted, queued for processing
    IN_PROGRESS, // Job currently being processed
    COMPLETED, // Job completed successfully
    FAILED, // Job failed
    CANCELLED // Job cancelled
}
