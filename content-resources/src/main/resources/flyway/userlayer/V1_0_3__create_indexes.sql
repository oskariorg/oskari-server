DROP INDEX IF EXISTS user_layer_data_user_layer_id_idx;
CREATE INDEX user_layer_data_user_layer_id_idx
ON user_layer_data
USING btree
(user_layer_id);

DROP index IF EXISTS user_layer_uuid_index;
CREATE INDEX user_layer_uuid_index on user_layer USING btree(uuid);

DROP index IF EXISTS user_layer_style_id_idx;
CREATE INDEX user_layer_style_id_idx
ON user_layer
USING btree
(style_id);
