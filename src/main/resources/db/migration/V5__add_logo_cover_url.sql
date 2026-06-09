-- V5: Add logo and cover image URL fields to locations
-- Both are optional; admins provide the URL (from CDN / S3 / wherever).

ALTER TABLE locations
    ADD COLUMN IF NOT EXISTS logo_url  VARCHAR(1000),
    ADD COLUMN IF NOT EXISTS cover_url VARCHAR(1000);

COMMENT ON COLUMN locations.logo_url  IS 'Brand logo URL — shown on map pins and list cards';
COMMENT ON COLUMN locations.cover_url IS 'Cover/hero image URL — shown on location detail page';
