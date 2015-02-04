DROP TABLE IF EXISTS oskari_role_oskari_user;
DROP TABLE IF EXISTS oskari_role_external_mapping;
DROP TABLE IF EXISTS oskari_roles;
DROP TABLE IF EXISTS oskari_users;

-- contains user information;
CREATE TABLE oskari_users (
  id serial NOT NULL,
  user_name character varying(128) NOT NULL,
  first_name character varying(128),
  last_name character varying(128),
  email character varying(256),
  uuid character varying(64),
  attributes text DEFAULT '{}',
  CONSTRAINT oskari_users_pkey PRIMARY KEY (id),
  CONSTRAINT oskari_users_user_name_key UNIQUE (user_name),
  CONSTRAINT oskari_users_uuid_key UNIQUE (uuid)
);

-- contains roles used in Oskari;
CREATE TABLE oskari_roles (
  id serial NOT NULL,
  name character varying(25) NOT NULL,
  is_guest boolean default FALSE,
  CONSTRAINT oskari_roles_pkey PRIMARY KEY (id),
  UNIQUE (name)
);

-- maps Oskari roles to users;
CREATE TABLE oskari_role_oskari_user
(
  id serial NOT NULL,
  role_id integer,
  user_id bigint,
  CONSTRAINT oskari_role_oskari_user_pkey PRIMARY KEY (id),
  CONSTRAINT oskari_role_oskari_user_role_id_fkey FOREIGN KEY (role_id)
  REFERENCES oskari_roles (id) MATCH SIMPLE
  ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT oskari_role_oskari_user_user_id_fkey FOREIGN KEY (user_id)
  REFERENCES oskari_users (id) MATCH SIMPLE
  ON UPDATE NO ACTION ON DELETE NO ACTION
);

-- maps external role name to Oskari role;
CREATE TABLE oskari_role_external_mapping (
  roleid  bigint NOT NULL,
  name character varying(50) NOT NULL,
  external_type character varying(50) NOT NULL default '',
  CONSTRAINT oskari_role_external_mapping_fkey FOREIGN KEY (roleid)
  REFERENCES oskari_roles (id) MATCH SIMPLE
  ON UPDATE NO ACTION ON DELETE CASCADE
);