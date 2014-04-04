DROP TABLE IF EXISTS oskari_users;
DROP TABLE IF EXISTS oskari_roles;

CREATE TABLE oskari_users (
  id serial NOT NULL,
  user_name character varying(25) NOT NULL,
  first_name character varying(128),
  last_name character varying(128),
  uuid character varying(64),
  CONSTRAINT oskari_users_pkey PRIMARY KEY (id),
  UNIQUE (user_name)
);

CREATE TABLE oskari_roles (
  id serial NOT NULL,
  name character varying(25) NOT NULL,
  is_guest boolean default FALSE,
  UNIQUE (name)
);
