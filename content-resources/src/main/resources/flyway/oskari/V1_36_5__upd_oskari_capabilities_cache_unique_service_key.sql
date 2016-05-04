-- Add column version to cache unique key


ALTER TABLE oskari_capabilities_cache DROP CONSTRAINT IF EXISTS oskari_capabilities_cache__unique_service;

ALTER TABLE oskari_capabilities_cache
  ADD CONSTRAINT oskari_capabilities_cache__unique_service UNIQUE(layertype, version, url);
