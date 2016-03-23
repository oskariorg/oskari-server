DROP INDEX IF EXISTS analysis_data_analysis_id_idx;
CREATE INDEX analysis_data_analysis_id_idx
ON analysis_data
USING btree
(analysis_id);


DROP index IF EXISTS analysis_uuid_index;
CREATE INDEX analysis_uuid_index on analysis USING btree(uuid);

DROP index IF EXISTS analysis_style_id_idx;
CREATE INDEX analysis_style_id_idx
ON analysis
USING btree
(style_id);
