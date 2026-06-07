package com.example.persona.migration.repository;

import com.example.persona.migration.enums.MigrationJobStatus;
import com.example.persona.migration.model.MigrationJob;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MigrationJobRepository extends JpaRepository<MigrationJob, Long> {

    Optional<MigrationJob> findByJobId(String jobId);

    List<MigrationJob> findByCustomerKeyOrderByCreatedDateDesc(String customerKey);

    @Query("SELECT j FROM MigrationJob j WHERE j.customerKey = :customerKey "
            + "AND j.jobStatus IN :statuses ORDER BY j.createdDate DESC")
    List<MigrationJob> findByCustomerKeyAndJobStatusIn(
            @Param("customerKey") String customerKey, @Param("statuses") List<MigrationJobStatus> statuses);

    boolean existsByCustomerKeyAndJobStatusIn(String customerKey, Collection<MigrationJobStatus> jobStatuses);

    Optional<MigrationJob> findFirstByCustomerKeyOrderByCreatedDateDesc(String customerKey);
}
