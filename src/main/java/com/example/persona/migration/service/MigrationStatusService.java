package com.example.persona.migration.service;

import com.example.persona.migration.dto.CustomerMigrationStageResponse;
import com.example.persona.migration.dto.MigrationStatusResponse;
import com.example.persona.migration.enums.MigrationStageStatus;
import com.example.persona.migration.model.CustomerMigrationStage;
import com.example.persona.migration.repository.CustomerMigrationStageRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Read-side migration status: reconciles stage rows when needed and builds the
 * status response. Kept separate from {@link MigrationService} so transactional
 * boundaries are always entered via a Spring proxy (no self-invocation).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MigrationStatusService {

    private final CustomerStageReconciler customerStageReconciler;
    private final CustomerMigrationStageRepository customerStageRepository;

    /**
     * Check migration status for a customer. Returns current stage, whether at
     * final stage, and next pending stage if any.
     */
    @Transactional
    public MigrationStatusResponse checkMigrationStatus(String customerKey) {
        log.info("Checking migration status for customer: {}", customerKey);

        List<CustomerMigrationStage> customerStages =
                customerStageReconciler.ensureStagesReadyForStatusCheck(customerKey);

        long completedCount = customerStages.stream()
                .filter(cs -> cs.getStageStatus() == MigrationStageStatus.COMPLETED)
                .count();

        long totalStages = customerStageRepository.countTotalActiveStages();

        Optional<CustomerMigrationStage> completedFinalStage =
                customerStageRepository.findCompletedFinalStage(customerKey);
        // Migration is complete when: the final stage is COMPLETED AND every mandatory stage is COMPLETED.
        // Non-mandatory stages that are FAILED or SKIPPED do not block completion.
        boolean migrationCompleted = completedFinalStage.isPresent()
                && customerStages.stream()
                        .filter(cs -> Boolean.TRUE.equals(cs.getStage().getIsMandatory()))
                        .allMatch(cs -> cs.getStageStatus() == MigrationStageStatus.COMPLETED);

        CustomerMigrationStage currentStage = findCurrentStage(customerStages);
        CustomerMigrationStage nextPendingStage = findNextPendingStage(customerStages);

        boolean isFinalStage = currentStage != null
                && Boolean.TRUE.equals(currentStage.getStage().getIsFinal());

        double progressPercentage = totalStages > 0 ? (completedCount * 100.0) / totalStages : 0.0;

        List<CustomerMigrationStageResponse> allStagesResponse = customerStages.stream()
                .map(CustomerMigrationStageResponse::fromEntity)
                .toList();

        return MigrationStatusResponse.builder()
                .customerKey(customerKey)
                .migrationCompleted(migrationCompleted)
                .isFinalStage(isFinalStage)
                .completedStagesCount((int) completedCount)
                .totalStagesCount((int) totalStages)
                .progressPercentage(progressPercentage)
                .currentStage(currentStage != null ? CustomerMigrationStageResponse.fromEntity(currentStage) : null)
                .nextPendingStage(
                        nextPendingStage != null ? CustomerMigrationStageResponse.fromEntity(nextPendingStage) : null)
                .allStages(allStagesResponse)
                .build();
    }

    private CustomerMigrationStage findCurrentStage(List<CustomerMigrationStage> stages) {
        return stages.stream()
                .filter(s -> s.getStageStatus() != MigrationStageStatus.COMPLETED
                        && s.getStageStatus() != MigrationStageStatus.SKIPPED)
                .findFirst()
                .orElse(null);
    }

    private CustomerMigrationStage findNextPendingStage(List<CustomerMigrationStage> stages) {
        return stages.stream()
                .filter(s -> s.getStageStatus() == MigrationStageStatus.PENDING
                        || s.getStageStatus() == MigrationStageStatus.FAILED)
                .findFirst()
                .orElse(null);
    }
}
