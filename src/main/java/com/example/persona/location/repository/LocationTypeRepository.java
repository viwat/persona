package com.example.persona.location.repository;

import com.example.persona.location.model.LocationType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LocationTypeRepository extends JpaRepository<LocationType, Long> {
    Optional<LocationType> findByCode(String code);
}
