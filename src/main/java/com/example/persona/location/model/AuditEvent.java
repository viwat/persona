package com.example.persona.location.model;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain event capturing every mutation to a Location.
 * Actor is the value of the X-Actor header forwarded by APIM.
 * Snapshot is a JSON string of the Location state after the change.
 */
public record AuditEvent(
        UUID id, UUID locationId, AuditAction action, String actor, String snapshot, Instant occurredAt) {
    public static AuditEvent of(UUID locationId, AuditAction action, String actor, String snapshot) {
        return new AuditEvent(UUID.randomUUID(), locationId, action, actor, snapshot, Instant.now());
    }
}
