-- drop overlapping values in portti_wfs_layer table
-- execute 1st 01_update_oskari_maplayer.sql
ALTER TABLE portti_wfs_layer
   DROP COLUMN  url,
   DROP COLUMN   username,
   DROP COLUMN   password,
   DROP COLUMN   srs_name,
   DROP COLUMN   wfs_version;
