package com.example.persona.location.service;

import com.example.persona.location.entity.AuditEventEntity;
import com.example.persona.location.model.AuditAction;
import com.example.persona.location.model.AuditEvent;
import com.example.persona.location.repository.AuditJpaRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditJpaRepository auditJpaRepository;

    public void record(AuditEvent event) {
        try {
            auditJpaRepository.save(AuditEventEntity.builder()
                    .id(event.id())
                    .locationId(event.locationId())
                    .action(event.action().name())
                    .actor(event.actor())
                    .snapshot(event.snapshot())
                    .occurredAt(event.occurredAt())
                    .build());
        } catch (Exception e) {
            log.error("Failed to record audit event for location {}: {}", event.locationId(), e.getMessage());
        }
    }

    public List<AuditEvent> findByLocationId(UUID locationId) {
        return auditJpaRepository.findByLocationIdOrderByOccurredAtDesc(locationId).stream()
                .map(e -> new AuditEvent(
                        e.getId(),
                        e.getLocationId(),
                        AuditAction.valueOf(e.getAction()),
                        e.getActor(),
                        e.getSnapshot(),
                        e.getOccurredAt()))
                .toList();
    }
}
