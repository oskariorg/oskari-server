DROP INDEX IF EXISTS my_places_category_id_idx;
CREATE INDEX my_places_category_id_idx
ON my_places
USING btree
(category_id);

DROP index IF EXISTS my_places_uuid_index;
CREATE INDEX my_places_uuid_index on my_places USING btree(uuid);