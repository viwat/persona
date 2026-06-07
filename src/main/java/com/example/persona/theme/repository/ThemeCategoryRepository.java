package com.example.persona.theme.repository;

import com.example.persona.enums.StatusType;
import com.example.persona.theme.model.ThemeCategory;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ThemeCategoryRepository extends JpaRepository<ThemeCategory, Long> {

    @Query("""
			SELECT tc FROM ThemeCategory tc
			WHERE tc.effectiveDate <= :currentDate
			AND tc.expirationDate >= :currentDate
			AND (tc.customerSegment IS NULL
			     OR tc.customerSegment = :segment
			     OR :segment IS NULL)
			AND (tc.customerSubSegment IS NULL
			     OR tc.customerSubSegment = :subSegment
			     OR :subSegment IS NULL)
			ORDER BY tc.displayOrder ASC
			""")
    List<ThemeCategory> findActiveCategories(
            @Param("currentDate") LocalDate currentDate,
            @Param("segment") String segment,
            @Param("subSegment") String subSegment);

    Optional<ThemeCategory> findByCode(String code);

    Optional<ThemeCategory> findByIdAndStatus(Long id, StatusType statusType);
}
