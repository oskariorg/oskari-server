DROP INDEX IF EXISTS analysis_data_geom_idx;
CREATE INDEX analysis_data_geom_idx ON analysis_data USING GIST (geometry);
