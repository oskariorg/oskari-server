UPDATE portti_view_bundle_seq SET bundleinstance = 'time-control-3d' where bundle_id = (SELECT id FROM portti_bundle where name = 'shadow-plugin-3d');
UPDATE portti_bundle SET name = 'time-control-3d' where name = 'shadow-plugin-3d';
