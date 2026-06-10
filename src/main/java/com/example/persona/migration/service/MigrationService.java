package com.example.persona.migration.service;

import com.example.persona.enums.ErrorCode;
import com.example.persona.enums.StatusType;
import com.example.persona.exception.BusinessException;
import com.example.persona.migration.dto.*;
import com.example.persona.migration.enums.MigrationJobStatus;
import com.example.persona.migration.enums.MigrationJobType;
import com.example.persona.migration.enums.MigrationStageStatus;
import com.example.persona.migration.model.CustomerMigrationStage;
import com.example.persona.migration.model.MigrationJob;
import com.example.persona.migration.repository.CustomerMigrationStageRepository;
import com.example.persona.migration.repository.MigrationJobRepository;
import com.example.persona.migration.repository.MigrationStageRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Migration service for tracking customer migration from Oracle to PostgreSQL.
 * Handles flexible stages with status tracking. Migration runs as a background
 * service with job tracking (UUID).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MigrationService {

    private static final List<MigrationJobStatus> QUEUED_OR_RUNNING_JOB_STATUSES =
            List.of(MigrationJobStatus.ACCEPTED, MigrationJobStatus.IN_PROGRESS);

    private static final int STAGE_UPDATE_OPTIMISTIC_RETRIES = 3;

    /**
     * If a job is {@code IN_PROGRESS} but no stage row is {@code IN_PROGRESS} for this long,
     * treat the job as abandoned (worker crash / lost thread) and allow a new submission.
     */
    private static final int ORPHAN_IN_PROGRESS_JOB_GRACE_MINUTES = 10;

    private final MigrationStageRepository stageRepository;
    private final CustomerMigrationStageRepository customerStageRepository;
    private final MigrationJobRepository jobRepository;
    private final MigrationBackgroundProcessor backgroundProcessor;
    private final MigrationStatusService migrationStatusService;
    private final TransactionTemplate transactionTemplate;

    /**
     * Jobs in {@code ACCEPTED} or {@code IN_PROGRESS} block new submissions. If a worker dies
     * or persistence fails, clear stale rows so {@code submitMigration} / {@code submitRetry} can
     * proceed. Override with {@code persona.migration.stale-blocking-job-minutes} (raise in prod
     * if a single stage can run longer than the default without completing).
     */
    @Value("${persona.migration.stale-blocking-job-minutes:30}")
    private int staleBlockingJobMinutes;

    /**
     * Check migration status for a customer. Delegates to {@link MigrationStatusService}
     * so the transactional boundary is not bypassed via self-invocation from
     * {@link #submitMigration} / {@link #submitRetry}.
     */
    public MigrationStatusResponse checkMigrationStatus(String customerKey) {
        return migrationStatusService.checkMigrationStatus(customerKey);
    }

    /**
     * Update a stage status for a customer.
     *
     * <p>Uses a short transaction per attempt and retries on optimistic lock
     * failures so concurrent updates (e.g. background migration) do not surface
     * as {@code StaleStateException} / unexpected row count {@code 0}.
     *
     * @param request
     *            the update request
     * @return updated CustomerMigrationStageResponse
     */
    public CustomerMigrationStageResponse updateStageStatus(UpdateStageRequest request) {
        OptimisticLockingFailureException lastConflict = null;
        for (int attempt = 1; attempt <= STAGE_UPDATE_OPTIMISTIC_RETRIES; attempt++) {
            try {
                return transactionTemplate.execute(status -> updateStageStatusInTransaction(request));
            } catch (OptimisticLockingFailureException e) {
                lastConflict = e;
                log.warn(
                        "Optimistic lock updating stage {} for customer {} (attempt {}/{})",
                        request.getStageCode(),
                        request.getCustomerKey(),
                        attempt,
                        STAGE_UPDATE_OPTIMISTIC_RETRIES);
            }
        }
        throw Objects.requireNonNull(lastConflict);
    }

    private CustomerMigrationStageResponse updateStageStatusInTransaction(UpdateStageRequest request) {
        log.info(
                "Updating stage {} to status {} for customer: {}",
                request.getStageCode(),
                request.getStageStatus(),
                request.getCustomerKey());

        CustomerMigrationStage customerStage = customerStageRepository
                .findByCustomerKeyAndStageCode(request.getCustomerKey(), request.getStageCode())
                .orElseThrow(() -> new BusinessException(
                        "Stage not found for customer: " + request.getCustomerKey() + ", stage: "
                                + request.getStageCode(),
                        ErrorCode.RESOURCE_NOT_FOUND));

        MigrationStageStatus oldStatus = customerStage.getStageStatus();
        customerStage.setStageStatus(request.getStageStatus());

        switch (request.getStageStatus()) {
            case IN_PROGRESS:
                if (customerStage.getStartedAt() == null) {
                    customerStage.setStartedAt(LocalDateTime.now());
                }
                break;
            case COMPLETED:
                customerStage.setCompletedAt(LocalDateTime.now());
                customerStage.setErrorMessage(null);
                customerStage.setErrorCode(null);
                break;
            case FAILED:
                customerStage.setErrorMessage(request.getErrorMessage());
                customerStage.setErrorCode(request.getErrorCode());
                customerStage.setRetryCount(
                        customerStage.getRetryCount() != null ? customerStage.getRetryCount() + 1 : 1);
                break;
            default:
                break;
        }

        if (request.getMetadata() != null) {
            customerStage.setMetadata(request.getMetadata());
        }

        CustomerMigrationStage saved = customerStageRepository.save(customerStage);
        log.info(
                "Updated stage {} from {} to {} for customer: {}",
                request.getStageCode(),
                oldStatus,
                request.getStageStatus(),
                request.getCustomerKey());

        return CustomerMigrationStageResponse.fromEntity(saved);
    }

    /**
     * Submit migration job for a customer. Returns immediately with job_id (UUID)
     * for tracking. Migration runs asynchronously in background.
     *
     * @param customerKey
     *            the customer key
     * @return MigrationJobResponse with job_id and ACCEPTED status
     */
    @Transactional
    public MigrationJobResponse submitMigration(String customerKey) {
        log.info("Submitting migration job for customer: {}", customerKey);

        // Check current status
        MigrationStatusResponse status = migrationStatusService.checkMigrationStatus(customerKey);

        if (status.isMigrationCompleted()) {
            reconcileOpenJobsAfterMigrationFinished(customerKey);
            log.info("Migration already completed for customer: {}", customerKey);
            return MigrationJobResponse.alreadyCompleted(customerKey);
        }

        releaseStaleBlockingJobsIfAny(customerKey);

        if (jobRepository.existsByCustomerKeyAndJobStatusIn(customerKey, QUEUED_OR_RUNNING_JOB_STATUSES)) {
            Optional<MigrationJob> existingJob =
                    jobRepository.findFirstByCustomerKeyOrderByCreatedDateDesc(customerKey);
            if (existingJob.isPresent()) {
                log.info(
                        "Migration job already queued or running for customer: {}, job: {} ({})",
                        customerKey,
                        existingJob.get().getJobId(),
                        existingJob.get().getJobStatus());
                return MigrationJobResponse.fromEntity(existingJob.get());
            }
        }

        // Create new job
        MigrationJob job = createJob(customerKey, MigrationJobType.FULL_MIGRATION, null);

        // Trigger background processing
        backgroundProcessor.processMigrationAsync(job.getJobId(), customerKey);

        log.info("Migration job {} accepted for customer: {}", job.getJobId(), customerKey);
        return MigrationJobResponse.accepted(job.getJobId(), customerKey, MigrationJobType.FULL_MIGRATION);
    }

    /**
     * Submit a specific stage for background processing. Returns immediately with
     * job_id (UUID) for tracking.
     *
     * @param customerKey
     *            the customer key
     * @param stageCode
     *            the stage code to process
     * @return MigrationJobResponse with job_id and ACCEPTED status
     */
    @Transactional
    public MigrationJobResponse submitStage(String customerKey, String stageCode) {
        log.info("Submitting stage {} job for customer: {}", stageCode, customerKey);

        // Validate stage exists
        customerStageRepository
                .findByCustomerKeyAndStageCode(customerKey, stageCode)
                .orElseThrow(() -> new BusinessException(
                        "Stage not found for customer: " + customerKey + ", stage: " + stageCode,
                        ErrorCode.RESOURCE_NOT_FOUND));

        releaseStaleBlockingJobsIfAny(customerKey);

        if (jobRepository.existsByCustomerKeyAndJobStatusIn(customerKey, QUEUED_OR_RUNNING_JOB_STATUSES)) {
            Optional<MigrationJob> existingJob =
                    jobRepository.findFirstByCustomerKeyOrderByCreatedDateDesc(customerKey);
            if (existingJob.isPresent()) {
                log.info(
                        "Stage job not created: job already queued or running for customer: {}, job: {}",
                        customerKey,
                        existingJob.get().getJobId());
                return MigrationJobResponse.fromEntity(existingJob.get());
            }
        }

        // Create new job
        MigrationJob job = createJob(customerKey, MigrationJobType.SINGLE_STAGE, stageCode);

        // Trigger background processing for specific stage
        backgroundProcessor.processStageAsync(job.getJobId(), customerKey, stageCode);

        log.info("Stage job {} accepted for customer: {}, stage: {}", job.getJobId(), customerKey, stageCode);
        return MigrationJobResponse.accepted(job.getJobId(), customerKey, MigrationJobType.SINGLE_STAGE);
    }

    /**
     * Submit retry job for failed stages. Returns immediately with job_id (UUID)
     * for tracking.
     *
     * @param customerKey
     *            the customer key
     * @return MigrationJobResponse with job_id and ACCEPTED status
     */
    @Transactional
    public MigrationJobResponse submitRetry(String customerKey) {
        log.info("Submitting retry job for customer: {}", customerKey);

        MigrationStatusResponse status = migrationStatusService.checkMigrationStatus(customerKey);

        // Check if there are any failed stages
        boolean hasFailedStages =
                status.getAllStages().stream().anyMatch(s -> s.getStageStatus() == MigrationStageStatus.FAILED);

        if (!hasFailedStages) {
            throw new BusinessException(
                    "No failed stages to retry for customer: " + customerKey, ErrorCode.BUSINESS_ERROR);
        }

        releaseStaleBlockingJobsIfAny(customerKey);

        if (jobRepository.existsByCustomerKeyAndJobStatusIn(customerKey, QUEUED_OR_RUNNING_JOB_STATUSES)) {
            Optional<MigrationJob> existingJob =
                    jobRepository.findFirstByCustomerKeyOrderByCreatedDateDesc(customerKey);
            if (existingJob.isPresent()) {
                log.info(
                        "Retry job not created: job already queued or running for customer: {}, job: {}",
                        customerKey,
                        existingJob.get().getJobId());
                return MigrationJobResponse.fromEntity(existingJob.get());
            }
        }

        // Create new job
        MigrationJob job = createJob(customerKey, MigrationJobType.RETRY, null);

        // Trigger background processing (will pick up failed stages)
        backgroundProcessor.processMigrationAsync(job.getJobId(), customerKey);

        log.info("Retry job {} accepted for customer: {}", job.getJobId(), customerKey);
        return MigrationJobResponse.accepted(job.getJobId(), customerKey, MigrationJobType.RETRY);
    }

    /**
     * Get job status by job_id.
     *
     * @param jobId
     *            the job UUID
     * @return MigrationJobResponse with current status
     */
    public MigrationJobResponse getJobStatus(String jobId) {
        MigrationJob job = jobRepository
                .findByJobId(jobId)
                .orElseThrow(() -> new BusinessException("Job not found: " + jobId, ErrorCode.RESOURCE_NOT_FOUND));

        return MigrationJobResponse.fromEntity(job);
    }

    /**
     * Get all jobs for a customer.
     *
     * @param customerKey
     *            the customer key
     * @return list of jobs
     */
    public List<MigrationJobResponse> getCustomerJobs(String customerKey) {
        return jobRepository.findByCustomerKeyOrderByCreatedDateDesc(customerKey, PageRequest.of(0, 50)).stream()
                .map(MigrationJobResponse::fromEntity)
                .toList();
    }

    /**
     * If all customer stages are already migrated but job rows were never finalized (e.g. worker
     * lost after the last stage completed), close those {@code ACCEPTED}/{@code IN_PROGRESS} jobs
     * as {@code COMPLETED} so {@code dgtl_migration_job} matches stage data.
     */
    private void reconcileOpenJobsAfterMigrationFinished(String customerKey) {
        List<MigrationJob> active =
                jobRepository.findByCustomerKeyAndJobStatusIn(customerKey, QUEUED_OR_RUNNING_JOB_STATUSES);
        if (active.isEmpty()) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        for (MigrationJob job : active) {
            log.warn(
                    "Reconciling migration job {} for customer {} from {} to COMPLETED — stages already fully migrated",
                    job.getJobId(),
                    customerKey,
                    job.getJobStatus());
            job.setJobStatus(MigrationJobStatus.COMPLETED);
            job.setCompletedAt(now);
            job.setErrorCode(null);
            job.setErrorMessage(null);
            jobRepository.save(job);
        }
    }

    /**
     * Marks blocking {@code ACCEPTED}/{@code IN_PROGRESS} jobs as {@code FAILED} when they are
     * clearly abandoned, so callers are not blocked forever.
     */
    private void releaseStaleBlockingJobsIfAny(String customerKey) {
        List<MigrationJob> active =
                jobRepository.findByCustomerKeyAndJobStatusIn(customerKey, QUEUED_OR_RUNNING_JOB_STATUSES);
        if (active.isEmpty()) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime timeCutoff = now.minusMinutes(staleBlockingJobMinutes);
        LocalDateTime orphanCutoff = now.minusMinutes(ORPHAN_IN_PROGRESS_JOB_GRACE_MINUTES);

        for (MigrationJob job : active) {
            if (!shouldReleaseStaleJob(job, customerKey, timeCutoff, orphanCutoff)) {
                continue;
            }
            MigrationJobStatus previousStatus = job.getJobStatus();
            log.warn(
                    "Releasing stale migration job {} for customer {} (was {}, error STALE_JOB)",
                    job.getJobId(),
                    customerKey,
                    previousStatus);
            job.setJobStatus(MigrationJobStatus.FAILED);
            job.setCompletedAt(now);
            job.setErrorCode("STALE_JOB");
            job.setErrorMessage("Job was stuck in "
                    + previousStatus
                    + " with no active worker progress; marked failed so a new job can run. "
                    + "Tune persona.migration.stale-blocking-job-minutes if needed.");
            jobRepository.save(job);
        }
    }

    private boolean shouldReleaseStaleJob(
            MigrationJob job, String customerKey, LocalDateTime timeCutoff, LocalDateTime orphanCutoff) {

        LocalDateTime ref = staleJobReferenceTime(job);
        if (ref != null && ref.isBefore(timeCutoff)) {
            return true;
        }

        if (job.getJobStatus() != MigrationJobStatus.IN_PROGRESS) {
            return false;
        }
        if (job.getStartedAt() == null || !job.getStartedAt().isBefore(orphanCutoff)) {
            return false;
        }
        return !customerStageRepository.existsByCustomerKeyAndStageStatus(
                customerKey, MigrationStageStatus.IN_PROGRESS);
    }

    private static LocalDateTime staleJobReferenceTime(MigrationJob job) {
        if (job.getJobStatus() == MigrationJobStatus.IN_PROGRESS && job.getStartedAt() != null) {
            return job.getStartedAt();
        }
        return job.getCreatedDate();
    }

    /**
     * Create a new migration job.
     */
    private MigrationJob createJob(String customerKey, MigrationJobType jobType, String stageCode) {
        int totalStages = (int) customerStageRepository.countTotalActiveStages();
        String typeSegment = jobType != null ? jobType.name() : "";
        String jobName =
                stageCode != null ? customerKey + "-" + typeSegment + "-" + stageCode : customerKey + "-" + typeSegment;
        MigrationJob job = MigrationJob.builder()
                .jobId(MigrationJob.generateJobId())
                .jobName(jobName)
                .customerKey(customerKey)
                .jobType(jobType)
                .stageCode(stageCode)
                .jobStatus(MigrationJobStatus.ACCEPTED)
                .totalStages(totalStages)
                .completedStages(0)
                .status(StatusType.ACTIVE)
                .build();

        return jobRepository.save(job);
    }

    /**
     * Get all available migration stages.
     *
     * @return list of all active stages
     */
    public List<MigrationStageResponse> getAllStages() {
        return stageRepository.findAllActiveOrderByDisplayOrder().stream()
                .map(MigrationStageResponse::fromEntity)
                .toList();
    }
}
