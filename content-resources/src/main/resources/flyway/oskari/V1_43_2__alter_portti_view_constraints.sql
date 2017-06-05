-- drop appsetup bundles when view is deleted;
ALTER TABLE IF EXISTS portti_view_bundle_seq DROP CONSTRAINT portti_view_bundle_seq_view_id_fkey;

ALTER TABLE IF EXISTS portti_view_bundle_seq ADD CONSTRAINT portti_view_bundle_seq_view_id_fkey FOREIGN KEY (view_id)
REFERENCES portti_view (id) MATCH SIMPLE
ON UPDATE NO ACTION ON DELETE CASCADE;
