-- user data store tables for storage of Oskari kmz, shp, .. file import results;
-- Configure user_layer_data and user_layer_data_style tables for GeoServer;

-- Create primary key table for GeoServer;
CREATE TABLE IF NOT EXISTS gt_pk_metadata_table
(
  table_schema  CHARACTER VARYING(32) NOT NULL,
  table_name    CHARACTER VARYING(32) NOT NULL,
  pk_column     CHARACTER VARYING(32) NOT NULL,
  pk_column_idx INTEGER,
  pk_policy     CHARACTER VARYING(32),
  pk_sequence   CHARACTER VARYING(64),
  CONSTRAINT gt_pk_metadata_table_table_schema_table_name_pk_column_key UNIQUE (table_schema, table_name, pk_column)
)
WITH (
OIDS =FALSE
);

-- Table: user_layer_style;
CREATE TABLE IF NOT EXISTS user_layer_style
(
  id               BIGSERIAL             NOT NULL,
  stroke_width     INTEGER,
  stroke_color     CHARACTER(7),
  fill_color       CHARACTER(7),
  dot_color        CHARACTER(7),
  dot_size         INTEGER,
  border_width     INTEGER,
  border_color     CHARACTER(7),
  dot_shape        CHARACTER VARYING(20) NOT NULL DEFAULT '8' :: CHARACTER VARYING,
  stroke_linejoin  CHARACTER VARYING(256),
  fill_pattern     INTEGER                        DEFAULT (-1),
  stroke_linecap   CHARACTER VARYING(256),
  stroke_dasharray CHARACTER VARYING(256),
  border_linejoin  CHARACTER VARYING(256),
  border_dasharray CHARACTER VARYING(256),
  CONSTRAINT user_layer_style_pkey PRIMARY KEY (id)
)
WITH (
OIDS =FALSE
);


-- Table: user_layer;
CREATE TABLE IF NOT EXISTS user_layer
(
  id             BIGSERIAL                                          NOT NULL,
  uuid           CHARACTER VARYING(64),
  layer_name     CHARACTER VARYING(256)                             NOT NULL,
  layer_desc     CHARACTER VARYING(256),
  layer_source   CHARACTER VARYING(256),
  publisher_name CHARACTER VARYING(256),
  style_id       BIGINT,
  created        TIMESTAMP WITH TIME ZONE DEFAULT current_timestamp NOT NULL,
  updated        TIMESTAMP WITH TIME ZONE,
  fields         JSON,
  CONSTRAINT user_layer_pkey PRIMARY KEY (id),
  CONSTRAINT user_layer_style_id_fkey FOREIGN KEY (style_id)
  REFERENCES user_layer_style (id) MATCH SIMPLE
  ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
OIDS =FALSE
);


-- Table: user_layer_data;
CREATE TABLE IF NOT EXISTS user_layer_data
(
  id            BIGSERIAL                                          NOT NULL,
  user_layer_id BIGINT                                             NOT NULL,
  uuid          CHARACTER VARYING(64),
  feature_id    CHARACTER VARYING(64),
  property_json JSON,
  geometry      GEOMETRY                                           NOT NULL,
  created       TIMESTAMP WITH TIME ZONE DEFAULT current_timestamp NOT NULL,
  updated       TIMESTAMP WITH TIME ZONE,
  CONSTRAINT "user_layer_data_pKey" PRIMARY KEY (id),
  CONSTRAINT user_layer_data_user_layer_fkey FOREIGN KEY (user_layer_id)
  REFERENCES user_layer (id) MATCH SIMPLE
  ON UPDATE NO ACTION ON DELETE CASCADE
)
WITH (
OIDS =FALSE
);

-- View: user_layer_data_style;
CREATE OR REPLACE VIEW user_layer_data_style AS
  SELECT
    ad.id,
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
  SELECT
    user_layer_data.id,
    user_layer_data.uuid,
    user_layer_data.user_layer_id,
    user_layer_data.feature_id,
    user_layer_data.property_json :: TEXT AS property_json,
    user_layer_data.created,
    user_layer_data.updated,
    user_layer_data.geometry
  FROM user_layer_data;



