package com.example.persona.location.repository;

import com.example.persona.location.entity.LocationEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LocationJpaRepository extends JpaRepository<LocationEntity, UUID> {

    List<LocationEntity> findByTypeInAndStatus(List<String> types, String status);

    Page<LocationEntity> findByTypeInAndStatus(List<String> types, String status, Pageable pageable);

    /** "All types" browse — types are data-driven (category codes), so no enum to expand. */
    List<LocationEntity> findByStatus(String status);

    Page<LocationEntity> findByStatus(String status, Pageable pageable);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, UUID id);

    /**
     * Haversine proximity query — PostgreSQL-native, no PostGIS required.
     * Used as fallback when Meilisearch is unavailable.
     *
     * The bounding-box pre-filter (minLat/maxLat/minLon/maxLon) uses the
     * composite B-tree index on (latitude, longitude) to discard rows outside
     * the search area before the more expensive Haversine check runs.
     */
    @Query(value = """
            SELECT l.*,
                6371000.0 * 2 * ASIN(SQRT(
                    POWER(SIN(RADIANS((l.latitude  - :lat) / 2)), 2) +
                    COS(RADIANS(:lat)) * COS(RADIANS(l.latitude)) *
                    POWER(SIN(RADIANS((l.longitude - :lon) / 2)), 2)
                )) AS dist
            FROM locations l
            WHERE l.latitude  BETWEEN :minLat AND :maxLat
              AND l.longitude BETWEEN :minLon AND :maxLon
              AND 6371000.0 * 2 * ASIN(SQRT(
                    POWER(SIN(RADIANS((l.latitude  - :lat) / 2)), 2) +
                    COS(RADIANS(:lat)) * COS(RADIANS(l.latitude)) *
                    POWER(SIN(RADIANS((l.longitude - :lon) / 2)), 2)
                )) <= :radiusMeters
              AND l.status = 'ACTIVE'
              AND (:types IS NULL OR l.type = ANY(CAST(:types AS text[])))
            ORDER BY dist ASC
            LIMIT :limit
            """, nativeQuery = true)
    List<Object[]> findNearbyRaw(
            @Param("lat") double lat,
            @Param("lon") double lon,
            @Param("minLat") double minLat,
            @Param("maxLat") double maxLat,
            @Param("minLon") double minLon,
            @Param("maxLon") double maxLon,
            @Param("radiusMeters") double radiusMeters,
            @Param("types") String types,
            @Param("limit") int limit);

    /**
     * PostgreSQL full-text search fallback (ILIKE — no extensions required).
     */
    @Query(value = """
            SELECT * FROM locations l
            WHERE l.status = 'ACTIVE'
              AND (:types IS NULL OR l.type = ANY(CAST(:types AS text[])))
              AND (:province IS NULL OR LOWER(l.province) = LOWER(:province))
              AND (:district IS NULL OR LOWER(l.district) = LOWER(:district))
              AND (:commune IS NULL OR LOWER(l.commune) = LOWER(:commune))
              AND (:text IS NULL OR (
                   l.name ILIKE '%' || :text || '%'
                OR l.branch_name ILIKE '%' || :text || '%'
                OR l.branch_code ILIKE '%' || :text || '%'
                OR l.street ILIKE '%' || :text || '%'
                OR l.commune ILIKE '%' || :text || '%'
                OR l.district ILIKE '%' || :text || '%'
                OR l.province ILIKE '%' || :text || '%'
              ))
            ORDER BY l.name ASC
            LIMIT :limit OFFSET :offset
            """, nativeQuery = true)
    List<LocationEntity> fullTextSearch(
            @Param("text") String text,
            @Param("types") String types,
            @Param("province") String province,
            @Param("district") String district,
            @Param("commune") String commune,
            @Param("offset") int offset,
            @Param("limit") int limit);

    @Query(value = """
            SELECT COUNT(*) FROM locations l
            WHERE l.status = 'ACTIVE'
              AND (:types IS NULL OR l.type = ANY(CAST(:types AS text[])))
              AND (:province IS NULL OR LOWER(l.province) = LOWER(:province))
              AND (:district IS NULL OR LOWER(l.district) = LOWER(:district))
              AND (:commune IS NULL OR LOWER(l.commune) = LOWER(:commune))
              AND (:text IS NULL OR (
                   l.name ILIKE '%' || :text || '%'
                OR l.branch_name ILIKE '%' || :text || '%'
                OR l.branch_code ILIKE '%' || :text || '%'
                OR l.street ILIKE '%' || :text || '%'
                OR l.commune ILIKE '%' || :text || '%'
                OR l.district ILIKE '%' || :text || '%'
                OR l.province ILIKE '%' || :text || '%'
              ))
            """, nativeQuery = true)
    long countFullTextSearch(
            @Param("text") String text,
            @Param("types") String types,
            @Param("province") String province,
            @Param("district") String district,
            @Param("commune") String commune);
}
