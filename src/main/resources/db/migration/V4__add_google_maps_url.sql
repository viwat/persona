-- V4__add_google_maps_url.sql
ALTER TABLE locations ADD COLUMN IF NOT EXISTS google_maps_url VARCHAR(500);
