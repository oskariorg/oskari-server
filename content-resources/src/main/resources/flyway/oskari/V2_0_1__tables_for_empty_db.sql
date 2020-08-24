--
-- PostgreSQL database dump
--

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET client_min_messages = warning;
SET row_security = off;


SET default_with_oids = false;


--
-- TOC entry 225 (class 1259 OID 24500)
-- Name: oskari_backendstatus; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.oskari_backendstatus (
    ts timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    maplayer_id integer NOT NULL,
    status character varying(500),
    statusmessage character varying(2000),
    infourl character varying(2000)
);


--
-- TOC entry 4750 (class 0 OID 0)
-- Dependencies: 225
-- Name: TABLE oskari_backendstatus; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.oskari_backendstatus IS 'Results of layer data source availability probes';


--
-- TOC entry 249 (class 1259 OID 24826)
-- Name: oskari_capabilities_cache; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.oskari_capabilities_cache (
    id bigint NOT NULL,
    layertype character varying(64) NOT NULL,
    url character varying(2048) NOT NULL,
    data text NOT NULL,
    created timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    updated timestamp with time zone,
    version text DEFAULT ''::text NOT NULL
);


--
-- TOC entry 4751 (class 0 OID 0)
-- Dependencies: 249
-- Name: TABLE oskari_capabilities_cache; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.oskari_capabilities_cache IS 'Cache of GetCapabilities results for WMS/WMTS layers';


--
-- TOC entry 248 (class 1259 OID 24824)
-- Name: oskari_capabilities_cache_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.oskari_capabilities_cache_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 4752 (class 0 OID 0)
-- Dependencies: 248
-- Name: oskari_capabilities_cache_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.oskari_capabilities_cache_id_seq OWNED BY public.oskari_capabilities_cache.id;


--
-- TOC entry 215 (class 1259 OID 24412)
-- Name: oskari_dataprovider; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.oskari_dataprovider (
    id integer NOT NULL,
    locale text DEFAULT '{}'::text
);


--
-- TOC entry 4753 (class 0 OID 0)
-- Dependencies: 215
-- Name: TABLE oskari_dataprovider; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.oskari_dataprovider IS 'Layer data provider name localizations';


--
-- TOC entry 246 (class 1259 OID 24794)
-- Name: oskari_jaas_users; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.oskari_jaas_users (
    id integer NOT NULL,
    login text NOT NULL,
    password text NOT NULL
);


--
-- TOC entry 4754 (class 0 OID 0)
-- Dependencies: 246
-- Name: TABLE oskari_jaas_users; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.oskari_jaas_users IS 'Credentials for users when using built-in login';


--
-- TOC entry 245 (class 1259 OID 24792)
-- Name: oskari_jaas_users_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.oskari_jaas_users_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 4755 (class 0 OID 0)
-- Dependencies: 245
-- Name: oskari_jaas_users_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.oskari_jaas_users_id_seq OWNED BY public.oskari_jaas_users.id;


--
-- TOC entry 214 (class 1259 OID 24410)
-- Name: oskari_layergroup_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.oskari_layergroup_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 4756 (class 0 OID 0)
-- Dependencies: 214
-- Name: oskari_layergroup_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.oskari_layergroup_id_seq OWNED BY public.oskari_dataprovider.id;


--
-- TOC entry 217 (class 1259 OID 24424)
-- Name: oskari_maplayer; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.oskari_maplayer (
    id integer NOT NULL,
    parentid integer DEFAULT '-1'::integer NOT NULL,
    type character varying(50) NOT NULL,
    base_map boolean DEFAULT false NOT NULL,
    dataprovider_id integer,
    name character varying(2000),
    url character varying(2000),
    locale text,
    opacity integer DEFAULT 100,
    style character varying(100),
    minscale double precision DEFAULT '-1'::integer,
    maxscale double precision DEFAULT '-1'::integer,
    legend_image character varying(2000),
    metadataid character varying(200),
    params text DEFAULT '{}'::text,
    options text DEFAULT '{}'::text,
    gfi_type character varying(200),
    gfi_xslt text,
    gfi_content text,
    realtime boolean DEFAULT false,
    refresh_rate integer DEFAULT 0,
    created timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    updated timestamp with time zone,
    username character varying(256),
    password character varying(256),
    srs_name character varying,
    version character varying(64) DEFAULT ''::character varying NOT NULL,
    attributes text DEFAULT '{}'::text,
    capabilities text DEFAULT '{}'::text,
    capabilities_last_updated timestamp with time zone,
    capabilities_update_rate_sec integer DEFAULT 0,
    internal boolean DEFAULT false NOT NULL
);


--
-- TOC entry 4757 (class 0 OID 0)
-- Dependencies: 217
-- Name: TABLE oskari_maplayer; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.oskari_maplayer IS 'Map layers configuration';


--
-- TOC entry 264 (class 1259 OID 25025)
-- Name: oskari_maplayer_externalid; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.oskari_maplayer_externalid (
    maplayerid integer NOT NULL,
    externalid character varying(50) NOT NULL
);


--
-- TOC entry 4758 (class 0 OID 0)
-- Dependencies: 264
-- Name: TABLE oskari_maplayer_externalid; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.oskari_maplayer_externalid IS 'Legacy "external id" associated with map layer';


--
-- TOC entry 213 (class 1259 OID 24401)
-- Name: oskari_maplayer_group; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.oskari_maplayer_group (
    id integer NOT NULL,
    locale character varying(20000),
    parentid integer DEFAULT '-1'::integer NOT NULL,
    selectable boolean DEFAULT true NOT NULL,
    order_number integer DEFAULT 1000000
);


--
-- TOC entry 4759 (class 0 OID 0)
-- Dependencies: 213
-- Name: TABLE oskari_maplayer_group; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.oskari_maplayer_group IS 'Logical group for layers';


