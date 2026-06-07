package com.example.persona.migration.repository;

import com.example.persona.migration.model.MigrationStage;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MigrationStageRepository extends JpaRepository<MigrationStage, Long> {

    Optional<MigrationStage> findByCode(String code);

    @Query("""
			SELECT s FROM MigrationStage s
			WHERE s.status = 'ACTIVE'
			ORDER BY s.displayOrder ASC
			""")
    List<MigrationStage> findAllActiveOrderByDisplayOrder();

    @Query("""
			SELECT s FROM MigrationStage s
			WHERE s.status = 'ACTIVE'
			AND s.isFinal = true
			""")
    Optional<MigrationStage> findFinalStage();

    @Query("""
			SELECT s FROM MigrationStage s
			WHERE s.status = 'ACTIVE'
			AND s.displayOrder = (SELECT MIN(s2.displayOrder) FROM MigrationStage s2 WHERE s2.status = 'ACTIVE')
			""")
    Optional<MigrationStage> findFirstStage();

    @Query("""
			SELECT s FROM MigrationStage s
			WHERE s.status = 'ACTIVE'
			AND s.displayOrder > :currentOrder
			ORDER BY s.displayOrder ASC LIMIT 1
			""")
    Optional<MigrationStage> findNextStage(Integer currentOrder);
}
