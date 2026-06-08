package com.example.persona.migration.dto;

/**
 * Result returned by
 * {@link com.example.persona.migration.service.MigrationStageExecutionService#processStage}.
 * Carries the success flag plus error details so callers do not need to
 * re-fetch the stage entity to read its {@code errorCode}/{@code errorMessage}.
 */
public record StageProcessResult(boolean success, String errorCode, String errorMessage) {

    public static StageProcessResult ok() {
        return new StageProcessResult(true, null, null);
    }

    public static StageProcessResult failure(String errorCode, String errorMessage) {
        return new StageProcessResult(false, errorCode, errorMessage);
    }
}
