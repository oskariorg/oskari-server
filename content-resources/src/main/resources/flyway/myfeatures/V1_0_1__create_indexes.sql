DROP INDEX IF EXISTS myfeatures_feature_layer_id_idx;
CREATE INDEX myfeatures_feature_layer_id_idx ON myfeatures_feature USING btree(layer_id);

DROP INDEX IF EXISTS myfeatures_layer_owner_uuid_idx;
CREATE INDEX myfeatures_layer_owner_uuid_idx on myfeatures_layer USING btree(owner_uuid);