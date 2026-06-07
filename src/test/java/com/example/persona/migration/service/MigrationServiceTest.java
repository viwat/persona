package com.example.persona.migration.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.persona.enums.StatusType;
import com.example.persona.migration.dto.CustomerMigrationStageResponse;
import com.example.persona.migration.dto.MigrationJobResponse;
import com.example.persona.migration.dto.MigrationStatusResponse;
import com.example.persona.migration.dto.UpdateStageRequest;
import com.example.persona.migration.enums.MigrationJobStatus;
import com.example.persona.migration.enums.MigrationJobType;
import com.example.persona.migration.enums.MigrationStageStatus;
import com.example.persona.migration.model.CustomerMigrationStage;
import com.example.persona.migration.model.MigrationJob;
import com.example.persona.migration.model.MigrationStage;
import com.example.persona.migration.repository.CustomerMigrationStageRepository;
import com.example.persona.migration.repository.MigrationJobRepository;
import com.example.persona.migration.repository.MigrationStageRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.SimpleTransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

@ExtendWith(MockitoExtension.class)
@DisplayName("MigrationService")
class MigrationServiceTest {

    private static final String CUSTOMER_KEY = "cust-001";
    private static final List<MigrationJobStatus> ACTIVE_STATUSES =
            List.of(MigrationJobStatus.ACCEPTED, MigrationJobStatus.IN_PROGRESS);

    @Mock
    private MigrationStageRepository stageRepository;

    @Mock
    private CustomerMigrationStageRepository customerStageRepository;

    @Mock
    private MigrationJobRepository jobRepository;

    @Mock
    private MigrationBackgroundProcessor backgroundProcessor;

    @Mock
    private MigrationStatusService migrationStatusService;

    @Mock
    private TransactionTemplate transactionTemplate;

    @InjectMocks
    private MigrationService migrationService;

    @BeforeEach
    void setStaleThreshold() {
        ReflectionTestUtils.setField(migrationService, "staleBlockingJobMinutes", 30);
    }

    @Nested
    @DisplayName("Stale blocking job release")
    class StaleBlockingJobs {

        @Test
        @DisplayName("submitMigration marks old ACCEPTED job STALE_JOB then accepts a new job")
        void submitMigration_releasesStaleAcceptedAndQueuesNew() {
            LocalDateTime now = LocalDateTime.now();
            MigrationJob stale = MigrationJob.builder()
                    .jobId("old-uuid")
                    .customerKey(CUSTOMER_KEY)
                    .jobStatus(MigrationJobStatus.ACCEPTED)
                    .jobType(MigrationJobType.FULL_MIGRATION)
                    .createdDate(now.minusMinutes(45))
                    .build();

            when(migrationStatusService.checkMigrationStatus(CUSTOMER_KEY))
                    .thenReturn(MigrationStatusResponse.builder()
                            .customerKey(CUSTOMER_KEY)
                            .migrationCompleted(false)
                            .allStages(List.of())
                            .build());
            when(jobRepository.findByCustomerKeyAndJobStatusIn(CUSTOMER_KEY, ACTIVE_STATUSES))
                    .thenReturn(List.of(stale));
            when(jobRepository.existsByCustomerKeyAndJobStatusIn(CUSTOMER_KEY, ACTIVE_STATUSES))
                    .thenReturn(false);
            when(customerStageRepository.countTotalActiveStages()).thenReturn(5L);

            MigrationJob newJob = MigrationJob.builder()
                    .jobId("new-uuid")
                    .customerKey(CUSTOMER_KEY)
                    .jobStatus(MigrationJobStatus.ACCEPTED)
                    .jobType(MigrationJobType.FULL_MIGRATION)
                    .totalStages(5)
                    .completedStages(0)
                    .status(StatusType.ACTIVE)
                    .build();
            when(jobRepository.save(any(MigrationJob.class))).thenReturn(newJob);

            MigrationJobResponse response = migrationService.submitMigration(CUSTOMER_KEY);

            assertThat(response.getJobId()).isEqualTo("new-uuid");
            assertThat(response.getJobStatus()).isEqualTo(MigrationJobStatus.ACCEPTED);
            verify(backgroundProcessor).processMigrationAsync("new-uuid", CUSTOMER_KEY);

            ArgumentCaptor<MigrationJob> savedCaptor = ArgumentCaptor.forClass(MigrationJob.class);
            verify(jobRepository, times(2)).save(savedCaptor.capture());
            MigrationJob firstSave = savedCaptor.getAllValues().get(0);
            assertThat(firstSave.getJobStatus()).isEqualTo(MigrationJobStatus.FAILED);
            assertThat(firstSave.getErrorCode()).isEqualTo("STALE_JOB");
        }

