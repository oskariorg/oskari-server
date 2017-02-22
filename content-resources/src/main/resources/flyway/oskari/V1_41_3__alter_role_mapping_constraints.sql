-- drop user/role mappings when user or role is deleted;
ALTER TABLE IF EXISTS oskari_role_oskari_user DROP CONSTRAINT oskari_role_oskari_user_role_id_fkey;

ALTER TABLE IF EXISTS oskari_role_oskari_user ADD CONSTRAINT oskari_role_oskari_user_role_id_fkey FOREIGN KEY (role_id)
REFERENCES oskari_roles (id) MATCH SIMPLE
ON UPDATE NO ACTION ON DELETE CASCADE;

ALTER TABLE IF EXISTS oskari_role_oskari_user DROP CONSTRAINT oskari_role_oskari_user_user_id_fkey;

ALTER TABLE IF EXISTS  oskari_role_oskari_user ADD CONSTRAINT oskari_role_oskari_user_user_id_fkey FOREIGN KEY (user_id)
REFERENCES oskari_users (id) MATCH SIMPLE
ON UPDATE NO ACTION ON DELETE CASCADE;