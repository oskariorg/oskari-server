-- drop previously unused uuid
ALTER TABLE portti_view DROP COLUMN uuid;
-- create a new one with UUID type (currently leaves blank, possible generated in server code in the future)
ALTER TABLE portti_view ADD COLUMN uuid UUID;

-- Other way of doing this is which generates missing uuids for views:
-- CREATE EXTENSION "uuid-ossp";
-- ALTER TABLE portti_view ALTER COLUMN uuid SET DATA TYPE UUID USING (uuid_generate_v4());