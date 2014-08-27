
-- Analysis tables for storage of Oskari analysis results;
-- Configure analysis_data and analysis_data_style tables for GeoServer; 


DROP VIEW IF EXISTS analysis_data_style;
DROP TABLE IF EXISTS analysis_data;
DROP TABLE IF EXISTS analysis;
DROP TABLE IF EXISTS analysis_style;

CREATE TABLE analysis (
    id bigserial NOT NULL,
    uuid character varying(64),
    name character varying(256) NOT NULL,
    layer_id integer,
    analyse_json text,
    style_id bigint,
    col1 character varying(64),
    col2 character varying(64),
    col3 character varying(64),
    col4 character varying(64),
    col5 character varying(64),
    col6 character varying(64),
    col7 character varying(64),
    col8 character varying(64),
    col10 character varying(64),
    select_to_data text,
    created timestamp with time zone,
    updated timestamp with time zone,
    col9 character varying(64),
    publisher_name character varying(256),
    override_sld character varying(256),
    CONSTRAINT analysis_pkey PRIMARY KEY (id)
);

CREATE TABLE analysis_data (
    id bigserial NOT NULL,
    analysis_id bigint NOT NULL,
    uuid character varying(64),
    t1 text,
    t2 text,
    t3 text,
    t4 text,
    t5 text,
    t6 text,
    t7 text,
    t8 text,
    n1 numeric,
    n2 numeric,
    n3 numeric,
    n4 numeric,
    n5 numeric,
    n6 numeric,
    n7 numeric,
    n8 numeric,
    d1 date,
    d2 date,
    d3 date,
    d4 date,
    geometry geometry NOT NULL,
    created timestamp with time zone,
    updated timestamp with time zone,
    CONSTRAINT analysis_data_pkey PRIMARY KEY (id)
);


CREATE TABLE analysis_style (
    id bigserial NOT NULL,
    stroke_width integer,
    stroke_color character(7),
    fill_color character(7),
    dot_color character(7),
    dot_size integer,
    border_width integer,
    border_color character(7),
    dot_shape character varying(20) DEFAULT '8'::character varying NOT NULL,
    stroke_linejoin character varying(256),
    fill_pattern integer DEFAULT (-1),
    stroke_linecap character varying(256),
    stroke_dasharray character varying(256),
    border_linejoin character varying(256),
    border_dasharray character varying(256),
    CONSTRAINT analysis_style_pkey PRIMARY KEY (id)
);

CREATE OR REPLACE VIEW analysis_data_style AS 
 SELECT ad.id, 
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
    ADD CONSTRAINT analysis_data_analysis_fkey FOREIGN KEY (analysis_id) REFERENCES analysis(id) ON DELETE CASCADE;




ALTER TABLE ONLY analysis
    ADD CONSTRAINT analysis_style_id_fkey FOREIGN KEY (style_id) REFERENCES analysis_style(id);