--
-- TOC entry 218 (class 1259 OID 24451)
-- Name: oskari_maplayer_group_link; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.oskari_maplayer_group_link (
    maplayerid integer NOT NULL,
    groupid integer NOT NULL,
    order_number integer DEFAULT 1000000
);


--
-- TOC entry 4760 (class 0 OID 0)
-- Dependencies: 218
-- Name: TABLE oskari_maplayer_group_link; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.oskari_maplayer_group_link IS 'Bridge table between map layer and its group';


--
-- TOC entry 216 (class 1259 OID 24422)
-- Name: oskari_maplayer_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.oskari_maplayer_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 4761 (class 0 OID 0)
-- Dependencies: 216
-- Name: oskari_maplayer_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.oskari_maplayer_id_seq OWNED BY public.oskari_maplayer.id;


--
-- TOC entry 220 (class 1259 OID 24466)
-- Name: oskari_maplayer_metadata; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.oskari_maplayer_metadata (
    id integer NOT NULL,
    metadataid character varying(256),
    wkt character varying(512) DEFAULT ''::character varying,
    json text DEFAULT ''::text,
    ts timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


--
-- TOC entry 4762 (class 0 OID 0)
-- Dependencies: 220
-- Name: TABLE oskari_maplayer_metadata; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.oskari_maplayer_metadata IS 'Metadata about map layers';


--
-- TOC entry 219 (class 1259 OID 24464)
-- Name: oskari_maplayer_metadata_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.oskari_maplayer_metadata_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 4763 (class 0 OID 0)
-- Dependencies: 219
-- Name: oskari_maplayer_metadata_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.oskari_maplayer_metadata_id_seq OWNED BY public.oskari_maplayer_metadata.id;


--
-- TOC entry 251 (class 1259 OID 24838)
-- Name: oskari_maplayer_projections; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.oskari_maplayer_projections (
    id bigint NOT NULL,
    name character varying(64) NOT NULL,
    maplayerid integer NOT NULL
);


--
-- TOC entry 4764 (class 0 OID 0)
-- Dependencies: 251
-- Name: TABLE oskari_maplayer_projections; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.oskari_maplayer_projections IS 'Supported projections (EPSG-codes) for map layers';


--
-- TOC entry 250 (class 1259 OID 24836)
-- Name: oskari_maplayer_projections_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.oskari_maplayer_projections_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 4765 (class 0 OID 0)
-- Dependencies: 250
-- Name: oskari_maplayer_projections_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.oskari_maplayer_projections_id_seq OWNED BY public.oskari_maplayer_projections.id;


--
-- TOC entry 224 (class 1259 OID 24491)
-- Name: oskari_permission; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.oskari_permission (
    id integer NOT NULL,
    oskari_resource_id bigint NOT NULL,
    external_type character varying(100),
    permission character varying(100),
    external_id character varying(1000)
);


--
-- TOC entry 4766 (class 0 OID 0)
-- Dependencies: 224
-- Name: TABLE oskari_permission; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.oskari_permission IS 'Permissions for resources';


--
-- TOC entry 223 (class 1259 OID 24489)
-- Name: oskari_permission_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.oskari_permission_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 4767 (class 0 OID 0)
-- Dependencies: 223
-- Name: oskari_permission_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.oskari_permission_id_seq OWNED BY public.oskari_permission.id;


--
-- TOC entry 222 (class 1259 OID 24480)
-- Name: oskari_resource; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.oskari_resource (
    id integer NOT NULL,
    resource_type character varying(100) NOT NULL,
    resource_mapping character varying(1000) NOT NULL
);


--
-- TOC entry 4768 (class 0 OID 0)
-- Dependencies: 222
-- Name: TABLE oskari_resource; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.oskari_resource IS 'Declares resources representing a map layer or functionality that permissions are linked to';


--
-- TOC entry 221 (class 1259 OID 24478)
-- Name: oskari_resource_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.oskari_resource_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 4769 (class 0 OID 0)
-- Dependencies: 221
-- Name: oskari_resource_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.oskari_resource_id_seq OWNED BY public.oskari_resource.id;


--
-- TOC entry 244 (class 1259 OID 24783)
-- Name: oskari_role_external_mapping; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.oskari_role_external_mapping (
    roleid bigint NOT NULL,
    name character varying(50) NOT NULL,
    external_type character varying(50) DEFAULT ''::character varying NOT NULL
);


--
-- TOC entry 4770 (class 0 OID 0)
-- Dependencies: 244
-- Name: TABLE oskari_role_external_mapping; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.oskari_role_external_mapping IS 'For mapping roles from external system to Oskari roles (requires custom code to use)';


--
-- TOC entry 243 (class 1259 OID 24767)
-- Name: oskari_role_oskari_user; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.oskari_role_oskari_user (
    id integer NOT NULL,
    role_id integer,
    user_id bigint
);


--
-- TOC entry 4771 (class 0 OID 0)
-- Dependencies: 243
-- Name: TABLE oskari_role_oskari_user; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.oskari_role_oskari_user IS 'Bridge table connecting role and user';


--
-- TOC entry 242 (class 1259 OID 24765)
-- Name: oskari_role_oskari_user_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.oskari_role_oskari_user_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 4772 (class 0 OID 0)
-- Dependencies: 242
-- Name: oskari_role_oskari_user_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.oskari_role_oskari_user_id_seq OWNED BY public.oskari_role_oskari_user.id;


--
-- TOC entry 241 (class 1259 OID 24756)
-- Name: oskari_roles; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.oskari_roles (
    id integer NOT NULL,
    name text NOT NULL,
    is_guest boolean DEFAULT false
);


--
-- TOC entry 4773 (class 0 OID 0)
-- Dependencies: 241
-- Name: TABLE oskari_roles; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.oskari_roles IS 'Roles that have associated permissions';


--
-- TOC entry 240 (class 1259 OID 24754)
-- Name: oskari_roles_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.oskari_roles_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 4774 (class 0 OID 0)
-- Dependencies: 240
-- Name: oskari_roles_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.oskari_roles_id_seq OWNED BY public.oskari_roles.id;


--
-- TOC entry 258 (class 1259 OID 24888)
-- Name: oskari_statistical_datasource; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.oskari_statistical_datasource (
    id bigint NOT NULL,
    locale text DEFAULT '{}'::text NOT NULL,
    config text DEFAULT '{}'::text,
    plugin text NOT NULL
);


--
-- TOC entry 4775 (class 0 OID 0)
-- Dependencies: 258
-- Name: TABLE oskari_statistical_datasource; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.oskari_statistical_datasource IS 'Data source for statistical data (thematic maps)';


--
-- TOC entry 257 (class 1259 OID 24886)
-- Name: oskari_statistical_datasource_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.oskari_statistical_datasource_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 4776 (class 0 OID 0)
-- Dependencies: 257
-- Name: oskari_statistical_datasource_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.oskari_statistical_datasource_id_seq OWNED BY public.oskari_statistical_datasource.id;


--
-- TOC entry 259 (class 1259 OID 24899)
-- Name: oskari_statistical_layer; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.oskari_statistical_layer (
    datasource_id integer NOT NULL,
    layer_id integer NOT NULL,
    config text DEFAULT '{}'::text
);


--
-- TOC entry 4777 (class 0 OID 0)
-- Dependencies: 259
-- Name: TABLE oskari_statistical_layer; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.oskari_statistical_layer IS 'Link map layers with region geometry to statistical data sources (based on data available on the data source)';


--
-- TOC entry 231 (class 1259 OID 24597)
-- Name: oskari_terms_of_use_for_publishing; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.oskari_terms_of_use_for_publishing (
    userid bigint NOT NULL,
    agreed boolean DEFAULT false NOT NULL,
    "time" timestamp with time zone
);


--
-- TOC entry 4778 (class 0 OID 0)
-- Dependencies: 231
-- Name: TABLE oskari_terms_of_use_for_publishing; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.oskari_terms_of_use_for_publishing IS 'Approval of terms for publishing by user';


--
-- TOC entry 233 (class 1259 OID 24692)
-- Name: oskari_user_indicator; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.oskari_user_indicator (
    id integer NOT NULL,
    user_id bigint,
    title character varying(1000),
    source character varying(1000),
    description character varying(1000),
    published boolean
);


--
-- TOC entry 4779 (class 0 OID 0)
-- Dependencies: 233
-- Name: TABLE oskari_user_indicator; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.oskari_user_indicator IS 'Metadata for statistical indicators created by users';


--
-- TOC entry 263 (class 1259 OID 24995)
-- Name: oskari_user_indicator_data; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.oskari_user_indicator_data (
    id integer NOT NULL,
    indicator_id integer NOT NULL,
    regionset_id integer NOT NULL,
    year integer NOT NULL,
    data text NOT NULL
);


--
-- TOC entry 4780 (class 0 OID 0)
-- Dependencies: 263
-- Name: TABLE oskari_user_indicator_data; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.oskari_user_indicator_data IS 'Data for statistical indicators created by users';


--
-- TOC entry 262 (class 1259 OID 24993)
-- Name: oskari_user_indicator_data_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.oskari_user_indicator_data_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 4781 (class 0 OID 0)
-- Dependencies: 262
-- Name: oskari_user_indicator_data_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.oskari_user_indicator_data_id_seq OWNED BY public.oskari_user_indicator_data.id;


--
-- TOC entry 232 (class 1259 OID 24690)
-- Name: oskari_user_indicator_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.oskari_user_indicator_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 4782 (class 0 OID 0)
-- Dependencies: 232
-- Name: oskari_user_indicator_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.oskari_user_indicator_id_seq OWNED BY public.oskari_user_indicator.id;


--
-- TOC entry 239 (class 1259 OID 24740)
-- Name: oskari_users; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.oskari_users (
    id integer NOT NULL,
    user_name character varying(128) NOT NULL,
    first_name character varying(128),
    last_name character varying(128),
    email character varying(256),
    uuid character varying(64),
    attributes text DEFAULT '{}'::text
);


--
-- TOC entry 4783 (class 0 OID 0)
-- Dependencies: 239
-- Name: TABLE oskari_users; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.oskari_users IS 'Oskari instance user accounts';


--
-- TOC entry 238 (class 1259 OID 24738)
-- Name: oskari_users_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.oskari_users_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 4784 (class 0 OID 0)
-- Dependencies: 238
-- Name: oskari_users_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.oskari_users_id_seq OWNED BY public.oskari_users.id;


--
-- TOC entry 261 (class 1259 OID 24927)
-- Name: oskari_users_pending; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.oskari_users_pending (
    id bigint NOT NULL,
    user_name text,
    email text,
    uuid text,
    expiry_timestamp timestamp with time zone
);


--
-- TOC entry 4785 (class 0 OID 0)
-- Dependencies: 261
-- Name: TABLE oskari_users_pending; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.oskari_users_pending IS 'Users that have started registration process but not completed it yet (used when end-user registration is enabled)';


--
-- TOC entry 260 (class 1259 OID 24925)
-- Name: oskari_users_pending_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.oskari_users_pending_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 4786 (class 0 OID 0)
-- Dependencies: 260
-- Name: oskari_users_pending_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.oskari_users_pending_id_seq OWNED BY public.oskari_users_pending.id;


--
-- TOC entry 253 (class 1259 OID 24849)
-- Name: oskari_wfs_search_channels; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.oskari_wfs_search_channels (
    id bigint NOT NULL,
    wfs_layer_id integer NOT NULL,
    params_for_search text NOT NULL,
    is_default boolean,
    locale text DEFAULT '{}'::text,
    config text DEFAULT '{}'::text
);


--
-- TOC entry 4787 (class 0 OID 0)
-- Dependencies: 253
-- Name: TABLE oskari_wfs_search_channels; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.oskari_wfs_search_channels IS 'Configuration for using WFS-services as search services';


--
-- TOC entry 252 (class 1259 OID 24847)
-- Name: oskari_wfs_search_channels_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.oskari_wfs_search_channels_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 4788 (class 0 OID 0)
-- Dependencies: 252
-- Name: oskari_wfs_search_channels_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.oskari_wfs_search_channels_id_seq OWNED BY public.oskari_wfs_search_channels.id;


--
-- TOC entry 229 (class 1259 OID 24546)
-- Name: portti_bundle; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.portti_bundle (
    id bigint NOT NULL,
    name character varying(128) NOT NULL,
    config character varying(20000) DEFAULT '{}'::character varying,
    state character varying(20000) DEFAULT '{}'::character varying,
    startup character varying(20000),
    CONSTRAINT nullchk CHECK ((startup IS NULL))
);


--
-- TOC entry 4789 (class 0 OID 0)
-- Dependencies: 229
-- Name: TABLE portti_bundle; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.portti_bundle IS 'List of available front-end functionality modules';


--
-- TOC entry 4790 (class 0 OID 0)
-- Dependencies: 229
-- Name: COLUMN portti_bundle.startup; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.portti_bundle.startup IS 'Deprecated column, always NULL';


--
-- TOC entry 228 (class 1259 OID 24544)
-- Name: portti_bundle_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.portti_bundle_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 4791 (class 0 OID 0)
-- Dependencies: 228
-- Name: portti_bundle_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.portti_bundle_id_seq OWNED BY public.portti_bundle.id;


--
-- TOC entry 212 (class 1259 OID 24399)
-- Name: portti_inspiretheme_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.portti_inspiretheme_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 4792 (class 0 OID 0)
-- Dependencies: 212
-- Name: portti_inspiretheme_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.portti_inspiretheme_id_seq OWNED BY public.oskari_maplayer_group.id;


--
-- TOC entry 237 (class 1259 OID 24723)
-- Name: portti_keyword_association; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.portti_keyword_association (
    keyid1 bigint NOT NULL,
    keyid2 bigint NOT NULL,
    type character varying(10)
);


--
-- TOC entry 4793 (class 0 OID 0)
-- Dependencies: 237
-- Name: TABLE portti_keyword_association; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.portti_keyword_association IS 'Conceptual linking of keywords';


--
-- TOC entry 235 (class 1259 OID 24701)
-- Name: portti_keywords; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.portti_keywords (
    id integer NOT NULL,
    keyword character varying(2000),
    uri character varying(2000),
    lang character varying(10),
    editable boolean
);


--
-- TOC entry 4794 (class 0 OID 0)
-- Dependencies: 235
-- Name: TABLE portti_keywords; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.portti_keywords IS 'Keywords that can be associated with resources';


--
-- TOC entry 234 (class 1259 OID 24699)
-- Name: portti_keywords_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.portti_keywords_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 4795 (class 0 OID 0)
-- Dependencies: 234
-- Name: portti_keywords_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.portti_keywords_id_seq OWNED BY public.portti_keywords.id;


--
-- TOC entry 236 (class 1259 OID 24710)
-- Name: portti_layer_keywords; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.portti_layer_keywords (
    keyid bigint NOT NULL,
    layerid bigint NOT NULL
);


--
-- TOC entry 4796 (class 0 OID 0)
-- Dependencies: 236
-- Name: TABLE portti_layer_keywords; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.portti_layer_keywords IS 'Bridge table linking map layers and keywords describing them';


--
-- TOC entry 227 (class 1259 OID 24520)
-- Name: portti_view; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.portti_view (
    uuid uuid,
    id bigint NOT NULL,
    name character varying(128) NOT NULL,
    is_default boolean DEFAULT false,
    type character varying(16) DEFAULT 'USER'::character varying,
    description character varying(2000),
    page character varying(128) DEFAULT 'index'::character varying,
    application character varying(128) DEFAULT 'servlet'::character varying,
    application_dev_prefix character varying(256) DEFAULT '/applications/sample'::character varying,
    only_uuid boolean DEFAULT false,
    creator bigint DEFAULT '-1'::integer,
    domain character varying(512) DEFAULT ''::character varying,
    lang character varying(2) DEFAULT 'en'::character varying,
    is_public boolean DEFAULT false,
    metadata text DEFAULT '{}'::text,
    old_id bigint DEFAULT '-1'::integer,
    created timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    used timestamp without time zone DEFAULT now() NOT NULL,
    usagecount bigint DEFAULT 0 NOT NULL
);


--
-- TOC entry 4797 (class 0 OID 0)
-- Dependencies: 227
-- Name: TABLE portti_view; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.portti_view IS 'Map views/appsetups';


--
-- TOC entry 230 (class 1259 OID 24559)
-- Name: portti_view_bundle_seq; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.portti_view_bundle_seq (
    view_id bigint NOT NULL,
    bundle_id bigint NOT NULL,
    seqno integer NOT NULL,
    config text DEFAULT '{}'::text,
    state text DEFAULT '{}'::text,
    startup text,
    bundleinstance character varying(128) DEFAULT ''::character varying,
    CONSTRAINT nullchk CHECK ((startup IS NULL))
);


--
-- TOC entry 4798 (class 0 OID 0)
-- Dependencies: 230
-- Name: TABLE portti_view_bundle_seq; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.portti_view_bundle_seq IS 'Bundles present in a view/appsetup and their loading order';


--
-- TOC entry 4799 (class 0 OID 0)
-- Dependencies: 230
-- Name: COLUMN portti_view_bundle_seq.startup; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.portti_view_bundle_seq.startup IS 'Deprecated column, always NULL';


--
-- TOC entry 226 (class 1259 OID 24518)
-- Name: portti_view_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.portti_view_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 4800 (class 0 OID 0)
-- Dependencies: 226
-- Name: portti_view_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.portti_view_id_seq OWNED BY public.portti_view.id;


--
-- TOC entry 256 (class 1259 OID 24863)
-- Name: ratings; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.ratings (
    id bigint NOT NULL,
    userid bigint NOT NULL,
    rating integer,
    category character varying(64) NOT NULL,
    categoryitem character varying(64) NOT NULL,
    comment character varying(1024),
    userrole character varying(64),
    created timestamp without time zone DEFAULT now() NOT NULL
);


--
-- TOC entry 4801 (class 0 OID 0)
-- Dependencies: 256
-- Name: TABLE ratings; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.ratings IS 'Table for rating metadata/anything really (not used currently)';


--
-- TOC entry 254 (class 1259 OID 24859)
-- Name: ratings_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.ratings_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 4802 (class 0 OID 0)
-- Dependencies: 254
-- Name: ratings_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.ratings_id_seq OWNED BY public.ratings.id;


--
-- TOC entry 255 (class 1259 OID 24861)
-- Name: ratings_userid_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.ratings_userid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 4803 (class 0 OID 0)
-- Dependencies: 255
-- Name: ratings_userid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.ratings_userid_seq OWNED BY public.ratings.userid;


--
-- TOC entry 4448 (class 2604 OID 24829)
-- Name: oskari_capabilities_cache id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.oskari_capabilities_cache ALTER COLUMN id SET DEFAULT nextval('public.oskari_capabilities_cache_id_seq'::regclass);


--
-- TOC entry 4387 (class 2604 OID 24415)
-- Name: oskari_dataprovider id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.oskari_dataprovider ALTER COLUMN id SET DEFAULT nextval('public.oskari_layergroup_id_seq'::regclass);


--
-- TOC entry 4446 (class 2604 OID 24797)
-- Name: oskari_jaas_users id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.oskari_jaas_users ALTER COLUMN id SET DEFAULT nextval('public.oskari_jaas_users_id_seq'::regclass);


--
-- TOC entry 4389 (class 2604 OID 24427)
-- Name: oskari_maplayer id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.oskari_maplayer ALTER COLUMN id SET DEFAULT nextval('public.oskari_maplayer_id_seq'::regclass);


--
-- TOC entry 4383 (class 2604 OID 24404)
-- Name: oskari_maplayer_group id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.oskari_maplayer_group ALTER COLUMN id SET DEFAULT nextval('public.portti_inspiretheme_id_seq'::regclass);


--
-- TOC entry 4406 (class 2604 OID 24469)
-- Name: oskari_maplayer_metadata id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.oskari_maplayer_metadata ALTER COLUMN id SET DEFAULT nextval('public.oskari_maplayer_metadata_id_seq'::regclass);


--
-- TOC entry 4451 (class 2604 OID 24841)
-- Name: oskari_maplayer_projections id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.oskari_maplayer_projections ALTER COLUMN id SET DEFAULT nextval('public.oskari_maplayer_projections_id_seq'::regclass);


--
-- TOC entry 4411 (class 2604 OID 24494)
-- Name: oskari_permission id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.oskari_permission ALTER COLUMN id SET DEFAULT nextval('public.oskari_permission_id_seq'::regclass);


--
-- TOC entry 4410 (class 2604 OID 24483)
-- Name: oskari_resource id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.oskari_resource ALTER COLUMN id SET DEFAULT nextval('public.oskari_resource_id_seq'::regclass);


--
-- TOC entry 4444 (class 2604 OID 24770)
-- Name: oskari_role_oskari_user id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.oskari_role_oskari_user ALTER COLUMN id SET DEFAULT nextval('public.oskari_role_oskari_user_id_seq'::regclass);


--
-- TOC entry 4442 (class 2604 OID 24759)
-- Name: oskari_roles id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.oskari_roles ALTER COLUMN id SET DEFAULT nextval('public.oskari_roles_id_seq'::regclass);


--
-- TOC entry 4458 (class 2604 OID 24891)
-- Name: oskari_statistical_datasource id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.oskari_statistical_datasource ALTER COLUMN id SET DEFAULT nextval('public.oskari_statistical_datasource_id_seq'::regclass);


--
-- TOC entry 4438 (class 2604 OID 24695)
-- Name: oskari_user_indicator id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.oskari_user_indicator ALTER COLUMN id SET DEFAULT nextval('public.oskari_user_indicator_id_seq'::regclass);


--
-- TOC entry 4463 (class 2604 OID 24998)
-- Name: oskari_user_indicator_data id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.oskari_user_indicator_data ALTER COLUMN id SET DEFAULT nextval('public.oskari_user_indicator_data_id_seq'::regclass);


--
-- TOC entry 4440 (class 2604 OID 24743)
-- Name: oskari_users id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.oskari_users ALTER COLUMN id SET DEFAULT nextval('public.oskari_users_id_seq'::regclass);


--
-- TOC entry 4462 (class 2604 OID 24930)
-- Name: oskari_users_pending id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.oskari_users_pending ALTER COLUMN id SET DEFAULT nextval('public.oskari_users_pending_id_seq'::regclass);


--
-- TOC entry 4452 (class 2604 OID 24852)
-- Name: oskari_wfs_search_channels id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.oskari_wfs_search_channels ALTER COLUMN id SET DEFAULT nextval('public.oskari_wfs_search_channels_id_seq'::regclass);


--
-- TOC entry 4429 (class 2604 OID 24549)
-- Name: portti_bundle id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.portti_bundle ALTER COLUMN id SET DEFAULT nextval('public.portti_bundle_id_seq'::regclass);


--
-- TOC entry 4439 (class 2604 OID 24704)
-- Name: portti_keywords id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.portti_keywords ALTER COLUMN id SET DEFAULT nextval('public.portti_keywords_id_seq'::regclass);


--
-- TOC entry 4413 (class 2604 OID 24523)
-- Name: portti_view id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.portti_view ALTER COLUMN id SET DEFAULT nextval('public.portti_view_id_seq'::regclass);


--
-- TOC entry 4455 (class 2604 OID 24866)
-- Name: ratings id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ratings ALTER COLUMN id SET DEFAULT nextval('public.ratings_id_seq'::regclass);


--
-- TOC entry 4456 (class 2604 OID 24867)
-- Name: ratings userid; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ratings ALTER COLUMN userid SET DEFAULT nextval('public.ratings_userid_seq'::regclass);

--
-- TOC entry 4532 (class 2606 OID 24921)
-- Name: oskari_capabilities_cache oskari_capabilities_cache__unique_service; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.oskari_capabilities_cache
    ADD CONSTRAINT oskari_capabilities_cache__unique_service UNIQUE (layertype, version, url);


--
-- TOC entry 4534 (class 2606 OID 24835)
-- Name: oskari_capabilities_cache oskari_capabilities_cache_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.oskari_capabilities_cache
    ADD CONSTRAINT oskari_capabilities_cache_pkey PRIMARY KEY (id);


--
-- TOC entry 4525 (class 2606 OID 24939)
-- Name: oskari_jaas_users oskari_jaas_users_login_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.oskari_jaas_users
    ADD CONSTRAINT oskari_jaas_users_login_key UNIQUE (login);


--
-- TOC entry 4527 (class 2606 OID 24799)
-- Name: oskari_jaas_users oskari_jaas_users_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.oskari_jaas_users
    ADD CONSTRAINT oskari_jaas_users_pkey PRIMARY KEY (id);


--
-- TOC entry 4481 (class 2606 OID 24421)
-- Name: oskari_dataprovider oskari_layergroup_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.oskari_dataprovider
    ADD CONSTRAINT oskari_layergroup_pkey PRIMARY KEY (id);


--
-- TOC entry 4554 (class 2606 OID 25036)
-- Name: oskari_maplayer_externalid oskari_maplayer_externalid_externalid_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.oskari_maplayer_externalid
    ADD CONSTRAINT oskari_maplayer_externalid_externalid_key UNIQUE (externalid);


--
-- TOC entry 4556 (class 2606 OID 25029)
-- Name: oskari_maplayer_externalid oskari_maplayer_externalid_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.oskari_maplayer_externalid
    ADD CONSTRAINT oskari_maplayer_externalid_pkey PRIMARY KEY (maplayerid);


--
-- TOC entry 4487 (class 2606 OID 24477)
-- Name: oskari_maplayer_metadata oskari_maplayer_metadata_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.oskari_maplayer_metadata
    ADD CONSTRAINT oskari_maplayer_metadata_pkey PRIMARY KEY (id);


--
-- TOC entry 4483 (class 2606 OID 24443)
-- Name: oskari_maplayer oskari_maplayer_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.oskari_maplayer
    ADD CONSTRAINT oskari_maplayer_pkey PRIMARY KEY (id);


--
-- TOC entry 4538 (class 2606 OID 24881)
-- Name: oskari_maplayer_projections oskari_maplayer_projections_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.oskari_maplayer_projections
    ADD CONSTRAINT oskari_maplayer_projections_pkey PRIMARY KEY (id);


--
-- TOC entry 4494 (class 2606 OID 24823)
-- Name: oskari_permission oskari_permission_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.oskari_permission
    ADD CONSTRAINT oskari_permission_pkey PRIMARY KEY (id);


--
-- TOC entry 4490 (class 2606 OID 25043)
-- Name: oskari_resource oskari_resource_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.oskari_resource
    ADD CONSTRAINT oskari_resource_pk PRIMARY KEY (id);


--
-- TOC entry 4523 (class 2606 OID 24772)
-- Name: oskari_role_oskari_user oskari_role_oskari_user_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.oskari_role_oskari_user
    ADD CONSTRAINT oskari_role_oskari_user_pkey PRIMARY KEY (id);


--
-- TOC entry 4519 (class 2606 OID 24957)
-- Name: oskari_roles oskari_roles_name_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.oskari_roles
    ADD CONSTRAINT oskari_roles_name_key UNIQUE (name);


--
-- TOC entry 4521 (class 2606 OID 24762)
-- Name: oskari_roles oskari_roles_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.oskari_roles
    ADD CONSTRAINT oskari_roles_pkey PRIMARY KEY (id);


--
-- TOC entry 4544 (class 2606 OID 24898)
-- Name: oskari_statistical_datasource oskari_statistical_datasource_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.oskari_statistical_datasource
    ADD CONSTRAINT oskari_statistical_datasource_pkey PRIMARY KEY (id);


--
-- TOC entry 4546 (class 2606 OID 24908)
-- Name: oskari_statistical_layer oskari_statistical_layer_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.oskari_statistical_layer
    ADD CONSTRAINT oskari_statistical_layer_pkey PRIMARY KEY (datasource_id, layer_id);


--
-- TOC entry 4550 (class 2606 OID 25005)
-- Name: oskari_user_indicator_data oskari_user_indicator_data_indicator_year; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.oskari_user_indicator_data
    ADD CONSTRAINT oskari_user_indicator_data_indicator_year UNIQUE (indicator_id, regionset_id, year);


--
-- TOC entry 4552 (class 2606 OID 25003)
-- Name: oskari_user_indicator_data oskari_user_indicator_data_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.oskari_user_indicator_data
    ADD CONSTRAINT oskari_user_indicator_data_pkey PRIMARY KEY (id);


--
-- TOC entry 4507 (class 2606 OID 24987)
-- Name: oskari_user_indicator oskari_user_indicator_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.oskari_user_indicator
    ADD CONSTRAINT oskari_user_indicator_pkey PRIMARY KEY (id);


--
-- TOC entry 4548 (class 2606 OID 24935)
-- Name: oskari_users_pending oskari_users_pending_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.oskari_users_pending
    ADD CONSTRAINT oskari_users_pending_pkey PRIMARY KEY (id);


--
-- TOC entry 4513 (class 2606 OID 24749)
-- Name: oskari_users oskari_users_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.oskari_users
    ADD CONSTRAINT oskari_users_pkey PRIMARY KEY (id);


--
-- TOC entry 4515 (class 2606 OID 24751)
-- Name: oskari_users oskari_users_user_name_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.oskari_users
    ADD CONSTRAINT oskari_users_user_name_key UNIQUE (user_name);


--
-- TOC entry 4517 (class 2606 OID 24753)
-- Name: oskari_users oskari_users_uuid_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.oskari_users
    ADD CONSTRAINT oskari_users_uuid_key UNIQUE (uuid);


--
-- TOC entry 4501 (class 2606 OID 24558)
-- Name: portti_bundle portti_bundle_name_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.portti_bundle
    ADD CONSTRAINT portti_bundle_name_key UNIQUE (name);


--
-- TOC entry 4503 (class 2606 OID 24556)
-- Name: portti_bundle portti_bundle_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.portti_bundle
    ADD CONSTRAINT portti_bundle_pkey PRIMARY KEY (id);


--
-- TOC entry 4479 (class 2606 OID 24409)
-- Name: oskari_maplayer_group portti_inspiretheme_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.oskari_maplayer_group
    ADD CONSTRAINT portti_inspiretheme_pkey PRIMARY KEY (id);


--
-- TOC entry 4509 (class 2606 OID 24709)
-- Name: portti_keywords portti_keywords_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.portti_keywords
    ADD CONSTRAINT portti_keywords_pkey PRIMARY KEY (id);


--
-- TOC entry 4497 (class 2606 OID 24541)
-- Name: portti_view portti_view_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.portti_view
    ADD CONSTRAINT portti_view_pkey PRIMARY KEY (id);


--
-- TOC entry 4499 (class 2606 OID 24543)
-- Name: portti_view portti_view_uuid_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.portti_view
    ADD CONSTRAINT portti_view_uuid_key UNIQUE (uuid);


--
-- TOC entry 4540 (class 2606 OID 24857)
-- Name: oskari_wfs_search_channels portti_wfs_search_channels_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.oskari_wfs_search_channels
    ADD CONSTRAINT portti_wfs_search_channels_pkey PRIMARY KEY (id);


--
-- TOC entry 4542 (class 2606 OID 24872)
-- Name: ratings ratings_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ratings
    ADD CONSTRAINT ratings_pkey PRIMARY KEY (id);


--
-- TOC entry 4492 (class 2606 OID 24488)
-- Name: oskari_resource type_mapping; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.oskari_resource
    ADD CONSTRAINT type_mapping UNIQUE (resource_type, resource_mapping);


--
-- TOC entry 4511 (class 2606 OID 24727)
-- Name: portti_keyword_association unique_all_columns; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.portti_keyword_association
    ADD CONSTRAINT unique_all_columns UNIQUE (keyid1, keyid2, type);


--
-- TOC entry 4505 (class 2606 OID 25038)
-- Name: portti_view_bundle_seq view_seq; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.portti_view_bundle_seq
    ADD CONSTRAINT view_seq UNIQUE (view_id, seqno);


--
-- TOC entry 4535 (class 1259 OID 24883)
-- Name: oskari_maplayer_projections_maplayerid_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX oskari_maplayer_projections_maplayerid_idx ON public.oskari_maplayer_projections USING btree (maplayerid);


--
-- TOC entry 4536 (class 1259 OID 24882)
-- Name: oskari_maplayer_projections_name_index; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX oskari_maplayer_projections_name_index ON public.oskari_maplayer_projections USING btree (name);


--
-- TOC entry 4484 (class 1259 OID 24449)
-- Name: oskari_maplayer_q1; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX oskari_maplayer_q1 ON public.oskari_maplayer USING btree (parentid);


--
-- TOC entry 4485 (class 1259 OID 24450)
-- Name: oskari_maplayer_q2; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX oskari_maplayer_q2 ON public.oskari_maplayer USING btree (dataprovider_id);


--
-- TOC entry 4495 (class 1259 OID 24885)
-- Name: oskari_permission_resid_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX oskari_permission_resid_idx ON public.oskari_permission USING btree (oskari_resource_id);


--
-- TOC entry 4488 (class 1259 OID 24884)
-- Name: oskari_resource_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX oskari_resource_idx ON public.oskari_resource USING btree (resource_type, resource_mapping);

--
-- TOC entry 4589 (class 2606 OID 25016)
-- Name: oskari_backendstatus oskari_backendstatus_maplayer_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.oskari_backendstatus
    ADD CONSTRAINT oskari_backendstatus_maplayer_id_fkey FOREIGN KEY (maplayer_id) REFERENCES public.oskari_maplayer(id) ON DELETE CASCADE;


--
-- TOC entry 4593 (class 2606 OID 24713)
-- Name: portti_layer_keywords oskari_layer_keywords_layerid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.portti_layer_keywords
    ADD CONSTRAINT oskari_layer_keywords_layerid_fkey FOREIGN KEY (layerid) REFERENCES public.oskari_maplayer(id) ON DELETE CASCADE;


--
-- TOC entry 4605 (class 2606 OID 25030)
-- Name: oskari_maplayer_externalid oskari_maplayer_externalid_maplayerid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.oskari_maplayer_externalid
    ADD CONSTRAINT oskari_maplayer_externalid_maplayerid_fkey FOREIGN KEY (maplayerid) REFERENCES public.oskari_maplayer(id) ON DELETE CASCADE;


--
-- TOC entry 4585 (class 2606 OID 24444)
-- Name: oskari_maplayer oskari_maplayer_groupid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.oskari_maplayer
    ADD CONSTRAINT oskari_maplayer_groupid_fkey FOREIGN KEY (dataprovider_id) REFERENCES public.oskari_dataprovider(id) ON DELETE CASCADE;


--
-- TOC entry 4586 (class 2606 OID 24454)
-- Name: oskari_maplayer_group_link oskari_maplayer_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.oskari_maplayer_group_link
    ADD CONSTRAINT oskari_maplayer_id_fkey FOREIGN KEY (maplayerid) REFERENCES public.oskari_maplayer(id) ON DELETE CASCADE;


--
-- TOC entry 4600 (class 2606 OID 24842)
-- Name: oskari_maplayer_projections oskari_maplayer_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.oskari_maplayer_projections
    ADD CONSTRAINT oskari_maplayer_id_fkey FOREIGN KEY (maplayerid) REFERENCES public.oskari_maplayer(id) ON DELETE CASCADE;


--
-- TOC entry 4588 (class 2606 OID 25044)
-- Name: oskari_permission oskari_resource_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.oskari_permission
    ADD CONSTRAINT oskari_resource_id_fkey FOREIGN KEY (oskari_resource_id) REFERENCES public.oskari_resource(id) ON DELETE CASCADE;


--
-- TOC entry 4599 (class 2606 OID 24787)
-- Name: oskari_role_external_mapping oskari_role_external_mapping_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.oskari_role_external_mapping
    ADD CONSTRAINT oskari_role_external_mapping_fkey FOREIGN KEY (roleid) REFERENCES public.oskari_roles(id) ON DELETE CASCADE;


--
-- TOC entry 4597 (class 2606 OID 24940)
-- Name: oskari_role_oskari_user oskari_role_oskari_user_role_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.oskari_role_oskari_user
    ADD CONSTRAINT oskari_role_oskari_user_role_id_fkey FOREIGN KEY (role_id) REFERENCES public.oskari_roles(id) ON DELETE CASCADE;


--
-- TOC entry 4598 (class 2606 OID 24945)
-- Name: oskari_role_oskari_user oskari_role_oskari_user_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.oskari_role_oskari_user
    ADD CONSTRAINT oskari_role_oskari_user_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.oskari_users(id) ON DELETE CASCADE;


--
-- TOC entry 4601 (class 2606 OID 24909)
-- Name: oskari_statistical_layer oskari_statistical_layer_datasource_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.oskari_statistical_layer
    ADD CONSTRAINT oskari_statistical_layer_datasource_id_fkey FOREIGN KEY (datasource_id) REFERENCES public.oskari_statistical_datasource(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- TOC entry 4602 (class 2606 OID 24914)
-- Name: oskari_statistical_layer oskari_statistical_layer_layer_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.oskari_statistical_layer
    ADD CONSTRAINT oskari_statistical_layer_layer_id_fkey FOREIGN KEY (layer_id) REFERENCES public.oskari_maplayer(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- TOC entry 4603 (class 2606 OID 25006)
-- Name: oskari_user_indicator_data oskari_user_indicator_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.oskari_user_indicator_data
    ADD CONSTRAINT oskari_user_indicator_id_fkey FOREIGN KEY (indicator_id) REFERENCES public.oskari_user_indicator(id) ON DELETE CASCADE;


--
-- TOC entry 4604 (class 2606 OID 25011)
-- Name: oskari_user_indicator_data oskari_user_indicator_regionset_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.oskari_user_indicator_data
    ADD CONSTRAINT oskari_user_indicator_regionset_fkey FOREIGN KEY (regionset_id) REFERENCES public.oskari_maplayer(id);


--
-- TOC entry 4592 (class 2606 OID 24988)
-- Name: oskari_user_indicator oskari_user_indicator_user_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.oskari_user_indicator
    ADD CONSTRAINT oskari_user_indicator_user_fk FOREIGN KEY (user_id) REFERENCES public.oskari_users(id) ON DELETE CASCADE;


--
-- TOC entry 4587 (class 2606 OID 24459)
-- Name: oskari_maplayer_group_link portti_inspiretheme_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.oskari_maplayer_group_link
    ADD CONSTRAINT portti_inspiretheme_id_fkey FOREIGN KEY (groupid) REFERENCES public.oskari_maplayer_group(id) ON DELETE CASCADE;


--
-- TOC entry 4595 (class 2606 OID 24728)
-- Name: portti_keyword_association portti_keyword_association_keyid1_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.portti_keyword_association
    ADD CONSTRAINT portti_keyword_association_keyid1_fkey FOREIGN KEY (keyid1) REFERENCES public.portti_keywords(id) ON DELETE CASCADE;


--
-- TOC entry 4596 (class 2606 OID 24733)
-- Name: portti_keyword_association portti_keyword_association_keyid2_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.portti_keyword_association
    ADD CONSTRAINT portti_keyword_association_keyid2_fkey FOREIGN KEY (keyid2) REFERENCES public.portti_keywords(id) ON DELETE CASCADE;


--
-- TOC entry 4594 (class 2606 OID 24718)
-- Name: portti_layer_keywords portti_layer_keywords_keyid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.portti_layer_keywords
    ADD CONSTRAINT portti_layer_keywords_keyid_fkey FOREIGN KEY (keyid) REFERENCES public.portti_keywords(id) ON DELETE CASCADE;


--
-- TOC entry 4590 (class 2606 OID 24570)
-- Name: portti_view_bundle_seq portti_view_bundle_seq_bundle_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.portti_view_bundle_seq
    ADD CONSTRAINT portti_view_bundle_seq_bundle_id_fkey FOREIGN KEY (bundle_id) REFERENCES public.portti_bundle(id);


--
-- TOC entry 4591 (class 2606 OID 24966)
-- Name: portti_view_bundle_seq portti_view_bundle_seq_view_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.portti_view_bundle_seq
    ADD CONSTRAINT portti_view_bundle_seq_view_id_fkey FOREIGN KEY (view_id) REFERENCES public.portti_view(id) ON DELETE CASCADE;


--
-- PostgreSQL database dump complete
--

