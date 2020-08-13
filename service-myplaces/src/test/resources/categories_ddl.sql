CREATE TABLE categories
(
    id bigint BIGSERIAL,
    category_name character varying(256) NOT NULL,
    "default" boolean,
    stroke_width integer DEFAULT 1,
    stroke_color character(7),
    fill_color character(7),
    uuid character varying(64),
    dot_color character(7) DEFAULT '#00FF00'::character(7),
    dot_size integer DEFAULT 3,
    border_width integer,
    border_color character(7),
    publisher_name character varying(256),
    dot_shape character varying(20) NOT NULL DEFAULT '1'::character varying,
    stroke_linejoin character varying(256),
    fill_pattern integer DEFAULT '-1'::integer,
    stroke_linecap character varying(256),
    stroke_dasharray character varying(256),
    border_linejoin character varying(256),
    border_dasharray character varying(256),
    options json,
    CONSTRAINT categories_pkey PRIMARY KEY (id)
)