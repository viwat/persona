-- V6: Operating status (temporarily_closed) + actor tracking (created_by / updated_by)

-- Temporarily closed flag — location stays ACTIVE but is not serving customers
ALTER TABLE locations
    ADD COLUMN IF NOT EXISTS temporarily_closed BOOLEAN  NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS closed_until        TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS created_by          VARCHAR(255),
    ADD COLUMN IF NOT EXISTS updated_by          VARCHAR(255);

CREATE INDEX IF NOT EXISTS idx_locations_temporarily_closed
    ON locations (temporarily_closed)
    WHERE temporarily_closed = TRUE;

COMMENT ON COLUMN locations.temporarily_closed IS 'True when closed for maintenance, holidays, etc. Status remains ACTIVE.';
COMMENT ON COLUMN locations.closed_until        IS 'Optional scheduled re-open time. NULL = indefinite closure.';
COMMENT ON COLUMN locations.created_by          IS 'Actor (from X-Actor header) who created this location.';
COMMENT ON COLUMN locations.updated_by          IS 'Actor (from X-Actor header) who last updated this location.';
