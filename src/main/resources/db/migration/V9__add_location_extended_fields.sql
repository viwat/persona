-- V9: Extended location fields (FR-03)
-- Source-system identifiers, presentation metadata, and a category link.
-- All nullable/additive — existing rows remain valid.

ALTER TABLE locations
    ADD COLUMN facebook_url  VARCHAR(500),
    ADD COLUMN image_url     VARCHAR(1000),
    ADD COLUMN branch_code   VARCHAR(50),
    ADD COLUMN branch_name   VARCHAR(255),
    ADD COLUMN atm_serial    VARCHAR(100),
    ADD COLUMN category_code VARCHAR(100),
    ADD COLUMN avg_rating    DOUBLE PRECISION,
    ADD COLUMN action_label  VARCHAR(255),
    ADD COLUMN action_url    VARCHAR(1000);

-- Backfill the category from the existing type so locations are categorised
-- immediately (category codes intentionally mirror LocationType codes).
UPDATE locations SET category_code = type WHERE category_code IS NULL;

-- Supports filtering/listing locations by category.
CREATE INDEX idx_location_category_code ON locations (category_code);

-- Referential integrity: a location may only reference an existing category code.
-- Runs after the backfill above, so all current rows already satisfy it.
-- ON DELETE RESTRICT pairs with the soft-delete strategy (rows persist, so this never trips
-- in normal operation) to block accidental hard-deletes of an in-use category.
ALTER TABLE locations
    ADD CONSTRAINT fk_location_category
        FOREIGN KEY (category_code)
        REFERENCES dgtl_location_category (code)
        ON UPDATE CASCADE
        ON DELETE RESTRICT;

-- Guard the rating range at the database level (0.0–5.0).
ALTER TABLE locations
    ADD CONSTRAINT chk_location_avg_rating CHECK (avg_rating IS NULL OR (avg_rating >= 0 AND avg_rating <= 5));

COMMENT ON COLUMN locations.category_code IS 'References dgtl_location_category.code; defaults to the location type code.';
COMMENT ON COLUMN locations.avg_rating    IS 'Average customer rating 0.0–5.0; null when unrated.';
COMMENT ON COLUMN locations.branch_code   IS 'Core-banking branch code (FR-03).';
COMMENT ON COLUMN locations.atm_serial    IS 'ATM/CRM serial number (FR-03).';
