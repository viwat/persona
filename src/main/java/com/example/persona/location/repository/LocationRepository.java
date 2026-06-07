package com.example.persona.location.repository;

import com.example.persona.location.model.Location;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {

    @Query(value = """
			SELECT l.*
			    FROM dgtl_location l
			    JOIN dgtl_location_type lt\s
			        ON lt.id = l.location_type_id
			    WHERE (:agentType = 'ALL' OR lt.name_en LIKE :agentType)
			      AND l.status = 'ACTIVE' AND lt.status = 'ACTIVE'
			      AND l.latitude BETWEEN :minLat AND :maxLat
			      AND l.longitude BETWEEN :minLng AND :maxLng
			      AND (
			          2 * :earthRadius * ASIN(
			              SQRT(
			                  POWER(SIN(RADIANS(:lat - l.latitude) / 2), 2) +
			                  COS(RADIANS(:lat)) * COS(RADIANS(l.latitude)) *
			                  POWER(SIN(RADIANS(:lng - l.longitude) / 2), 2)
			              )
			          )
			      ) <= :radius * 2
			""", countQuery = """
			        SELECT COUNT(l.*)
			            FROM dgtl_location l
			            JOIN dgtl_location_type lt\s
			                ON lt.id = l.location_type_id
			            WHERE (:agentType = 'ALL' OR lt.name_en LIKE :agentType)
			              AND l.status = 'ACTIVE' AND lt.status = 'ACTIVE'
			              AND l.latitude BETWEEN :minLat AND :maxLat
			              AND l.longitude BETWEEN :minLng AND :maxLng
			              AND (
			                  2 * :earthRadius * ASIN(
			                      SQRT(
			                          POWER(SIN(RADIANS(:lat - l.latitude) / 2), 2) +
			                          COS(RADIANS(:lat)) * COS(RADIANS(l.latitude)) *
			                          POWER(SIN(RADIANS(:lng - l.longitude) / 2), 2)
			                      )
			                  )
			              ) <= :radius * 2
			""", nativeQuery = true)
    Page<Location> findNearbyLocations(
            @Param("agentType") String agentType,
            @Param("lat") double latitude,
            @Param("lng") double longitude,
            @Param("radius") double radiusKm,
            @Param("minLat") double minLat,
            @Param("maxLat") double maxLat,
            @Param("minLng") double minLng,
            @Param("maxLng") double maxLng,
            @Param("earthRadius") double earthRadius,
            Pageable pageable);

    List<Location> findByLocationTypeId(Long locationTypeId);
}
