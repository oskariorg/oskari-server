-- allow longer usernames;
ALTER TABLE oskari_users
   ALTER COLUMN  user_name TYPE character varying(128);

-- add constraint on uuid
ALTER TABLE oskari_users
ADD CONSTRAINT oskari_users_uuid_key UNIQUE (uuid);

-- add new attributes field that can be used to store additional data/user
ALTER TABLE oskari_users
   ADD attributes text DEFAULT '{}';
