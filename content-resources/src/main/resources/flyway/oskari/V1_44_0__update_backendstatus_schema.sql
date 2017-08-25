BEGIN;

-- drop views, handle logic in code not db
DROP VIEW IF EXISTS portti_backendstatus_allknown;
DROP VIEW IF EXISTS portti_backendalert;

-- modernise the name of the table
ALTER TABLE portti_backendstatus RENAME TO oskari_backendstatus;

-- drop columns that are not needed
ALTER TABLE oskari_backendstatus DROP COLUMN id;
ALTER TABLE oskari_backendstatus DROP COLUMN source;
ALTER TABLE oskari_backendstatus DROP COLUMN statusjson;

-- change the type of maplayer_id to int
TRUNCATE oskari_backendstatus; -- clean the table, make sure the next operation won't fail
ALTER TABLE oskari_backendstatus ALTER maplayer_id TYPE int USING maplayer_id::int;
ALTER TABLE oskari_backendstatus ALTER maplayer_id SET NOT NULL;
ALTER TABLE oskari_backendstatus ADD FOREIGN KEY (maplayer_id) REFERENCES oskari_maplayer(id);

COMMIT;
