-- we really should rename this table to oskari_user_credentials etc but leave the name as is for now
ALTER TABLE IF EXISTS oskari_jaas_users
  ALTER COLUMN password TYPE text;

ALTER TABLE IF EXISTS oskari_jaas_users
  ALTER COLUMN login TYPE text;
