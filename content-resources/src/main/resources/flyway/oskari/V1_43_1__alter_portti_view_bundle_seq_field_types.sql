
ALTER TABLE portti_view_bundle_seq
  ALTER COLUMN config TYPE text;

ALTER TABLE portti_view_bundle_seq
  ALTER COLUMN config SET DEFAULT '{}';

ALTER TABLE portti_view_bundle_seq
  ALTER COLUMN state TYPE text;

ALTER TABLE portti_view_bundle_seq
  ALTER COLUMN state SET DEFAULT '{}';

ALTER TABLE portti_view_bundle_seq
  ALTER COLUMN startup TYPE text;

ALTER TABLE portti_view_bundle_seq
  ALTER COLUMN startup SET DEFAULT '{}';

