ALTER TABLE portti_bundle RENAME TO oskari_bundle;

ALTER TABLE oskari_bundle
    ALTER COLUMN name TYPE TEXT,
    ALTER COLUMN config TYPE TEXT DEFAULT '',
    ALTER COLUMN state TYPE TEXT DEFAULT '',
    DROP COLUMN startup;

ALTER TABLE portti_view RENAME TO oskari_appsetup;

ALTER TABLE oskari_appsetup
    ALTER COLUMN name TYPE TEXT NOT NULL,
    ALTER COLUMN description TYPE TEXT,
    ALTER COLUMN application TYPE TEXT DEFAULT 'geoportal',
    ALTER COLUMN domain TYPE TEXT DEFAULT '',
    DROP COLUMN application_dev_prefix,
    DROP COLUMN old_id;

ALTER TABLE portti_view_bundle_seq RENAME TO oskari_appsetup_bundles;

ALTER TABLE oskari_appsetup_bundles
    RENAME COLUMN view_id TO appsetup_id,
    ALTER COLUMN name TYPE TEXT,
    ALTER COLUMN bundleinstance TYPE TEXT DEFAULT '',
    DROP COLUMN startup;
