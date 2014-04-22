DROP TABLE IF EXISTS oskari_jaas_users;
DROP TABLE IF EXISTS oskari_jaas_roles;

CREATE TABLE oskari_jaas_users (
  id serial NOT NULL,
  login character varying(25) NOT NULL,
  password character varying(50) NOT NULL,
  CONSTRAINT oskari_jaas_users_pkey PRIMARY KEY (id),
  UNIQUE (login)
);

CREATE TABLE oskari_jaas_roles (
  id serial NOT NULL,
  login character varying(25) NOT NULL,
  role character varying(50) NOT NULL,
  CONSTRAINT oskari_jaas_roles_pkey PRIMARY KEY (id)
);
