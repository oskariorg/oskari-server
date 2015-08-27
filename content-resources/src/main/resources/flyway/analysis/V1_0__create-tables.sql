-- Analysis tables for storage of Oskari analysis results;
-- Configure analysis_data and analysis_data_style tables for GeoServer;


CREATE TABLE IF NOT EXISTS analysis (
  id             BIGSERIAL              NOT NULL,
  uuid           CHARACTER VARYING(64),
  name           CHARACTER VARYING(256) NOT NULL,
  layer_id       INTEGER,
  analyse_json   TEXT,
  style_id       BIGINT,
  col1           CHARACTER VARYING(64),
  col2           CHARACTER VARYING(64),
  col3           CHARACTER VARYING(64),
  col4           CHARACTER VARYING(64),
  col5           CHARACTER VARYING(64),
  col6           CHARACTER VARYING(64),
  col7           CHARACTER VARYING(64),
  col8           CHARACTER VARYING(64),
  col10          CHARACTER VARYING(64),
  select_to_data TEXT,
  created        TIMESTAMP WITH TIME ZONE DEFAULT current_timestamp,
  updated        TIMESTAMP WITH TIME ZONE,
  col9           CHARACTER VARYING(64),
  publisher_name CHARACTER VARYING(256),
  override_sld   CHARACTER VARYING(256),
  CONSTRAINT analysis_pkey PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS analysis_data (
  id          BIGSERIAL NOT NULL,
  analysis_id BIGINT    NOT NULL,
  uuid        CHARACTER VARYING(64),
  t1          TEXT,
  t2          TEXT,
  t3          TEXT,
  t4          TEXT,
  t5          TEXT,
  t6          TEXT,
  t7          TEXT,
  t8          TEXT,
  n1          NUMERIC,
  n2          NUMERIC,
  n3          NUMERIC,
  n4          NUMERIC,
  n5          NUMERIC,
  n6          NUMERIC,
  n7          NUMERIC,
  n8          NUMERIC,
  d1          DATE,
  d2          DATE,
  d3          DATE,
  d4          DATE,
  geometry    GEOMETRY  NOT NULL,
  created     TIMESTAMP WITH TIME ZONE DEFAULT current_timestamp,
  updated     TIMESTAMP WITH TIME ZONE,
  CONSTRAINT analysis_data_pkey PRIMARY KEY (id)
);


CREATE TABLE IF NOT EXISTS analysis_style (
  id               BIGSERIAL                                              NOT NULL,
  stroke_width     INTEGER,
  stroke_color     CHARACTER(7),
  fill_color       CHARACTER(7),
  dot_color        CHARACTER(7),
  dot_size         INTEGER,
  border_width     INTEGER,
  border_color     CHARACTER(7),
  dot_shape        CHARACTER VARYING(20) DEFAULT '8' :: CHARACTER VARYING NOT NULL,
  stroke_linejoin  CHARACTER VARYING(256),
  fill_pattern     INTEGER DEFAULT (-1),
  stroke_linecap   CHARACTER VARYING(256),
  stroke_dasharray CHARACTER VARYING(256),
  border_linejoin  CHARACTER VARYING(256),
  border_dasharray CHARACTER VARYING(256),
  CONSTRAINT analysis_style_pkey PRIMARY KEY (id)
);

CREATE OR REPLACE VIEW analysis_data_style AS
  SELECT
    ad.id,
    ad.uuid,
    ad.analysis_id,
    a.name,
    a.publisher_name,
    ad.t1,
    ad.n1,
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
  FROM analysis_data ad,
    analysis a,
    analysis_style st
  WHERE ad.analysis_id = a.id AND a.style_id = st.id;

ALTER TABLE ONLY analysis_data
  DROP CONSTRAINT IF EXISTS analysis_data_analysis_fkey;

ALTER TABLE ONLY analysis_data
  ADD CONSTRAINT analysis_data_analysis_fkey FOREIGN KEY (analysis_id) REFERENCES analysis (id) ON DELETE CASCADE;

ALTER TABLE ONLY analysis
  DROP CONSTRAINT IF EXISTS analysis_style_id_fkey;

ALTER TABLE ONLY analysis
  ADD CONSTRAINT analysis_style_id_fkey FOREIGN KEY (style_id) REFERENCES analysis_style (id);



