--
-- PostgreSQL database dump
--

-- Dumped from database version 9.3.1
-- Dumped by pg_dump version 9.3.0
-- Started on 2014-03-20 15:00:57

SET statement_timeout = 0;
SET lock_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;

--
-- TOC entry 191 (class 3079 OID 12617)
-- Name: plpgsql; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS plpgsql WITH SCHEMA pg_catalog;


--
-- TOC entry 4094 (class 0 OID 0)
-- Dependencies: 191
-- Name: EXTENSION plpgsql; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION plpgsql IS 'PL/pgSQL procedural language';


--
-- TOC entry 192 (class 3079 OID 478364)
-- Name: postgis; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS postgis WITH SCHEMA public;


--
-- TOC entry 4095 (class 0 OID 0)
-- Dependencies: 192
-- Name: EXTENSION postgis; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION postgis IS 'PostGIS geometry, geography, and raster spatial types and functions';


SET search_path = public, pg_catalog;

--
-- TOC entry 1257 (class 1255 OID 479701)
-- Name: procedure_user_layer_data_update(); Type: FUNCTION; Schema: public; Owner: liferay
--

CREATE FUNCTION procedure_user_layer_data_update() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
	IF (TG_OP = 'UPDATE') THEN
		NEW.updated := current_timestamp;
	RETURN NEW;
	ELSIF (TG_OP = 'INSERT') THEN
		NEW.created := current_timestamp;
	RETURN NEW;
	END IF;
	RETURN NEW;
END;
$$;

--
-- TOC entry 1256 (class 1255 OID 479659)
-- Name: procedure_user_layer_update(); Type: FUNCTION; Schema: public; Owner: liferay
--

CREATE FUNCTION procedure_user_layer_update() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
	IF (TG_OP = 'UPDATE') THEN
		NEW.updated := current_timestamp;
	RETURN NEW;
	ELSIF (TG_OP = 'INSERT') THEN
		NEW.created := current_timestamp;
	RETURN NEW;
	END IF;
	RETURN NEW;
END;
$$;


SET default_tablespace = '';

SET default_with_oids = false;

--
-- TOC entry 186 (class 1259 OID 479686)
-- Name: user_layer; Type: TABLE; Schema: public; Owner: liferay; Tablespace: 
--

CREATE TABLE user_layer (
    id bigint NOT NULL,
    uuid character varying(64),
    layer_name character varying(256) NOT NULL,
    layer_desc character varying(256),
    layer_source character varying(256),
    publisher_name character varying(256),
    style_id bigint,
    created timestamp with time zone NOT NULL,
    updated timestamp with time zone,
    fields json
);


--
-- TOC entry 188 (class 1259 OID 479704)
-- Name: user_layer_data; Type: TABLE; Schema: public; Owner: liferay; Tablespace: 
--

CREATE TABLE user_layer_data (
    id bigint NOT NULL,
    user_layer_id bigint NOT NULL,
    uuid character varying(64),
    feature_id character varying(64),
    property_json json,
    geometry geometry NOT NULL,
    created timestamp with time zone NOT NULL,
    updated timestamp with time zone
);

--
-- TOC entry 187 (class 1259 OID 479702)
-- Name: user_layer_data_id_seq; Type: SEQUENCE; Schema: public; Owner: liferay
--

CREATE SEQUENCE user_layer_data_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 4098 (class 0 OID 0)
-- Dependencies: 187
-- Name: user_layer_data_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: liferay
--

ALTER SEQUENCE user_layer_data_id_seq OWNED BY user_layer_data.id;


--
-- TOC entry 184 (class 1259 OID 479673)
-- Name: user_layer_style; Type: TABLE; Schema: public; Owner: liferay; Tablespace: 
--

CREATE TABLE user_layer_style (
    id bigint NOT NULL,
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
    border_dasharray character varying(256)
);

--
-- TOC entry 189 (class 1259 OID 479719)
-- Name: user_layer_data_style; Type: VIEW; Schema: public; Owner: liferay
--

CREATE VIEW user_layer_data_style AS
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
  WHERE ((ad.user_layer_id = a.id) AND (a.style_id = st.id));

--
-- TOC entry 185 (class 1259 OID 479684)
-- Name: user_layer_id_seq; Type: SEQUENCE; Schema: public; Owner: liferay
--

CREATE SEQUENCE user_layer_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 4101 (class 0 OID 0)
-- Dependencies: 185
-- Name: user_layer_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: liferay
--

