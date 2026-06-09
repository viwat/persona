-- V3__add_location_type_partner.sql
-- Example: how to safely add a new location type without breaking anything.
-- Uncomment and run when a new type (e.g. "partner") needs to be added.

-- Step 1: Relax the CHECK constraint
-- ALTER TABLE locations DROP CONSTRAINT chk_location_type;
-- ALTER TABLE locations ADD CONSTRAINT chk_location_type
--     CHECK (type IN ('branch', 'atm_crm', 'agent', 'master_agent', 'partner'));

-- Step 2: Add enum value to LocationType.java and redeploy
-- Step 3: Run POST /api/v1/admin/search/reindex to update Meilisearch filterableAttributes

-- This migration is intentionally a no-op placeholder.
SELECT 1;
