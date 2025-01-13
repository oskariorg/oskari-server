CREATE TABLE categories
(
    id BIGSERIAL NOT NULL,
    category_name character varying(256) NOT NULL,
    "default" boolean,
    uuid character varying(64),
    publisher_name character varying(256),
    options json,
    locale json,
    CONSTRAINT categories_pkey PRIMARY KEY (id)
);
