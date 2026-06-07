package com.example.persona.theme.repository;

import com.example.persona.theme.dto.response.ThemePreviewResponse;
import com.example.persona.theme.model.Theme;
import com.example.persona.theme.model.ThemeCategory;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ThemeRepository extends JpaRepository<Theme, Long> {

    @Query("""
			SELECT t FROM Theme t
			WHERE t.category = :category
			AND t.effectiveDate <= :currentDate
			AND t.expirationDate >= :currentDate
			AND (t.customerSegment IS NULL
			     OR t.customerSegment = :segment
			     OR :segment IS NULL)
			AND (t.customerSubSegment IS NULL
			     OR t.customerSubSegment = :subSegment
			     OR :subSegment IS NULL)
			ORDER BY t.displayOrder ASC
			""")
    List<Theme> findActiveThemes(
            @Param("category") ThemeCategory category,
            @Param("currentDate") LocalDate currentDate,
            @Param("segment") String segment,
            @Param("subSegment") String subSegment);

    @Query("""
			    SELECT new com.example.persona.theme.dto.response.ThemePreviewResponse(
			        t.id,
			        t.previewImageUrl,
			        t.animated
			    )
			    FROM Theme t
			    WHERE t.category.id IN :categoryIds
			      AND t.effectiveDate <= :currentDate
			      AND (t.expirationDate IS NULL OR t.expirationDate >= :currentDate)
			      AND (t.customerSegment IS NULL OR t.customerSegment = :segment OR :segment IS NULL)
			      AND (t.customerSubSegment IS NULL OR t.customerSubSegment = :subSegment OR :subSegment IS NULL)
			    ORDER BY t.displayOrder ASC
			""")
    List<ThemePreviewResponse> findActiveThemes(
            @Param("categoryIds") List<Long> categoryIds,
            @Param("currentDate") LocalDate currentDate,
            @Param("segment") String segment,
            @Param("subSegment") String subSegment);

    Optional<Theme> findByCode(String themeCode);

    @Query("""
			SELECT t FROM Theme t
			LEFT JOIN FETCH t.lightModeThemeId
			LEFT JOIN FETCH t.darkModeThemeId
			WHERE t.code = :themeCode
			""")
    Optional<Theme> findByCodeWithPackages(@Param("themeCode") String themeCode);
}
