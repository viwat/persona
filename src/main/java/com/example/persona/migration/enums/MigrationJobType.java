package com.example.persona.migration.enums;

public enum MigrationJobType {

    /** Run all pending/failed stages for a customer from start to finish. */
    FULL_MIGRATION,

    /** Run a single, explicitly specified stage for a customer. */
    SINGLE_STAGE,

    /** Re-run only the stages that are currently in FAILED status. */
    RETRY
}
