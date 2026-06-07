package com.example.persona.location.repository;

import com.example.persona.location.model.LocationAvailableService;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LocationAvailableServiceRepository extends JpaRepository<LocationAvailableService, Long> {
    Optional<LocationAvailableService> findByIdAndLocationId(Long operatingHourId, Long locationId);
}
