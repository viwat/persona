package com.example.persona.home.repository;

import com.example.persona.home.model.Promotional;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PromotionalRepository extends JpaRepository<Promotional, Long> {
    List<Promotional> findByEffectiveFromBeforeAndEffectiveToAfter(
            LocalDateTime effectiveFrom, LocalDateTime effectiveTo);
}
