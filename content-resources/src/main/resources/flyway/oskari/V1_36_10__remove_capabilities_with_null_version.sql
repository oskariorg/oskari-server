-- Cleanup capabilities where version is null - these don't work correctly;

DELETE FROM oskari_capabilities_cache WHERE version is null;
ALTER TABLE oskari_capabilities_cache ALTER COLUMN version SET NOT NULL;
ALTER TABLE oskari_capabilities_cache ALTER COLUMN version SET DEFAULT '';