ALTER SEQUENCE user_layer_id_seq OWNED BY user_layer.id;


--
-- TOC entry 183 (class 1259 OID 479671)
-- Name: user_layer_style_id_seq; Type: SEQUENCE; Schema: public; Owner: liferay
--

CREATE SEQUENCE user_layer_style_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 4102 (class 0 OID 0)
-- Dependencies: 183
-- Name: user_layer_style_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: liferay
--

ALTER SEQUENCE user_layer_style_id_seq OWNED BY user_layer_style.id;


--
-- TOC entry 190 (class 1259 OID 479782)
-- Name: vuser_layer_data; Type: VIEW; Schema: public; Owner: liferay
--

CREATE VIEW vuser_layer_data AS
 SELECT user_layer_data.id, 
    user_layer_data.uuid, 
    user_layer_data.user_layer_id, 
    user_layer_data.feature_id, 
    (user_layer_data.property_json)::text AS property_json, 
    user_layer_data.created, 
    user_layer_data.updated, 
    user_layer_data.geometry
   FROM user_layer_data;


--
-- TOC entry 3959 (class 2604 OID 479689)
-- Name: id; Type: DEFAULT; Schema: public; Owner: liferay
--

ALTER TABLE ONLY user_layer ALTER COLUMN id SET DEFAULT nextval('user_layer_id_seq'::regclass);


--
-- TOC entry 3960 (class 2604 OID 479707)
-- Name: id; Type: DEFAULT; Schema: public; Owner: liferay
--

ALTER TABLE ONLY user_layer_data ALTER COLUMN id SET DEFAULT nextval('user_layer_data_id_seq'::regclass);


--
-- TOC entry 3956 (class 2604 OID 479676)
-- Name: id; Type: DEFAULT; Schema: public; Owner: liferay
--

ALTER TABLE ONLY user_layer_style ALTER COLUMN id SET DEFAULT nextval('user_layer_style_id_seq'::regclass);


--
-- TOC entry 3966 (class 2606 OID 479712)
-- Name: user_layer_data_pKey; Type: CONSTRAINT; Schema: public; Owner: liferay; Tablespace: 
--

ALTER TABLE ONLY user_layer_data
    ADD CONSTRAINT "user_layer_data_pKey" PRIMARY KEY (id);


--
-- TOC entry 3964 (class 2606 OID 479694)
-- Name: user_layer_pkey; Type: CONSTRAINT; Schema: public; Owner: liferay; Tablespace: 
--

ALTER TABLE ONLY user_layer
    ADD CONSTRAINT user_layer_pkey PRIMARY KEY (id);


--
-- TOC entry 3962 (class 2606 OID 479683)
-- Name: user_layer_style_pkey; Type: CONSTRAINT; Schema: public; Owner: liferay; Tablespace: 
--

ALTER TABLE ONLY user_layer_style
    ADD CONSTRAINT user_layer_style_pkey PRIMARY KEY (id);


--
-- TOC entry 3969 (class 2620 OID 479700)
-- Name: trigger_user_layer; Type: TRIGGER; Schema: public; Owner: liferay
--

CREATE TRIGGER trigger_user_layer BEFORE INSERT OR UPDATE ON user_layer FOR EACH ROW EXECUTE PROCEDURE procedure_user_layer_update();


--
-- TOC entry 3970 (class 2620 OID 479718)
-- Name: trigger_user_layer_update; Type: TRIGGER; Schema: public; Owner: liferay
--

CREATE TRIGGER trigger_user_layer_update BEFORE INSERT OR UPDATE ON user_layer_data FOR EACH ROW EXECUTE PROCEDURE procedure_user_layer_data_update();


--
-- TOC entry 3968 (class 2606 OID 479713)
-- Name: user_layer_data_user_layer_fkey; Type: FK CONSTRAINT; Schema: public; Owner: liferay
--

ALTER TABLE ONLY user_layer_data
    ADD CONSTRAINT user_layer_data_user_layer_fkey FOREIGN KEY (user_layer_id) REFERENCES user_layer(id) ON DELETE CASCADE;


--
-- TOC entry 3967 (class 2606 OID 479695)
-- Name: user_layer_style_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: liferay
--

ALTER TABLE ONLY user_layer
    ADD CONSTRAINT user_layer_style_id_fkey FOREIGN KEY (style_id) REFERENCES user_layer_style(id);