        @Test
        @DisplayName("submitMigration returns existing job when active job is still fresh")
        void submitMigration_blocksWhenFreshActiveJobExists() {
            LocalDateTime now = LocalDateTime.now();
            MigrationJob fresh = MigrationJob.builder()
                    .jobId("running-uuid")
                    .customerKey(CUSTOMER_KEY)
                    .jobStatus(MigrationJobStatus.IN_PROGRESS)
                    .jobType(MigrationJobType.FULL_MIGRATION)
                    .createdDate(now.minusMinutes(2))
                    .startedAt(now.minusMinutes(2))
                    .build();

            when(migrationStatusService.checkMigrationStatus(CUSTOMER_KEY))
                    .thenReturn(MigrationStatusResponse.builder()
                            .customerKey(CUSTOMER_KEY)
                            .migrationCompleted(false)
                            .allStages(List.of())
                            .build());
            when(jobRepository.findByCustomerKeyAndJobStatusIn(CUSTOMER_KEY, ACTIVE_STATUSES))
                    .thenReturn(List.of(fresh));
            when(jobRepository.existsByCustomerKeyAndJobStatusIn(CUSTOMER_KEY, ACTIVE_STATUSES))
                    .thenReturn(true);
            when(jobRepository.findFirstByCustomerKeyOrderByCreatedDateDesc(CUSTOMER_KEY))
                    .thenReturn(java.util.Optional.of(fresh));

            MigrationJobResponse response = migrationService.submitMigration(CUSTOMER_KEY);

            assertThat(response.getJobId()).isEqualTo("running-uuid");
            assertThat(response.getJobStatus()).isEqualTo(MigrationJobStatus.IN_PROGRESS);
            verify(jobRepository, never()).save(any(MigrationJob.class));
            verify(backgroundProcessor, never()).processMigrationAsync(anyString(), anyString());
        }

        @Test
        @DisplayName("submitMigration releases orphan IN_PROGRESS job when no stage is IN_PROGRESS")
        void submitMigration_releasesOrphanInProgressJob() {
            LocalDateTime now = LocalDateTime.now();
            MigrationJob orphan = MigrationJob.builder()
                    .jobId("orphan-uuid")
                    .customerKey(CUSTOMER_KEY)
                    .jobStatus(MigrationJobStatus.IN_PROGRESS)
                    .jobType(MigrationJobType.FULL_MIGRATION)
                    .createdDate(now.minusMinutes(15))
                    .startedAt(now.minusMinutes(15))
                    .build();

            when(migrationStatusService.checkMigrationStatus(CUSTOMER_KEY))
                    .thenReturn(MigrationStatusResponse.builder()
                            .customerKey(CUSTOMER_KEY)
                            .migrationCompleted(false)
                            .allStages(List.of())
                            .build());
            when(jobRepository.findByCustomerKeyAndJobStatusIn(CUSTOMER_KEY, ACTIVE_STATUSES))
                    .thenReturn(List.of(orphan));
            when(customerStageRepository.existsByCustomerKeyAndStageStatus(
                            CUSTOMER_KEY, MigrationStageStatus.IN_PROGRESS))
                    .thenReturn(false);
            when(jobRepository.existsByCustomerKeyAndJobStatusIn(CUSTOMER_KEY, ACTIVE_STATUSES))
                    .thenReturn(false);
            when(customerStageRepository.countTotalActiveStages()).thenReturn(3L);

            MigrationJob newJob = MigrationJob.builder()
                    .jobId("recovery-uuid")
                    .customerKey(CUSTOMER_KEY)
                    .jobStatus(MigrationJobStatus.ACCEPTED)
                    .jobType(MigrationJobType.FULL_MIGRATION)
                    .totalStages(3)
                    .completedStages(0)
                    .status(StatusType.ACTIVE)
                    .build();
            when(jobRepository.save(any(MigrationJob.class))).thenReturn(newJob);

            MigrationJobResponse response = migrationService.submitMigration(CUSTOMER_KEY);

            assertThat(response.getJobId()).isEqualTo("recovery-uuid");
            verify(backgroundProcessor).processMigrationAsync("recovery-uuid", CUSTOMER_KEY);
            ArgumentCaptor<MigrationJob> captor = ArgumentCaptor.forClass(MigrationJob.class);
            verify(jobRepository, times(2)).save(captor.capture());
            assertThat(captor.getAllValues().get(0).getErrorCode()).isEqualTo("STALE_JOB");
        }

