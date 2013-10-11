-- NOTE!;
-- THE FILE IS TOKENIZED WITH SEMICOLON CHARACTER!;
-- EACH COMMENT _NEED_ TO END WITH A SEMICOLON OR OTHERWISE THE NEXT ACTUAL SQL IS NOT RUN!;
-- ----------------------------------------------------------------------------------------;

DROP TABLE IF EXISTS portti_inspiretheme;
DROP TABLE IF EXISTS portti_layerclass;
DROP TABLE IF EXISTS portti_maplayer;
DROP TABLE IF EXISTS portti_permissions;
DROP TABLE IF EXISTS portti_resource_user;
DROP TABLE IF EXISTS portti_maplayer_metadata;
DROP TABLE IF EXISTS portti_capabilities_cache;

DROP VIEW IF EXISTS portti_backendalert;
DROP VIEW IF EXISTS portti_backendstatus_allknown;
DROP TABLE IF EXISTS portti_backendstatus;

DROP TABLE IF EXISTS portti_view_bundle_seq;
DROP TABLE IF EXISTS portti_bundle;
DROP TABLE IF EXISTS portti_view;
DROP TABLE IF EXISTS portti_view_supplement;

DROP TABLE IF EXISTS portti_terms_of_use_for_publishing;

CREATE TABLE portti_capabilities_cache
(
  layer_id serial NOT NULL,
  data character varying(20000),
  updated timestamp DEFAULT CURRENT_TIMESTAMP,
  "WMSversion" character(10) NOT NULL,
  CONSTRAINT portti_capabilities_cache_pkey PRIMARY KEY (layer_id)
);

CREATE TABLE portti_inspiretheme (
  id serial NOT NULL,
  locale character varying(20000),
  CONSTRAINT portti_inspiretheme_pkey PRIMARY KEY (id)
);


CREATE TABLE portti_layerclass (
  id serial NOT NULL,
  maplayers_selectable boolean DEFAULT false,
  parent integer DEFAULT null,
  legend_image character varying(2000) DEFAULT '',
  dataurl character varying(2000) DEFAULT '',
  group_map boolean DEFAULT false,
  locale character varying(20000),
  CONSTRAINT portti_layerclass_pkey PRIMARY KEY (id)

);


CREATE TABLE portti_maplayer (
  id serial NOT NULL,
  layerclassid integer,
  wmsname character varying(2000) default '',
  wmsurl character varying(2000) default '',
  opacity integer default 100,
  style character varying(20000) default '',
  minscale double precision default 0,
  maxscale double precision default 0,
  description_link character varying(2000) default '',
  legend_image character varying(2000) default '',
  inspire_theme_id integer default 0,
  dataurl character varying(2000) default '',
  metadataurl character varying(2000) default '',
  order_number integer default 0,
  layer_type character varying(100) NOT NULL,
  tile_matrix_set_id character varying(1024) default '',
  tile_matrix_set_data character varying(20000) default '',
  created timestamp DEFAULT CURRENT_TIMESTAMP,
  updated timestamp DEFAULT CURRENT_TIMESTAMP,
  wms_dcp_http character varying(2000) default '',
  wms_parameter_layers character varying(2000) default '',
  resource_url_scheme character varying(100) default '',
  resource_url_scheme_pattern character varying(2000) default '',
  resource_url_client_pattern character varying(2000) default '',
  resource_daily_max_per_ip integer default -1,
  xslt character varying(20000) default '',
  gfi_type character varying(2000) default '',
  selection_style character varying(20000) default '',
  "version" character varying(10) default '',
  epsg integer DEFAULT 3067,
  locale character varying(20000),
  CONSTRAINT portti_maplayer_pkey PRIMARY KEY (id)

);



CREATE TABLE portti_permissions (
  id serial NOT NULL,
  resource_user_id integer NOT NULL,
  permissions_type character varying(100),
  CONSTRAINT portti_permissions_pkey PRIMARY KEY (id)
);


CREATE TABLE portti_resource_user (
  id serial NOT NULL,
  resource_name character varying(1000),
  resource_namespace character varying(1000),
  resource_type character varying(100),
  externalid character varying(1000),
  externalid_type character varying(20),
  CONSTRAINT portti_resource_user_pkey PRIMARY KEY (id)

);

