-- drop duplicates, those who have same layertype + url
DELETE FROM oskari_capabilities_cache
WHERE id IN (SELECT id
             FROM (SELECT id,
                     ROW_NUMBER() OVER (partition BY layertype, url ORDER BY id) AS rnum
                   FROM oskari_capabilities_cache) t
             WHERE t.rnum > 1);

-- should ever only have one row for each service
ALTER TABLE oskari_capabilities_cache
  ADD CONSTRAINT oskari_capabilities_cache__unique_service UNIQUE (layertype, url);
