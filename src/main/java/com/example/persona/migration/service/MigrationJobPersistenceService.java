package com.example.persona.migration.service;

import com.example.persona.migration.enums.MigrationJobStatus;
import com.example.persona.migration.repository.MigrationJobRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MigrationJobPersistenceService {

    private final MigrationJobRepository jobRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateJobStatus(String jobId, MigrationJobStatus status, String errorCode, String errorMessage) {
        jobRepository.findByJobId(jobId).ifPresent(job -> {
            job.setJobStatus(status);
            if (status == MigrationJobStatus.IN_PROGRESS && job.getStartedAt() == null) {
                job.setStartedAt(LocalDateTime.now());
            }
            if (status == MigrationJobStatus.COMPLETED || status == MigrationJobStatus.FAILED) {
                job.setCompletedAt(LocalDateTime.now());
            }
            if (status == MigrationJobStatus.FAILED) {
                job.setErrorCode(errorCode);
                job.setErrorMessage(errorMessage);
            }
            jobRepository.save(job);
        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateJobProgress(String jobId, int completedStages, int totalStages) {
        jobRepository.findByJobId(jobId).ifPresent(job -> {
            job.setCompletedStages(completedStages);
            job.setTotalStages(totalStages);
            jobRepository.save(job);
        });
    }
}
