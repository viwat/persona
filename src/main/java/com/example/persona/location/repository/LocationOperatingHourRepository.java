package com.example.persona.location.repository;

import com.example.persona.location.model.LocationOperatingHour;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LocationOperatingHourRepository extends JpaRepository<LocationOperatingHour, Long> {
    Optional<LocationOperatingHour> findByIdAndLocationId(Long operatingHourId, Long locationId);
}
