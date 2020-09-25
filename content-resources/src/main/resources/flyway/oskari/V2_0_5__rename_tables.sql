ALTER TABLE portti_bundle RENAME TO oskari_bundle;

ALTER TABLE oskari_bundle
    ALTER COLUMN name TYPE TEXT,
    ALTER COLUMN config TYPE TEXT,
    ALTER COLUMN state TYPE TEXT,
    DROP COLUMN startup;

ALTER TABLE portti_view RENAME TO oskari_appsetup;

ALTER TABLE oskari_appsetup
    ALTER COLUMN name TYPE TEXT,
    ALTER COLUMN description TYPE TEXT,
    ALTER COLUMN application TYPE TEXT,
    ALTER COLUMN domain TYPE TEXT,
    DROP COLUMN application_dev_prefix,
    DROP COLUMN old_id;

ALTER TABLE portti_view_bundle_seq RENAME TO oskari_appsetup_bundles;

ALTER TABLE oskari_appsetup_bundles
    ALTER COLUMN bundleinstance TYPE TEXT,
    DROP COLUMN startup;

ALTER TABLE oskari_appsetup_bundles
    RENAME COLUMN view_id TO appsetup_id;