        @Test
        @DisplayName("submitRetry releases stale job then accepts retry when failed stages exist")
        void submitRetry_releasesStaleThenAccepts() {
            LocalDateTime now = LocalDateTime.now();
            MigrationJob stale = MigrationJob.builder()
                    .jobId("stuck-retry")
                    .customerKey(CUSTOMER_KEY)
                    .jobStatus(MigrationJobStatus.IN_PROGRESS)
                    .jobType(MigrationJobType.RETRY)
                    .createdDate(now.minusHours(2))
                    .startedAt(now.minusHours(2))
                    .build();

            when(migrationStatusService.checkMigrationStatus(CUSTOMER_KEY))
                    .thenReturn(MigrationStatusResponse.builder()
                            .customerKey(CUSTOMER_KEY)
                            .migrationCompleted(false)
                            .allStages(List.of(CustomerMigrationStageResponse.builder()
                                    .stageStatus(MigrationStageStatus.FAILED)
                                    .build()))
                            .build());
            when(jobRepository.findByCustomerKeyAndJobStatusIn(CUSTOMER_KEY, ACTIVE_STATUSES))
                    .thenReturn(List.of(stale));
            when(jobRepository.existsByCustomerKeyAndJobStatusIn(CUSTOMER_KEY, ACTIVE_STATUSES))
                    .thenReturn(false);
            when(customerStageRepository.countTotalActiveStages()).thenReturn(4L);

            MigrationJob newJob = MigrationJob.builder()
                    .jobId("retry-new")
                    .customerKey(CUSTOMER_KEY)
                    .jobStatus(MigrationJobStatus.ACCEPTED)
                    .jobType(MigrationJobType.RETRY)
                    .totalStages(4)
                    .completedStages(0)
                    .status(StatusType.ACTIVE)
                    .build();
            when(jobRepository.save(any(MigrationJob.class))).thenReturn(newJob);

            MigrationJobResponse response = migrationService.submitRetry(CUSTOMER_KEY);

            assertThat(response.getJobType()).isEqualTo(MigrationJobType.RETRY.name());
            verify(backgroundProcessor).processMigrationAsync("retry-new", CUSTOMER_KEY);
        }

