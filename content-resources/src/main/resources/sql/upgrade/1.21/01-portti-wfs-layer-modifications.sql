-- add new columns for feature engine support
ALTER TABLE portti_wfs_layer ADD COLUMN job_type character varying(256);
ALTER TABLE portti_wfs_layer ADD COLUMN request_impulse character varying(256);

-- setup custom parser job type for layers that used custom_parser
update portti_wfs_layer SET job_type = 'oskari-custom-parser' WHERE id IN(select id from portti_wfs_layer where custom_parser = true);

-- drop deprecated columns
ALTER TABLE portti_wfs_layer DROP COLUMN custom_parser;
ALTER TABLE portti_wfs_layer DROP COLUMN test_location;
ALTER TABLE portti_wfs_layer DROP COLUMN test_zoom;
ALTER TABLE portti_wfs_layer DROP COLUMN schema_changed;
ALTER TABLE portti_wfs_layer DROP COLUMN schema_last;
ALTER TABLE portti_wfs_layer DROP COLUMN schema_status;