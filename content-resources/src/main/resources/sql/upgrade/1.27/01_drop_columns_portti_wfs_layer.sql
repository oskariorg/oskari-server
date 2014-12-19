-- drop overlapping locales column in portti_wfs_layer table
-- no in use
ALTER TABLE portti_wfs_layer
   DROP COLUMN  locales
 