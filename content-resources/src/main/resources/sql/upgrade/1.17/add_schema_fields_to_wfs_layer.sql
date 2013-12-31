ALTER TABLE portti_wfs_layer ADD COLUMN schema_changed timestamp with time zone;
ALTER TABLE portti_wfs_layer ADD COLUMN schema_last timestamp with time zone;
ALTER TABLE portti_wfs_layer ADD COLUMN schema_status character varying(512);
ALTER TABLE portti_wfs_layer ADD COLUMN custom_parser boolean NOT NULL DEFAULT false;
ALTER TABLE portti_wfs_layer ADD COLUMN test_location character varying(512) default '[]';
ALTER TABLE portti_wfs_layer ADD COLUMN test_zoom integer NOT NULL DEFAULT 9;