package com.example.persona.location.repository;

import com.example.persona.location.entity.AuditEventEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditJpaRepository extends JpaRepository<AuditEventEntity, UUID> {

    List<AuditEventEntity> findByLocationIdOrderByOccurredAtDesc(UUID locationId);
}
