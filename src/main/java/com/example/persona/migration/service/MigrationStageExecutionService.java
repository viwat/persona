package com.example.persona.migration.service;

import com.example.persona.migration.dto.MigrationResult;
import com.example.persona.migration.enums.MigrationStageStatus;
import com.example.persona.migration.handler.MigrationStageHandler;
import com.example.persona.migration.model.CustomerMigrationStage;
import com.example.persona.migration.repository.CustomerMigrationStageRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class MigrationStageExecutionService {

    private final CustomerMigrationStageRepository customerStageRepository;
    private final Map<String, MigrationStageHandler> stageHandlers;

    public MigrationStageExecutionService(
            CustomerMigrationStageRepository customerStageRepository, List<MigrationStageHandler> handlers) {
        this.customerStageRepository = customerStageRepository;
        this.stageHandlers =
                handlers.stream().collect(Collectors.toMap(MigrationStageHandler::getStageCode, Function.identity()));
        log.info("Initialized MigrationStageExecutionService with {} handlers", stageHandlers.size());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean processStage(Long customerStageId) {
        CustomerMigrationStage customerStage = customerStageRepository
                .findById(customerStageId)
                .orElseThrow(() -> new IllegalArgumentException("Stage ID not found: " + customerStageId));

        String stageCode = customerStage.getStage().getCode();
        String customerKey = customerStage.getCustomerKey();

        log.info("Processing stage {} for customer: {}", stageCode, customerKey);

        MigrationStageHandler handler = stageHandlers.get(stageCode);
        if (handler == null) {
            updateStageStatus(customerStageId, MigrationStageStatus.FAILED, "NO_HANDLER", "No handler registered");
            return false;
        }

        updateStageStatus(customerStageId, MigrationStageStatus.IN_PROGRESS, null, null);

        customerStage = customerStageRepository
                .findById(customerStageId)
                .orElseThrow(() -> new IllegalArgumentException("Stage ID not found: " + customerStageId));

        try {
            MigrationResult result = handler.execute(customerStage);

            if (result.success()) {
                CustomerMigrationStage latest = customerStageRepository
                        .findById(customerStageId)
                        .orElseThrow(() -> new IllegalArgumentException("Stage ID not found: " + customerStageId));
                latest.setStageStatus(MigrationStageStatus.COMPLETED);
                latest.setCompletedAt(LocalDateTime.now());
                latest.setErrorMessage(null);
                latest.setErrorCode(null);
                if (result.metadata() != null) {
                    latest.setMetadata(result.metadata());
                }
                customerStageRepository.save(latest);
                return true;
            }
            updateStageStatus(customerStageId, MigrationStageStatus.FAILED, result.errorCode(), result.errorMessage());
            return false;

        } catch (Exception e) {
            log.error("Exception during stage {}", stageCode, e);
            updateStageStatus(customerStageId, MigrationStageStatus.FAILED, "EXCEPTION", e.getMessage());
            return false;
        }
    }

    /**
     * Loads the row from the database before mutating so {@code @Version} on
     * {@link CustomerMigrationStage} is always current (avoids conflicts with
     * concurrent HTTP {@code updateStageStatus} or other writers).
     */
    private void updateStageStatus(
            Long customerStageId, MigrationStageStatus status, String errorCode, String errorMessage) {
        CustomerMigrationStage stage = customerStageRepository
                .findById(customerStageId)
                .orElseThrow(() -> new IllegalArgumentException("Stage ID not found: " + customerStageId));
        stage.setStageStatus(status);
        if (status == MigrationStageStatus.IN_PROGRESS && stage.getStartedAt() == null) {
            stage.setStartedAt(LocalDateTime.now());
        }
        if (status == MigrationStageStatus.FAILED) {
            stage.setErrorCode(errorCode);
            stage.setErrorMessage(errorMessage);
            stage.setRetryCount(stage.getRetryCount() != null ? stage.getRetryCount() + 1 : 1);
        }

        customerStageRepository.save(stage);
    }
}
