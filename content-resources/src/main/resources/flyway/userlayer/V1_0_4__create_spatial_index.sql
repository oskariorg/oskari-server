DROP INDEX IF EXISTS user_layer_data_geom_idx;
CREATE INDEX user_layer_data_geom_idx ON user_layer_data USING GIST (geometry);