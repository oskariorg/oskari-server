-- add columns for username and password;
ALTER TABLE oskari_maplayer ADD COLUMN username character varying(256);
ALTER TABLE oskari_maplayer ADD COLUMN password character varying(256);
