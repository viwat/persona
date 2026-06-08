package com.example.persona.migration.service;

import com.example.persona.migration.enums.MigrationJobStatus;
import com.example.persona.migration.repository.MigrationJobRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence operations for {@link com.example.persona.migration.model.MigrationJob}.
 *
 * <p>Each method issues a single targeted {@code UPDATE} via a {@code @Modifying} JPQL query,
 * eliminating the prior {@code findByJobId} SELECT + {@code save()} round-trip. The
 * {@code REQUIRES_NEW} propagation keeps these writes isolated from the caller's outer
 * transaction so a job-status update is never rolled back by a stage failure.
 */
@Service
@RequiredArgsConstructor
public class MigrationJobPersistenceService {

    private final MigrationJobRepository jobRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateJobStatus(String jobId, MigrationJobStatus status, String errorCode, String errorMessage) {
        LocalDateTime completedAt = (status == MigrationJobStatus.COMPLETED || status == MigrationJobStatus.FAILED)
                ? LocalDateTime.now() : null;
        // Only persist errorCode/errorMessage on FAILED; clear them otherwise.
        String resolvedCode = status == MigrationJobStatus.FAILED ? errorCode : null;
        String resolvedMsg  = status == MigrationJobStatus.FAILED ? errorMessage : null;

        jobRepository.updateStatusFields(jobId, status, resolvedCode, resolvedMsg, completedAt);

        if (status == MigrationJobStatus.IN_PROGRESS) {
            jobRepository.setStartedAtIfNull(jobId, LocalDateTime.now());
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateJobProgress(String jobId, int completedStages, int totalStages) {
        jobRepository.updateProgress(jobId, completedStages, totalStages);
    }
}
