-- V10: Data-driven location types
-- The LocationType enum is gone: `type` now holds a dgtl_location_category.code and is
-- validated against the category table instead of a hardcoded list. New categories created
-- through the admin API are usable immediately — no code change, no redeploy.
--
-- `category_code` (V9) duplicated `type` row-for-row (the service kept them in lockstep),
-- so the column is dropped and its FK moves onto `type` itself.

-- Safety: V9 backfilled category_code from type, but guarantee equality before dropping.
UPDATE locations SET type = category_code
WHERE category_code IS NOT NULL AND type IS DISTINCT FROM category_code;

ALTER TABLE locations DROP CONSTRAINT chk_location_type;
ALTER TABLE locations DROP CONSTRAINT fk_location_category;
DROP INDEX IF EXISTS idx_location_category_code;
ALTER TABLE locations DROP COLUMN category_code;

-- Every type value must reference an existing category. ON DELETE RESTRICT pairs with the
-- categories' soft-delete strategy (rows persist with status DELETED), so it never trips
-- in normal operation while still blocking accidental hard-deletes of an in-use category.
ALTER TABLE locations
    ADD CONSTRAINT fk_location_type
        FOREIGN KEY (type)
        REFERENCES dgtl_location_category (code)
        ON UPDATE CASCADE
        ON DELETE RESTRICT;

COMMENT ON COLUMN locations.type IS 'References dgtl_location_category.code — data-driven, no enum.';
