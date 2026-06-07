package com.example.persona.migration.dto;

/**
 * Result of migration stage execution.
 */
public record MigrationResult(boolean success, String errorCode, String errorMessage, String metadata) {
    public static MigrationResult completed() {
        return new MigrationResult(true, null, null, null);
    }

    public static MigrationResult completed(String metadata) {
        return new MigrationResult(true, null, null, metadata);
    }

    public static MigrationResult failure(String errorCode, String errorMessage) {
        return new MigrationResult(false, errorCode, errorMessage, null);
    }
}
