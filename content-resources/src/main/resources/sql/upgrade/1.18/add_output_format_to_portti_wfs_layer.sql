-- add a column to save the output format of the wfs query;

ALTER TABLE portti_wfs_layer ADD COLUMN output_format CHARACTER VARYING(256);