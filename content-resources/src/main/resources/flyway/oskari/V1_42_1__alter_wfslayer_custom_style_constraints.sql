-- drop wfslayer/style mappings when user or role is deleted;
ALTER TABLE IF EXISTS portti_wfs_layers_styles DROP CONSTRAINT portti_wfs_layers_styles_wfs_layer_fkey;

ALTER TABLE IF EXISTS portti_wfs_layers_styles ADD CONSTRAINT portti_wfs_layers_styles_wfs_layer_fkey FOREIGN KEY (wfs_layer_id)
REFERENCES portti_wfs_layer (id) MATCH SIMPLE
ON UPDATE NO ACTION ON DELETE CASCADE;
