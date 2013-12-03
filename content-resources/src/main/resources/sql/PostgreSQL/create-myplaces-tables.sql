
DROP VIEW IF EXISTS my_places_categories;
DROP TABLE IF EXISTS my_places;
DROP TABLE IF EXISTS categories;

CREATE TABLE categories
(
  id bigserial NOT NULL,
  category_name character varying(256) NOT NULL,
  "default" boolean,
  stroke_width integer DEFAULT 1,
  stroke_color character(7),
  fill_color character(7),
  uuid character varying(64),
  dot_color character(7) DEFAULT '#00FF00'::bpchar,
  dot_size integer DEFAULT 3,
  border_width integer,
  border_color character(7),
  publisher_name character varying(256),
  dot_shape character varying(20) NOT NULL DEFAULT '1'::character varying,
  stroke_linejoin character varying(256),
  fill_pattern integer DEFAULT (-1),
  stroke_linecap character varying(256),
  stroke_dasharray character varying(256),
  border_linejoin character varying(256),
  border_dasharray character varying(256),
  CONSTRAINT categories_pkey PRIMARY KEY (id)
);


CREATE TABLE my_places
(
  id bigserial NOT NULL,
  uuid character varying(64) NOT NULL,
  category_id integer NOT NULL,
  name character varying(256) NOT NULL,
  created timestamp with time zone NOT NULL,
  updated timestamp with time zone NOT NULL,
  geometry geometry NOT NULL,
  place_desc text,
  link text,
  image_url character varying(512),
  CONSTRAINT "my_places_pKey" PRIMARY KEY (id)
);


CREATE OR REPLACE VIEW my_places_categories AS
 SELECT mp.id, mp.uuid, mp.category_id, mp.name, mp.place_desc, mp.created, mp.updated, mp.geometry, c.category_name, c."default", c.stroke_width, c.stroke_color, c.fill_color, c.dot_color, c.dot_size, c.dot_shape, c.border_width, c.border_color, c.publisher_name, mp.link, mp.image_url, c.fill_pattern, c.stroke_linejoin, c.stroke_linecap, c.stroke_dasharray, c.border_linejoin, c.border_dasharray
   FROM my_places mp, categories c
  WHERE mp.category_id = c.id;
