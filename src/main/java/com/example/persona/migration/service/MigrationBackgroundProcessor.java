package com.example.persona.migration.service;

import com.example.persona.enums.ErrorCode;
import com.example.persona.migration.enums.MigrationJobStatus;
import com.example.persona.migration.enums.MigrationStageStatus;
import com.example.persona.migration.model.CustomerMigrationStage;
import com.example.persona.migration.repository.CustomerMigrationStageRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Async migration jobs. Transactional persistence and per-stage work.
 *
 * <p><strong>isMandatory semantics:</strong> if a mandatory stage fails the entire job is
 * aborted and marked FAILED. If a non-mandatory stage fails it is marked SKIPPED so the
 * loop does not re-pick it, and processing continues with the next stage.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MigrationBackgroundProcessor {

    /** Statuses considered "actionable" — picked up by the background loop. */
    private static final List<MigrationStageStatus> ACTIONABLE_STATUSES =
            List.of(MigrationStageStatus.PENDING, MigrationStageStatus.FAILED);

    private final CustomerMigrationStageRepository customerStageRepository;
    private final CustomerStageReconciler customerStageReconciler;
    private final MigrationJobPersistenceService migrationJobPersistence;
    private final MigrationStageExecutionService migrationStageExecution;

    @Async("migrationTaskExecutor")
    public void processMigrationAsync(String jobId, String customerKey) {
        MDC.put("migration.job_id", jobId);
        MDC.put("migration.customer_key", customerKey);
        try {
            log.info("[ASYNC] Starting background migration job {} for customer: {}", jobId, customerKey);

            migrationJobPersistence.updateJobStatus(jobId, MigrationJobStatus.IN_PROGRESS, null, null);
            customerStageReconciler.ensureInitializedOrReconciled(customerKey);

            int totalStages = (int) customerStageRepository.countTotalActiveStages();
            int completedStages = 0;
            boolean continueProcessing = true;
            boolean jobFailed = false;

            while (continueProcessing) {
                Optional<CustomerMigrationStage> nextStageOpt = findNextStageToProcess(customerKey);

                if (nextStageOpt.isEmpty()) {
                    continueProcessing = false;
                } else {
                    CustomerMigrationStage stage = nextStageOpt.get();
                    boolean mandatory = Boolean.TRUE.equals(stage.getStage().getIsMandatory());
                    String stageCode = stage.getStage().getCode();

                    boolean success = migrationStageExecution.processStage(stage.getId());

                    if (success) {
                        completedStages++;
                        migrationJobPersistence.updateJobProgress(jobId, completedStages, totalStages);
                    } else if (mandatory) {
                        // Mandatory stage failed — abort the job.
                        CustomerMigrationStage failed = customerStageRepository
                                .findById(stage.getId())
                                .orElse(stage);
                        String errorCode = failed.getErrorCode() != null
                                ? failed.getErrorCode() : "STAGE_FAILED";
                        String errorMsg = failed.getErrorMessage() != null
                                ? failed.getErrorMessage() : "Mandatory stage failed: " + stageCode;

                        log.warn("[ASYNC] Mandatory stage {} failed for job {} — stopping migration",
                                stageCode, jobId);
                        migrationJobPersistence.updateJobStatus(
                                jobId, MigrationJobStatus.FAILED, errorCode, errorMsg);
                        jobFailed = true;
                        continueProcessing = false;
                    } else {
                        // Non-mandatory stage failed — mark SKIPPED so it is not re-picked, continue.
                        log.warn("[ASYNC] Non-mandatory stage {} failed for job {} — skipping and continuing",
                                stageCode, jobId);
                        CustomerMigrationStage failed = customerStageRepository
                                .findById(stage.getId())
                                .orElse(stage);
                        failed.setStageStatus(MigrationStageStatus.SKIPPED);
                        customerStageRepository.save(failed);
                    }
                }
            }

            if (!jobFailed) {
                migrationJobPersistence.updateJobStatus(jobId, MigrationJobStatus.COMPLETED, null, null);
                log.info("[ASYNC] Job {} completed", jobId);
            }

        } catch (Exception e) {
            log.error("[ASYNC] Job {} failed", jobId, e);
            migrationJobPersistence.updateJobStatus(
                    jobId,
                    MigrationJobStatus.FAILED,
                    ErrorCode.INTERNAL_SERVER_ERROR.getCode(),
                    e.getMessage() != null ? e.getMessage() : "Unknown error");
        } finally {
            MDC.remove("migration.job_id");
            MDC.remove("migration.customer_key");
        }
    }

    /**
     * Process a single stage in background.
     */
    @Async("migrationTaskExecutor")
    public void processStageAsync(String jobId, String customerKey, String stageCode) {
        MDC.put("migration.job_id", jobId);
        MDC.put("migration.customer_key", customerKey);
        try {
            migrationJobPersistence.updateJobStatus(jobId, MigrationJobStatus.IN_PROGRESS, null, null);

            Optional<CustomerMigrationStage> stageOpt =
                    customerStageRepository.findByCustomerKeyAndStageCode(customerKey, stageCode);

            if (stageOpt.isEmpty()) {
                migrationJobPersistence.updateJobStatus(
                        jobId, MigrationJobStatus.FAILED, "STAGE_NOT_FOUND", "Stage not found");
                return;
            }

            boolean success =
                    migrationStageExecution.processStage(stageOpt.get().getId());

            if (success) {
                migrationJobPersistence.updateJobStatus(jobId, MigrationJobStatus.COMPLETED, null, null);
            } else {
                migrationJobPersistence.updateJobStatus(
                        jobId, MigrationJobStatus.FAILED, "STAGE_FAILED", "Check stage logs");
            }

        } catch (Exception e) {
            migrationJobPersistence.updateJobStatus(
                    jobId,
                    MigrationJobStatus.FAILED,
                    ErrorCode.INTERNAL_SERVER_ERROR.getCode(),
                    e.getMessage() != null ? e.getMessage() : "Unknown error");
        } finally {
            MDC.remove("migration.job_id");
            MDC.remove("migration.customer_key");
        }
    }

    /**
     * Returns the next actionable stage (PENDING or FAILED) for the customer, ordered by
     * display order. Single query — replaces the previous two-call {@code .or()} chain.
     */
    private Optional<CustomerMigrationStage> findNextStageToProcess(String customerKey) {
        return customerStageRepository
                .findFirstByCustomerKeyAndStageStatusInOrderByStage_DisplayOrderAsc(
                        customerKey, ACTIONABLE_STATUSES);
    }
}
