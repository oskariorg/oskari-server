-- Add a table for user registration
CREATE TABLE IF NOT EXISTS oskari_users_pending
(
  id        BIGSERIAL NOT NULL,
  user_name text,
  email     text,
  uuid      text,
  expiry_timestamp timestamp with time zone,
  CONSTRAINT oskari_users_pending_pkey PRIMARY KEY (id)
);
