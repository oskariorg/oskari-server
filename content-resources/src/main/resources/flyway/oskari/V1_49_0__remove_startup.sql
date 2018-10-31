ALTER TABLE portti_bundle ALTER COLUMN startup DROP NOT NULL;

UPDATE portti_bundle SET startup = NULL;
UPDATE portti_view_bundle_seq SET startup = NULL;

ALTER TABLE portti_bundle ADD CONSTRAINT nullchk CHECK (startup IS NULL);
ALTER TABLE portti_view_bundle_seq ADD CONSTRAINT nullchk CHECK (startup IS NULL);

COMMENT ON COLUMN portti_bundle.startup IS 'Deprecated column, always NULL';
COMMENT ON COLUMN portti_view_bundle_seq.startup IS 'Deprecated column, always NULL';