ALTER TABLE IF EXISTS oskari_jaas_users
  ALTER COLUMN login TYPE text,
  ALTER COLUMN password TYPE text;