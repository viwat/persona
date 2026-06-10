package com.example.persona.migration.repository;

import com.example.persona.migration.enums.MigrationJobStatus;
import com.example.persona.migration.model.MigrationJob;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MigrationJobRepository extends JpaRepository<MigrationJob, Long> {

    Optional<MigrationJob> findByJobId(String jobId);

    List<MigrationJob> findByCustomerKeyOrderByCreatedDateDesc(String customerKey, Pageable pageable);

    @Query("SELECT j FROM MigrationJob j WHERE j.customerKey = :customerKey "
            + "AND j.jobStatus IN :statuses ORDER BY j.createdDate DESC")
    List<MigrationJob> findByCustomerKeyAndJobStatusIn(
            @Param("customerKey") String customerKey, @Param("statuses") List<MigrationJobStatus> statuses);

    boolean existsByCustomerKeyAndJobStatusIn(String customerKey, Collection<MigrationJobStatus> jobStatuses);

    Optional<MigrationJob> findFirstByCustomerKeyOrderByCreatedDateDesc(String customerKey);

    /**
     * Single-shot UPDATE for status fields — eliminates the SELECT + save() round-trip in
     * {@link com.example.persona.migration.service.MigrationJobPersistenceService}.
     */
    @Modifying
    @Query("""
            UPDATE MigrationJob j
            SET j.jobStatus   = :status,
                j.errorCode   = :errorCode,
                j.errorMessage = :errorMessage,
                j.completedAt  = :completedAt
            WHERE j.jobId = :jobId
            """)
    int updateStatusFields(
            @Param("jobId") String jobId,
            @Param("status") MigrationJobStatus status,
            @Param("errorCode") String errorCode,
            @Param("errorMessage") String errorMessage,
            @Param("completedAt") LocalDateTime completedAt);

    /**
     * Sets {@code started_at} only if it is currently {@code NULL} — safe to call
     * concurrently (idempotent conditional update).
     */
    @Modifying
    @Query("""
            UPDATE MigrationJob j
            SET j.startedAt = :startedAt
            WHERE j.jobId = :jobId AND j.startedAt IS NULL
            """)
    int setStartedAtIfNull(@Param("jobId") String jobId, @Param("startedAt") LocalDateTime startedAt);

    /**
     * Updates progress counters without touching any other fields.
     */
    @Modifying
    @Query("""
            UPDATE MigrationJob j
            SET j.completedStages = :completedStages,
                j.totalStages     = :totalStages
            WHERE j.jobId = :jobId
            """)
    int updateProgress(
            @Param("jobId") String jobId,
            @Param("completedStages") int completedStages,
            @Param("totalStages") int totalStages);
}