CREATE TABLE portti_maplayer_metadata
(
  id serial NOT NULL,
  maplayerid integer,
  uuid character varying(256),
  namefi character varying(512),
  namesv character varying(512),
  nameen character varying(512),
  abstractfi character varying(1024),
  abstractsv character varying(1024),
  abstracten character varying(1024),
  browsegraphic character varying(1024),
  geom character varying(512) default '',
  CONSTRAINT portti_maplayer_metadata_pkey PRIMARY KEY (id)
);


CREATE TABLE portti_backendstatus
(
  id serial NOT NULL,
  ts timestamp DEFAULT CURRENT_TIMESTAMP,
  maplayer_id character varying(50),
  status character varying(500),
  statusmessage character varying(2000),
  infourl character varying(2000),
  statusjson character varying(20000),
  CONSTRAINT portti_backendstatus_pkey PRIMARY KEY (id)
);

CREATE VIEW portti_backendalert as SELECT id,ts,maplayer_id,status,statusmessage,infourl,statusjson FROM portti_backendstatus WHERE NOT status is null AND NOT status = 'UNKNOWN' AND NOT status = 'OK';

CREATE VIEW portti_backendstatus_allknown AS
  SELECT portti_backendstatus.id, portti_backendstatus.ts, portti_backendstatus.maplayer_id, portti_backendstatus.status, portti_backendstatus.statusmessage, portti_backendstatus.infourl, portti_backendstatus.statusjson
  FROM portti_backendstatus;



CREATE TABLE portti_view_supplement (
   id               bigserial NOT NULL,
   creator          BIGINT        DEFAULT -1,
   pubdomain        VARCHAR(512)  DEFAULT '',
   lang             VARCHAR(2)    DEFAULT 'en',
   width            INTEGER       DEFAULT 0,
   height           INTEGER       DEFAULT 0,
   is_public        BOOLEAN       DEFAULT FALSE,
   old_id	    BIGINT	  DEFAULT -1,
  CONSTRAINT portti_view_supplement_pkey PRIMARY KEY (id)
);


CREATE TABLE portti_view (
   uuid             VARCHAR(128),
   id               bigserial NOT NULL,
   name             VARCHAR(128)  NOT NULL,
   supplement_id    BIGINT        ,
   is_default       BOOLEAN       DEFAULT FALSE,
   type		    varchar(16)	  DEFAULT 'USER',
   description   VARCHAR(2000) ,
   page character varying(128) DEFAULT 'index',
   application character varying(128) DEFAULT 'servlet',
   application_dev_prefix character varying(256) DEFAULT '/applications/sample',
  CONSTRAINT portti_view_pkey PRIMARY KEY (id),
  CONSTRAINT portti_view_supplement_id_fkey FOREIGN KEY (supplement_id)
  REFERENCES portti_view_supplement (id) MATCH SIMPLE
);



CREATE TABLE portti_bundle (
   id    	    bigserial NOT NULL,
   name 	    VARCHAR(128)  NOT NULL,
   config 	    character varying(20000) DEFAULT '{}',
   state 	    character varying(20000) DEFAULT '{}',
   startup 	    character varying(20000) 	  NOT NULL,
  CONSTRAINT portti_bundle_pkey PRIMARY KEY (id),
  CONSTRAINT portti_bundle_name_key UNIQUE (name)
);



CREATE TABLE portti_view_bundle_seq (
   view_id 	    BIGINT	  NOT NULL,
   bundle_id 	    BIGINT 	   NOT NULL,
   seqno 	    INTEGER 	  NOT NULL,
   config 	    character varying(20000) DEFAULT '{}',
   state 	    character varying(20000) DEFAULT '{}',
   startup 	    character varying(20000),
   bundleinstance character varying(128) DEFAULT '',
  CONSTRAINT portti_view_bundle_seq_bundle_id_fkey FOREIGN KEY (bundle_id)
  REFERENCES portti_bundle (id) MATCH SIMPLE
  ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT portti_view_bundle_seq_view_id_fkey FOREIGN KEY (view_id)
  REFERENCES portti_view (id) MATCH SIMPLE
  ON UPDATE NO ACTION ON DELETE NO ACTION,
   CONSTRAINT 	    view_seq	  UNIQUE (view_id, seqno)
);

------ Additional table not found in hsqldb - needed for publisher;

CREATE TABLE portti_terms_of_use_for_publishing
(
  userid bigint NOT NULL,
  agreed boolean NOT NULL DEFAULT false,
  "time" timestamp with time zone
);