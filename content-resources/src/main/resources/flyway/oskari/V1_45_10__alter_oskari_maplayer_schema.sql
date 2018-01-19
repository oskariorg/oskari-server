BEGIN;
ALTER TABLE oskari_maplayer ADD COLUMN capabilities_last_updated timestamp with time zone;
ALTER TABLE oskari_maplayer ADD COLUMN capabilities_update_rate_sec int DEFAULT 0;
COMMIT;
