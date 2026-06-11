-- V11: Rename location category tables to location type
-- Aligns the DB schema with the Java rename: LocationCategory → LocationType.
-- PostgreSQL FK constraints track tables by OID, so fk_location_type on locations.type
-- automatically follows the dgtl_location_category → dgtl_location_type rename.

ALTER TABLE dgtl_location_category      RENAME TO dgtl_location_type;
ALTER TABLE dgtl_location_category_tag  RENAME TO dgtl_location_type_tag;

-- Rename constraints and indexes to match the new table names
ALTER INDEX  uk_location_category_code             RENAME TO uk_location_type_code;
ALTER INDEX  idx_location_category_display_order   RENAME TO idx_location_type_display_order;

ALTER TABLE dgtl_location_type_tag
    RENAME CONSTRAINT fk_loc_cat_tag_category TO fk_loc_type_tag_type;
ALTER TABLE dgtl_location_type_tag
    RENAME CONSTRAINT fk_loc_cat_tag_tag TO fk_loc_type_tag_tag;

COMMENT ON TABLE dgtl_location_type     IS 'Location types (FR-03) used by locator filters; referenced by locations via code.';
COMMENT ON TABLE dgtl_location_type_tag IS 'Many-to-many join between location types and tags.';

COMMENT ON COLUMN locations.type IS 'References dgtl_location_type.code — data-driven, no enum.';
