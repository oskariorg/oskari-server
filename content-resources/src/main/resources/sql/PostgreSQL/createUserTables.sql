DROP TABLE IF EXISTS oskari_role_oskari_user;
DROP TABLE IF EXISTS oskari_roles;
DROP TABLE IF EXISTS oskari_users;

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
  CONSTRAINT oskari_roles_pkey PRIMARY KEY (id),
  UNIQUE (name)
);

CREATE TABLE oskari_role_oskari_user (
  id serial NOT NULL,
  user_name character varying(25) REFERENCES oskari_users(user_name),
  role_id integer REFERENCES oskari_roles(id),
  CONSTRAINT oskari_role_oskari_user_pkey PRIMARY KEY (id)
);
