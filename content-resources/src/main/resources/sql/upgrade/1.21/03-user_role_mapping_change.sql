
-- add user_id column;
ALTER TABLE oskari_role_oskari_user ADD COLUMN user_id bigint;

-- add user_id value based on user_name mapping;
UPDATE oskari_role_oskari_user SET user_id = (SELECT id FROM oskari_users WHERE oskari_users.user_name = oskari_role_oskari_user.user_name);

-- drop user_name column;
ALTER TABLE oskari_role_oskari_user DROP COLUMN user_name;

-- setup the new constraint;
ALTER TABLE oskari_role_oskari_user ADD FOREIGN KEY(user_id) REFERENCES oskari_users(id);