
-- run node script before this since there can be duplicate uuids! ;
-- SCRIPT=1.25/01-generate-uuids-for-views node app.js ;

ALTER TABLE portti_view ADD CONSTRAINT portti_view_uuid_key UNIQUE (uuid);