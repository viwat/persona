package com.example.persona.location.repository;

import com.example.persona.enums.StatusType;
import com.example.persona.location.model.LocationCategory;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LocationCategoryRepository extends JpaRepository<LocationCategory, Long> {

    /**
     * Categories with the given status, ordered for display, with tags eagerly
     * joined so mapping is safe outside an open session (open-in-view is disabled).
     */
    @Query("""
            SELECT DISTINCT c FROM LocationCategory c
            LEFT JOIN FETCH c.tags
            WHERE c.status = :status
            ORDER BY c.displayOrder ASC
            """)
    List<LocationCategory> findAllByStatusWithTags(@Param("status") StatusType status);

    Optional<LocationCategory> findByCode(String code);

    boolean existsByCode(String code);

    boolean existsByCodeAndStatus(String code, StatusType status);
}
