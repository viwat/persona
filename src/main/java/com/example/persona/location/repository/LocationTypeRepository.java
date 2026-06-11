package com.example.persona.location.repository;

import com.example.persona.enums.StatusType;
import com.example.persona.location.model.LocationType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LocationTypeRepository extends JpaRepository<LocationType, Long> {

    /**
     * Types with the given status, ordered for display, with tags eagerly
     * joined so mapping is safe outside an open session (open-in-view is disabled).
     */
    @Query("""
            SELECT DISTINCT t FROM LocationType t
            LEFT JOIN FETCH t.tags
            WHERE t.status = :status
            ORDER BY t.displayOrder ASC
            """)
    List<LocationType> findAllByStatusWithTags(@Param("status") StatusType status);

    Optional<LocationType> findByCode(String code);

    boolean existsByCode(String code);

    boolean existsByCodeAndStatus(String code, StatusType status);
}
