-- V1__create_locations_schema.sql
-- Wing Bank Location Service - Initial Schema
-- Requires: PostgreSQL 14+

-- ── Main locations table ──────────────────────────────────────────────────────
CREATE TABLE locations (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(255)    NOT NULL,
    type        VARCHAR(50)     NOT NULL,
    status      VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE',

    latitude    DOUBLE PRECISION        NOT NULL,
    longitude   DOUBLE PRECISION        NOT NULL,

    -- Address (Cambodia admin divisions)
    street      VARCHAR(500),
    commune     VARCHAR(100),
    district    VARCHAR(100),
    province    VARCHAR(100)    NOT NULL,
    country     VARCHAR(100)    NOT NULL DEFAULT 'Cambodia',

    -- Contact
    phone       VARCHAR(50),
    email       VARCHAR(255),
    website     VARCHAR(500),

    -- Flexible JSON columns
    opening_hours       JSONB,
    available_services  JSONB,

    -- Audit
    created_at  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    version     BIGINT          NOT NULL DEFAULT 0,

    -- Constraints
    CONSTRAINT chk_location_type   CHECK (type IN ('branch', 'atm_crm', 'agent', 'master_agent')),
    CONSTRAINT chk_location_status CHECK (status IN ('ACTIVE', 'INACTIVE'))
);

-- ── Standard B-tree indexes ───────────────────────────────────────────────────
CREATE INDEX idx_locations_lat_lon     ON locations (latitude, longitude);
CREATE INDEX idx_locations_type        ON locations (type);
CREATE INDEX idx_locations_status      ON locations (status);
CREATE INDEX idx_locations_province    ON locations (province);
CREATE INDEX idx_locations_district    ON locations (district);
CREATE INDEX idx_locations_commune     ON locations (commune);
CREATE INDEX idx_locations_created_at  ON locations (created_at DESC);

-- ── Composite index for the most common query (type + status) ─────────────────
CREATE INDEX idx_locations_type_status ON locations (type, status);

-- ── Updated_at auto-update trigger ────────────────────────────────────────────
CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_locations_updated_at
    BEFORE UPDATE ON locations
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- ── Comments ──────────────────────────────────────────────────────────────────
COMMENT ON TABLE  locations                   IS 'Wing Bank location data — branches, ATMs, agents';
COMMENT ON COLUMN locations.latitude          IS 'WGS84 latitude.';
COMMENT ON COLUMN locations.longitude         IS 'WGS84 longitude.';
COMMENT ON COLUMN locations.type              IS 'branch | atm_crm | agent | master_agent';
COMMENT ON COLUMN locations.opening_hours     IS 'JSON: {schedule: {MONDAY: {openTime, closeTime}}, specialNotes}';
COMMENT ON COLUMN locations.available_services IS 'JSON array of service strings';
