-- should ever only have one row for each service
ALTER TABLE oskari_capabilities_cache
  ADD CONSTRAINT oskari_capabilities_cache__unique_service UNIQUE (layertype, url);
