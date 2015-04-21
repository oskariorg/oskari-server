
-- user data store tables for storage of Oskari kmz, shp, .. file import results;
-- Configure user_layer_data and user_layer_data_style tables for GeoServer;

DROP TABLE IF EXISTS gt_pk_metadata_table;
DROP VIEW IF EXISTS user_layer_data_style;
DROP VIEW IF EXISTS vuser_layer_data;

DROP TABLE IF EXISTS user_layer_data;
DROP TABLE IF EXISTS user_layer;
DROP TABLE IF EXISTS user_layer_style;

-- Create primary key table for GeoServer;
CREATE TABLE gt_pk_metadata_table
(
  table_schema character varying(32) NOT NULL,
  table_name character varying(32) NOT NULL,
  pk_column character varying(32) NOT NULL,
  pk_column_idx integer,
  pk_policy character varying(32),
  pk_sequence character varying(64),
  CONSTRAINT gt_pk_metadata_table_table_schema_table_name_pk_column_key UNIQUE (table_schema, table_name, pk_column)
)
WITH (
OIDS=FALSE
);

INSERT INTO gt_pk_metadata_table(
  table_schema, table_name, pk_column, pk_column_idx, pk_policy,
  pk_sequence)
  VALUES (
    'public',
    'vuser_layer_data',
    'id',
    null,
    'assigned',
    null);

-- Table: user_layer_style;
CREATE TABLE user_layer_style
(
  id bigserial NOT NULL,
  stroke_width integer,
  stroke_color character(7),
  fill_color character(7),
  dot_color character(7),
  dot_size integer,
  border_width integer,
  border_color character(7),
  dot_shape character varying(20) NOT NULL DEFAULT '8'::character varying,
  stroke_linejoin character varying(256),
  fill_pattern integer DEFAULT (-1),
  stroke_linecap character varying(256),
  stroke_dasharray character varying(256),
  border_linejoin character varying(256),
  border_dasharray character varying(256),
  CONSTRAINT user_layer_style_pkey PRIMARY KEY (id)
)
WITH (
  OIDS=FALSE
);


-- Table: user_layer;
CREATE TABLE user_layer
(
  id bigserial NOT NULL,
  uuid character varying(64),
  layer_name character varying(256) NOT NULL,
  layer_desc character varying(256),
  layer_source character varying(256),
  publisher_name character varying(256),
  style_id bigint,
  created timestamp with time zone NOT NULL,
  updated timestamp with time zone,
  fields json,
  CONSTRAINT user_layer_pkey PRIMARY KEY (id),
  CONSTRAINT user_layer_style_id_fkey FOREIGN KEY (style_id)
      REFERENCES user_layer_style (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);


-- Table: user_layer_data;
CREATE TABLE user_layer_data
(
  id bigserial NOT NULL,
  user_layer_id bigint NOT NULL,
  uuid character varying(64),
  feature_id character varying(64),
  property_json json,
  geometry geometry NOT NULL,
  created timestamp with time zone NOT NULL,
  updated timestamp with time zone,
  CONSTRAINT "user_layer_data_pKey" PRIMARY KEY (id),
  CONSTRAINT user_layer_data_user_layer_fkey FOREIGN KEY (user_layer_id)
      REFERENCES user_layer (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE CASCADE
)
WITH (
  OIDS=FALSE
);

-- View: user_layer_data_style;
CREATE OR REPLACE VIEW user_layer_data_style AS 
 SELECT ad.id, 
    ad.uuid, 
    ad.user_layer_id, 
    a.layer_name, 
    a.publisher_name, 
    ad.feature_id, 
    ad.created, 
    ad.updated, 
    ad.geometry, 
    st.stroke_width, 
    st.stroke_color, 
    st.fill_color, 
    st.dot_color, 
    st.dot_size, 
    st.dot_shape, 
    st.border_width, 
    st.border_color, 
    st.fill_pattern, 
    st.stroke_linejoin, 
    st.stroke_linecap, 
    st.stroke_dasharray, 
    st.border_linejoin, 
    st.border_dasharray
   FROM user_layer_data ad, 
    user_layer a, 
    user_layer_style st
  WHERE ad.user_layer_id = a.id AND a.style_id = st.id;


-- View: vuser_layer_data;
CREATE OR REPLACE VIEW vuser_layer_data AS
 SELECT user_layer_data.id, 
    user_layer_data.uuid, 
    user_layer_data.user_layer_id, 
    user_layer_data.feature_id, 
    user_layer_data.property_json::text AS property_json, 
    user_layer_data.created, 
    user_layer_data.updated, 
    user_layer_data.geometry
   FROM user_layer_data;