        @Test
        @DisplayName("submitMigration sets open jobs to COMPLETED when stages already fully migrated")
        void submitMigration_reconcilesOpenJobsWhenAlreadyMigrated() {
            MigrationJob stuckInProgress = MigrationJob.builder()
                    .jobId("stuck-uuid")
                    .customerKey(CUSTOMER_KEY)
                    .jobStatus(MigrationJobStatus.IN_PROGRESS)
                    .jobType(MigrationJobType.FULL_MIGRATION)
                    .build();

            when(migrationStatusService.checkMigrationStatus(CUSTOMER_KEY))
                    .thenReturn(MigrationStatusResponse.builder()
                            .customerKey(CUSTOMER_KEY)
                            .migrationCompleted(true)
                            .allStages(List.of())
                            .build());
            when(jobRepository.findByCustomerKeyAndJobStatusIn(CUSTOMER_KEY, ACTIVE_STATUSES))
                    .thenReturn(List.of(stuckInProgress));

            MigrationJobResponse response = migrationService.submitMigration(CUSTOMER_KEY);

            assertThat(response.getJobStatus()).isEqualTo(MigrationJobStatus.COMPLETED);
            assertThat(response.getMessage()).contains("already completed");
            verify(jobRepository).save(stuckInProgress);
            assertThat(stuckInProgress.getJobStatus()).isEqualTo(MigrationJobStatus.COMPLETED);
            assertThat(stuckInProgress.getCompletedAt()).isNotNull();
            verify(backgroundProcessor, never()).processMigrationAsync(anyString(), anyString());
        }
    }

    @Nested
    @DisplayName("updateStageStatus optimistic locking")
    class UpdateStageStatusRetries {

        @Test
        @DisplayName("retries transaction on OptimisticLockingFailureException then succeeds")
        void updateStageStatus_retriesThenSucceeds() {
            MigrationStage stage = new MigrationStage();
            stage.setId(1L);
            stage.setCode("ACC");
            stage.setName("Account");
            stage.setDisplayOrder(1);
            stage.setIsFinal(false);
            stage.setIsMandatory(true);

            CustomerMigrationStage entity = new CustomerMigrationStage();
            entity.setId(99L);
            entity.setCustomerKey(CUSTOMER_KEY);
            entity.setStage(stage);
            entity.setStageStatus(MigrationStageStatus.PENDING);

            UpdateStageRequest request = UpdateStageRequest.builder()
                    .customerKey(CUSTOMER_KEY)
                    .stageCode("ACC")
                    .stageStatus(MigrationStageStatus.IN_PROGRESS)
                    .build();

            when(customerStageRepository.findByCustomerKeyAndStageCode(CUSTOMER_KEY, "ACC"))
                    .thenReturn(java.util.Optional.of(entity));
            when(customerStageRepository.save(any(CustomerMigrationStage.class)))
                    .thenReturn(entity);

            when(transactionTemplate.execute(any()))
                    .thenThrow(new ObjectOptimisticLockingFailureException(CustomerMigrationStage.class, 99L))
                    .thenAnswer(inv -> {
                        @SuppressWarnings("unchecked")
                        TransactionCallback<CustomerMigrationStageResponse> cb =
                                inv.getArgument(0, TransactionCallback.class);
                        return cb.doInTransaction(new SimpleTransactionStatus());
                    });

            CustomerMigrationStageResponse response = migrationService.updateStageStatus(request);

            assertThat(response.getStageStatus()).isEqualTo(MigrationStageStatus.IN_PROGRESS);
            verify(transactionTemplate, times(2)).execute(any());
            verify(customerStageRepository, times(1)).findByCustomerKeyAndStageCode(CUSTOMER_KEY, "ACC");
        }

        @Test
        @DisplayName("throws after exhausting retries on persistent optimistic lock failures")
        void updateStageStatus_throwsAfterMaxRetries() {
            UpdateStageRequest request = UpdateStageRequest.builder()
                    .customerKey(CUSTOMER_KEY)
                    .stageCode("X")
                    .stageStatus(MigrationStageStatus.COMPLETED)
                    .build();

            ObjectOptimisticLockingFailureException ole =
                    new ObjectOptimisticLockingFailureException(CustomerMigrationStage.class, 1L);
            when(transactionTemplate.execute(any())).thenThrow(ole);

            assertThatThrownBy(() -> migrationService.updateStageStatus(request))
                    .isInstanceOf(OptimisticLockingFailureException.class);

            verify(transactionTemplate, times(3)).execute(any());
        }
    }
}
