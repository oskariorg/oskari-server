CREATE TABLE IF NOT EXISTS categories
(
  id               BIGSERIAL              NOT NULL,
  category_name    CHARACTER VARYING(256) NOT NULL,
  "default"        BOOLEAN,
  stroke_width     INTEGER                         DEFAULT 1,
  stroke_color     CHARACTER(7),
  fill_color       CHARACTER(7),
  uuid             CHARACTER VARYING(64),
  dot_color        CHARACTER(7)                    DEFAULT '#00FF00' :: bpchar,
  dot_size         INTEGER                         DEFAULT 3,
  border_width     INTEGER,
  border_color     CHARACTER(7),
  publisher_name   CHARACTER VARYING(256),
  dot_shape        CHARACTER VARYING(20)  NOT NULL DEFAULT '1' :: CHARACTER VARYING,
  stroke_linejoin  CHARACTER VARYING(256),
  fill_pattern     INTEGER                         DEFAULT (-1),
  stroke_linecap   CHARACTER VARYING(256),
  stroke_dasharray CHARACTER VARYING(256),
  border_linejoin  CHARACTER VARYING(256),
  border_dasharray CHARACTER VARYING(256),
  CONSTRAINT categories_pkey PRIMARY KEY (id)
);


CREATE TABLE IF NOT EXISTS my_places
(
  id             BIGSERIAL                                          NOT NULL,
  uuid           CHARACTER VARYING(64)                              NOT NULL,
  category_id    INTEGER                                            NOT NULL,
  name           CHARACTER VARYING(256)                             NOT NULL,
  attention_text TEXT,
  created        TIMESTAMP WITH TIME ZONE DEFAULT current_timestamp NOT NULL,
  updated        TIMESTAMP WITH TIME ZONE,
  geometry       GEOMETRY                                           NOT NULL,
  place_desc     TEXT,
  link           TEXT,
  image_url      CHARACTER VARYING(512),
  CONSTRAINT "my_places_pKey" PRIMARY KEY (id)
);


CREATE OR REPLACE VIEW my_places_categories AS
  SELECT
    mp.id,
    mp.uuid,
    mp.category_id,
    mp.name,
    mp.attention_text,
    mp.place_desc,
    mp.created,
    mp.updated,
    mp.geometry,
    c.category_name,
    c."default",
    c.stroke_width,
    c.stroke_color,
    c.fill_color,
    c.dot_color,
    c.dot_size,
    c.dot_shape,
    c.border_width,
    c.border_color,
    c.publisher_name,
    mp.link,
    mp.image_url,
    c.fill_pattern,
    c.stroke_linejoin,
    c.stroke_linecap,
    c.stroke_dasharray,
    c.border_linejoin,
    c.border_dasharray
  FROM my_places mp, categories c
  WHERE mp.category_id = c.id;
