package com.example.persona.widget.repository;

import com.example.persona.enums.StatusType;
import com.example.persona.widget.model.Widget;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface WidgetRepository extends JpaRepository<Widget, Long> {

    @Query("""
			SELECT w FROM Widget w
			WHERE (:segment IS NULL OR w.customerSegment = :segment)
			AND (:subSegment IS NULL OR w.customerSubSegment = :subSegment)
			AND w.status = :status
			ORDER BY w.displayOrder
			""")
    List<Widget> findActiveWidgets(
            @Param("segment") String segment,
            @Param("subSegment") String subSegment,
            @Param("status") StatusType status);
}
