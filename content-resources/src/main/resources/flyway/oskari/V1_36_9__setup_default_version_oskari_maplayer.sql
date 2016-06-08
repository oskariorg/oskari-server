-- Capabilities cache fails if version is null;

UPDATE oskari_maplayer SET version = '' WHERE version is null;
ALTER TABLE oskari_maplayer ALTER COLUMN version SET NOT NULL;
ALTER TABLE oskari_maplayer ALTER COLUMN version SET DEFAULT '';