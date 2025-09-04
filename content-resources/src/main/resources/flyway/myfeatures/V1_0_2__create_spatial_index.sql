DROP INDEX IF EXISTS myfeatures_feature_geom_idx;
CREATE INDEX myfeatures_feature_geom_idx ON myfeatures_feature USING gist(geom);