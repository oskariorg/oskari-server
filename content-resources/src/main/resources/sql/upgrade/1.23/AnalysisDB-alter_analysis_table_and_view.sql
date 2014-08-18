-- database: analysis


ALTER TABLE analysis

  ADD COLUMN override_sld character varying(256);

  -- View: analysis_data_style

DROP VIEW analysis_data_style;

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

ALTER TABLE analysis_data_style
  OWNER TO liferay;
GRANT ALL ON TABLE analysis_data_style TO liferay;
GRANT SELECT ON TABLE analysis_data_style TO omat_paikat;
