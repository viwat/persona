package com.example.persona.migration.service;

import com.example.persona.migration.dto.MigrationResult;
import com.example.persona.migration.dto.StageProcessResult;
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
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Executes a single migration stage inside its own transaction
 * ({@code REQUIRES_NEW}) so that a stage failure does not roll back the outer
 * job-tracking transaction.
 *
 * <p><strong>DB-access optimisation:</strong> the original implementation did
 * 3–4 {@code findById} calls per stage (one to read, one inside
 * {@code updateStageStatus}, one after setting IN_PROGRESS, one to save the
 * final result). This version uses one initial fetch and then works on the
 * managed entity throughout; {@code save()} returns the updated entity with the
 * refreshed {@code @Version} so no re-fetch is needed before the next write.
 */
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
        log.info("Registered {} migration stage handler(s): {}", stageHandlers.size(), stageHandlers.keySet());
    }

    /**
     * Process one stage. Returns a {@link StageProcessResult} carrying the success flag and
     * error details so callers do not need to re-fetch the stage entity.
     *
     * <p>The final status write uses a one-time retry on
     * {@link ObjectOptimisticLockingFailureException}: handler execution can take
     * several seconds (Oracle + CDP calls), during which a concurrent HTTP write
     * (e.g. manual status override) may bump {@code @Version}. On conflict the
     * entity is re-fetched and the same outcome is re-applied, preventing the
     * stage from getting stuck as {@code IN_PROGRESS}.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public StageProcessResult processStage(Long customerStageId) {
        // Single initial fetch — the entity is managed for the lifetime of this transaction.
        CustomerMigrationStage stage = customerStageRepository
                .findById(customerStageId)
                .orElseThrow(() -> new IllegalArgumentException("Stage row not found: " + customerStageId));

        String stageCode = stage.getStage().getCode();
        String customerKey = stage.getCustomerKey();

        log.info("Processing stage {} for customer: {}", stageCode, customerKey);

        MigrationStageHandler handler = stageHandlers.get(stageCode);
        if (handler == null) {
            log.error("No handler registered for stage code: {}", stageCode);
            stage.setStageStatus(MigrationStageStatus.FAILED);
            stage.setErrorCode("NO_HANDLER");
            stage.setErrorMessage("No handler registered for stage: " + stageCode);
            customerStageRepository.save(stage);
            return StageProcessResult.failure("NO_HANDLER", "No handler registered for stage: " + stageCode);
        }

        // Mark IN_PROGRESS — save() returns the entity with updated @Version.
        stage.setStageStatus(MigrationStageStatus.IN_PROGRESS);
        if (stage.getStartedAt() == null) {
            stage.setStartedAt(LocalDateTime.now());
        }
        stage = customerStageRepository.save(stage);

        // Execute handler (separated so OLE on the final save is distinguishable).
        MigrationResult result;
        try {
            result = handler.execute(stage);
        } catch (Exception e) {
            log.error("Unexpected exception in stage {}", stageCode, e);
            result = MigrationResult.failure("EXCEPTION", e.getMessage() != null ? e.getMessage() : "Unknown error");
        }

        applyResultToStage(stage, result);

        // Persist — retry once if a concurrent write (e.g. HTTP status override) caused an OLE.
        try {
            customerStageRepository.save(stage);
        } catch (ObjectOptimisticLockingFailureException ole) {
            log.warn(
                    "Optimistic lock conflict saving final status for stage {} — re-fetching and retrying",
                    stageCode,
                    ole);
            CustomerMigrationStage fresh = customerStageRepository
                    .findById(customerStageId)
                    .orElseThrow(
                            () -> new IllegalStateException("Stage row disappeared on OLE retry: " + customerStageId));
            applyResultToStage(fresh, result);
            customerStageRepository.save(fresh);
        }

        return result.success()
                ? StageProcessResult.ok()
                : StageProcessResult.failure(result.errorCode(), result.errorMessage());
    }

    /**
     * Applies a {@link MigrationResult} to the given stage entity (in-memory only; caller
     * is responsible for persisting).
     */
    private void applyResultToStage(CustomerMigrationStage stage, MigrationResult result) {
        if (result.success()) {
            stage.setStageStatus(MigrationStageStatus.COMPLETED);
            stage.setCompletedAt(LocalDateTime.now());
            stage.setErrorMessage(null);
            stage.setErrorCode(null);
            if (result.metadata() != null) {
                stage.setMetadata(result.metadata());
            }
        } else {
            stage.setStageStatus(MigrationStageStatus.FAILED);
            stage.setErrorCode(result.errorCode());
            stage.setErrorMessage(result.errorMessage());
            stage.setRetryCount(stage.getRetryCount() != null ? stage.getRetryCount() + 1 : 1);
        }
    }
}
