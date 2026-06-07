package com.example.persona.migration.repository;

import com.example.persona.migration.enums.MigrationStageStatus;
import com.example.persona.migration.model.CustomerMigrationStage;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerMigrationStageRepository
        extends JpaRepository<@NonNull CustomerMigrationStage, @NonNull Long> {

    @Query("""
			SELECT cms FROM CustomerMigrationStage cms
			JOIN FETCH cms.stage st
			WHERE cms.customerKey = :customerKey
			ORDER BY st.displayOrder ASC
			""")
    List<CustomerMigrationStage> findByCustomerKeyOrderByStageDisplayOrderAsc(@Param("customerKey") String customerKey);

    @Query("""
			SELECT cms FROM CustomerMigrationStage cms
			JOIN FETCH cms.stage
			WHERE cms.customerKey = :customerKey
			AND cms.stage.code = :stageCode
			""")
    Optional<CustomerMigrationStage> findByCustomerKeyAndStageCode(
            @Param("customerKey") String customerKey, @Param("stageCode") String stageCode);

    Optional<CustomerMigrationStage> findByCustomerKeyAndStageId(String customerKey, Long stageId);

    @Query("""
			SELECT DISTINCT cms FROM CustomerMigrationStage cms
			JOIN FETCH cms.stage st
			WHERE cms.customerKey = :customerKey
			AND cms.stageStatus IN :statuses
			ORDER BY st.displayOrder ASC
			""")
    List<CustomerMigrationStage> findByCustomerKeyAndStageStatusIn(
            @Param("customerKey") String customerKey, @Param("statuses") List<MigrationStageStatus> statuses);

    /**
     * First row for the customer/status ordered by master stage display order. Uses Spring Data
     * {@code First} instead of JPQL {@code LIMIT 1} for portability, with an entity graph so
     * {@code stage} is initialized.
     */
    @EntityGraph(attributePaths = "stage")
    Optional<CustomerMigrationStage> findFirstByCustomerKeyAndStageStatusOrderByStage_DisplayOrderAsc(
            String customerKey, MigrationStageStatus stageStatus);

    /**
     * Combined PENDING-or-FAILED lookup — single query replacing the previous two-call
     * {@code .or()} chain in {@link com.example.persona.migration.service.MigrationBackgroundProcessor}.
     * Returns the first actionable stage ordered by display order.
     */
    @EntityGraph(attributePaths = "stage")
    Optional<CustomerMigrationStage> findFirstByCustomerKeyAndStageStatusInOrderByStage_DisplayOrderAsc(
            String customerKey, Collection<MigrationStageStatus> statuses);

    @Query("""
			SELECT cms FROM CustomerMigrationStage cms
			JOIN FETCH cms.stage
			WHERE cms.customerKey = :customerKey
			AND cms.stage.isFinal = true
			AND cms.stageStatus = 'COMPLETED'
			""")
    Optional<CustomerMigrationStage> findCompletedFinalStage(@Param("customerKey") String customerKey);

    @Query("""
			SELECT COUNT(cms) FROM CustomerMigrationStage cms
			WHERE cms.customerKey = :customerKey
			AND cms.stageStatus = 'COMPLETED'
			""")
    long countCompletedStages(@Param("customerKey") String customerKey);

    @Query("SELECT COUNT(s) FROM MigrationStage s WHERE s.status = 'ACTIVE'")
    long countTotalActiveStages();

    boolean existsByCustomerKeyAndStageStatus(String customerKey, MigrationStageStatus stageStatus);
}
