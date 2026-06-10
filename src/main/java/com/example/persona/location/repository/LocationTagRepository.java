package com.example.persona.location.repository;

import com.example.persona.enums.StatusType;
import com.example.persona.location.model.LocationTag;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LocationTagRepository extends JpaRepository<LocationTag, Long> {

    Optional<LocationTag> findByCode(String code);

    boolean existsByCode(String code);

    List<LocationTag> findByStatusOrderByCodeAsc(StatusType status);
}
