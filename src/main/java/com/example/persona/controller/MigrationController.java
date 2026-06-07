package com.example.persona.controller;

import com.example.persona.cipher.annotation.ZeroTrust;
import com.example.persona.dto.ApiResponse;
import com.example.persona.migration.dto.*;
import com.example.persona.migration.service.MigrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import kh.com.wingbank.cipher.token.annotation.TrackUserContext;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@TrackUserContext
@RestController
@RequestMapping("/migration")
@RequiredArgsConstructor
@Tag(
        name = "Migration",
        description = "Migration APIs for tracking Oracle to PostgreSQL migration (async background service)")
public class MigrationController {

    private final MigrationService migrationService;

    @ZeroTrust
    @PostMapping("/submit/{customerKey}")
    @Operation(
            summary = "Submit migration job",
            description =
                    "Submits a migration job for a customer. Returns immediately with job_id (UUID) for tracking. "
                            + "Migration runs asynchronously in background. Use /job/{jobId} to check progress.")
    public ResponseEntity<@NonNull ApiResponse<MigrationJobResponse>> submitMigration(
            @PathVariable String customerKey) {

        MigrationJobResponse job = migrationService.submitMigration(customerKey);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(ApiResponse.success(job));
    }

    @ZeroTrust
    @PostMapping("/submit/{customerKey}/stage/{stageCode}")
    @Operation(
            summary = "Submit single stage job",
            description = "Submits a single stage migration job. Returns immediately with job_id (UUID) for tracking.")
    public ResponseEntity<ApiResponse<MigrationJobResponse>> submitStage(
            @PathVariable String customerKey, @PathVariable String stageCode) {

        MigrationJobResponse job = migrationService.submitStage(customerKey, stageCode);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(ApiResponse.success(job));
    }

    @ZeroTrust
    @PostMapping("/retry/{customerKey}")
    @Operation(
            summary = "Submit retry job",
            description = "Submits a retry job for failed stages. Returns immediately with job_id (UUID) for tracking.")
    public ResponseEntity<ApiResponse<MigrationJobResponse>> submitRetry(@PathVariable String customerKey) {

        MigrationJobResponse job = migrationService.submitRetry(customerKey);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(ApiResponse.success(job));
    }

    @ZeroTrust
    @GetMapping("/job/{jobId}")
    @Operation(
            summary = "Get job status",
            description = "Gets the current status of a migration job by job_id (UUID). "
                    + "Shows progress percentage, completed stages, and any errors.")
    public ResponseEntity<ApiResponse<MigrationJobResponse>> getJobStatus(@PathVariable String jobId) {

        MigrationJobResponse job = migrationService.getJobStatus(jobId);
        return ResponseEntity.ok(ApiResponse.success(job));
    }

    @ZeroTrust
    @GetMapping("/jobs/{customerKey}")
    @Operation(
            summary = "Get customer jobs",
            description = "Gets all migration jobs for a customer, ordered by most recent first.")
    public ResponseEntity<ApiResponse<List<MigrationJobResponse>>> getCustomerJobs(@PathVariable String customerKey) {

        List<MigrationJobResponse> jobs = migrationService.getCustomerJobs(customerKey);
        return ResponseEntity.ok(ApiResponse.success(jobs));
    }

    @ZeroTrust
    @GetMapping("/check/{customerKey}")
    @Operation(
            summary = "Check migration status",
            description = "Checks the migration status for a customer. Returns current stage, "
                    + "whether at final stage, and next pending stage if not completed.")
    public ResponseEntity<ApiResponse<MigrationStatusResponse>> checkMigrationStatus(@PathVariable String customerKey) {

        MigrationStatusResponse status = migrationService.checkMigrationStatus(customerKey);
        return ResponseEntity.ok(ApiResponse.success(status));
    }

    @ZeroTrust
    @PutMapping("/stage/update")
    @Operation(
            summary = "Update stage status manually",
            description = "Manually updates the status of a specific migration stage for a customer. "
                    + "Use this to mark stages as COMPLETED, FAILED, or SKIPPED.")
    public ResponseEntity<ApiResponse<CustomerMigrationStageResponse>> updateStageStatus(
            @Valid @RequestBody UpdateStageRequest request) {

        CustomerMigrationStageResponse stage = migrationService.updateStageStatus(request);
        return ResponseEntity.ok(ApiResponse.success(stage));
    }

    @ZeroTrust
    @GetMapping("/stages")
    @Operation(summary = "Get all migration stages", description = "Returns all available migration stages in order")
    public ResponseEntity<ApiResponse<List<MigrationStageResponse>>> getAllStages() {

        List<MigrationStageResponse> stages = migrationService.getAllStages();
        return ResponseEntity.ok(ApiResponse.success(stages));
    }
}
