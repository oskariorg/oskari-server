-- add parse_config column to portti_wfs_template_model table
-- this column is used for WFS path parser configuration setup

ALTER TABLE portti_wfs_template_model
   ADD parse_config text;