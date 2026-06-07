package com.example.persona.migration.service;

import com.example.persona.enums.ErrorCode;
import com.example.persona.migration.enums.MigrationJobStatus;
import com.example.persona.migration.enums.MigrationStageStatus;
import com.example.persona.migration.model.CustomerMigrationStage;
import com.example.persona.migration.repository.CustomerMigrationStageRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Async migration jobs. Transactional persistence and per-stage work
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MigrationBackgroundProcessor {

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
                Optional<CustomerMigrationStage> nextStage = findNextStageToProcess(customerKey);

                if (nextStage.isEmpty()) {
                    continueProcessing = false;
                } else {
                    CustomerMigrationStage stage = nextStage.get();

                    boolean success = migrationStageExecution.processStage(stage.getId());

                    if (success) {
                        completedStages++;
                        migrationJobPersistence.updateJobProgress(jobId, completedStages, totalStages);
                    } else {
                        Optional<CustomerMigrationStage> failedStage = customerStageRepository.findById(stage.getId());
                        String errorCode = failedStage
                                .map(CustomerMigrationStage::getErrorCode)
                                .orElse("UNKNOWN");
                        String errorMsg = failedStage
                                .map(CustomerMigrationStage::getErrorMessage)
                                .orElse("Stage Failed");

                        log.warn("[ASYNC] Stage failed for job {}, stopping migration", jobId);
                        migrationJobPersistence.updateJobStatus(jobId, MigrationJobStatus.FAILED, errorCode, errorMsg);
                        jobFailed = true;
                        continueProcessing = false;
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

    private Optional<CustomerMigrationStage> findNextStageToProcess(String customerKey) {
        return customerStageRepository
                .findFirstByCustomerKeyAndStageStatusOrderByStage_DisplayOrderAsc(
                        customerKey, MigrationStageStatus.PENDING)
                .or(() -> customerStageRepository.findFirstByCustomerKeyAndStageStatusOrderByStage_DisplayOrderAsc(
                        customerKey, MigrationStageStatus.FAILED));
    }
}
