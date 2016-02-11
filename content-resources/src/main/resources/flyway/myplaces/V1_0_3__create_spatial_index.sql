DROP INDEX IF EXISTS my_places_geom_idx;
CREATE INDEX my_places_geom_idx ON my_places USING GIST (geometry);
