-- Add column version to manage capabilites caches as version based
-- Column: version

ALTER TABLE oskari_capabilities_cache ADD COLUMN "version" text;

