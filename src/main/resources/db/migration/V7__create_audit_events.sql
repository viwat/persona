-- V7: Audit event log for every Location mutation

CREATE TABLE audit_events (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    location_id UUID        NOT NULL,
    action      VARCHAR(50) NOT NULL,
    actor       VARCHAR(255),
    snapshot    JSONB,                        -- location state after the change
    occurred_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Fast lookup of all events for a given location (e.g. audit trail UI)
CREATE INDEX idx_audit_events_location_id  ON audit_events (location_id);
CREATE INDEX idx_audit_events_occurred_at  ON audit_events (occurred_at DESC);

COMMENT ON TABLE  audit_events              IS 'Immutable audit log for Wing Bank location changes.';
COMMENT ON COLUMN audit_events.action       IS 'CREATED | UPDATED | ACTIVATED | DEACTIVATED | DELETED | CLOSED_TEMPORARILY | REOPENED | IMAGE_UPLOADED';
COMMENT ON COLUMN audit_events.actor        IS 'Value of X-Actor header set by APIM; defaults to ''system''.';
COMMENT ON COLUMN audit_events.snapshot     IS 'JSON snapshot of the Location after the change (null for DELETED).';
