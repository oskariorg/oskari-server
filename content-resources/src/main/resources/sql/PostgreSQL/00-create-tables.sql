-- NOTE!;
-- THE FILE IS TOKENIZED WITH SEMICOLON CHARACTER!;
-- EACH COMMENT _NEED_ TO END WITH A SEMICOLON OR OTHERWISE THE NEXT ACTUAL SQL IS NOT RUN!;
-- ----------------------------------------------------------------------------------------;

DROP TABLE IF EXISTS portti_maplayer;
DROP TABLE IF EXISTS portti_layerclass;
DROP TABLE IF EXISTS oskari_permission;
DROP TABLE IF EXISTS oskari_resource;
DROP TABLE IF EXISTS oskari_maplayer_themes;
DROP TABLE IF EXISTS oskari_maplayer;
DROP TABLE IF EXISTS oskari_layergroup;
DROP TABLE IF EXISTS portti_inspiretheme;


DROP TABLE IF EXISTS portti_maplayer_metadata;
DROP TABLE IF EXISTS portti_capabilities_cache;

DROP VIEW IF EXISTS portti_backendalert;
DROP VIEW IF EXISTS portti_backendstatus_allknown;
DROP TABLE IF EXISTS portti_backendstatus;

DROP TABLE IF EXISTS portti_view_bundle_seq;
DROP TABLE IF EXISTS portti_bundle;
DROP TABLE IF EXISTS portti_view;
DROP TABLE IF EXISTS portti_view_supplement;

DROP TABLE IF EXISTS portti_published_map_usage;
DROP TABLE IF EXISTS portti_published_map_statistics;
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

CREATE TABLE oskari_layergroup
(
  id serial NOT NULL,
  locale text DEFAULT '{}'::text,
  CONSTRAINT oskari_layergroup_pkey PRIMARY KEY (id)
)
WITH (
OIDS=FALSE
);

CREATE TABLE oskari_maplayer
(
  id serial NOT NULL,
  parentId integer DEFAULT -1 NOT NULL,
  externalId character varying(50),
  type character varying(50) NOT NULL,
  base_map boolean DEFAULT false NOT NULL,
  groupId integer,
  name character varying(2000),
  url character varying(2000),
  locale text,
  opacity integer DEFAULT 100,
  style character varying(100),
  minscale double precision DEFAULT -1,
  maxscale double precision DEFAULT -1,
  legend_image character varying(2000),
  metadataId character varying(200),
  tile_matrix_set_id character varying(200),
  tile_matrix_set_data text,
  params text DEFAULT '{}'::text,
  options text DEFAULT '{}'::text,
  gfi_type character varying(200),
  gfi_xslt text,
  realtime boolean DEFAULT false,
  refresh_rate integer DEFAULT 0,
  created timestamp with time zone,
  updated timestamp with time zone,
  CONSTRAINT oskari_maplayer_pkey PRIMARY KEY (id),
  CONSTRAINT oskari_maplayer_groupId_fkey FOREIGN KEY (groupId)
  REFERENCES oskari_layergroup (id) MATCH SIMPLE
  ON UPDATE NO ACTION ON DELETE CASCADE
)
WITH (
OIDS=FALSE
);

CREATE INDEX oskari_maplayer_q1
ON oskari_maplayer
USING btree
(parentId);


CREATE INDEX oskari_maplayer_q2
ON oskari_maplayer
USING btree
(groupId);

CREATE TABLE oskari_maplayer_themes
(
  maplayerid integer NOT NULL,
  themeid integer NOT NULL,
  CONSTRAINT oskari_maplayer_id_fkey FOREIGN KEY (maplayerid)
  REFERENCES oskari_maplayer (id) MATCH SIMPLE
  ON UPDATE NO ACTION ON DELETE CASCADE,
  CONSTRAINT portti_inspiretheme_id_fkey FOREIGN KEY (themeid)
  REFERENCES portti_inspiretheme (id) MATCH SIMPLE
  ON UPDATE NO ACTION ON DELETE CASCADE
)
WITH (
OIDS=FALSE
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

CREATE TABLE oskari_resource
(
  id serial NOT NULL,
  resource_type character varying(100) NOT NULL,
  resource_mapping character varying(1000) NOT NULL,
  CONSTRAINT type_mapping UNIQUE (resource_type, resource_mapping)
);

CREATE TABLE oskari_permission
(
  id serial NOT NULL,
  oskari_resource_id bigint NOT NULL,
  external_type character varying(100),
  permission character varying(100),
  external_id character varying(1000)
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

CREATE TABLE portti_published_map_usage
(
  id serial NOT NULL,
  published_map_id bigint NOT NULL,
  usage_count bigint NOT NULL,
  force_lock boolean NOT NULL DEFAULT false,
  CONSTRAINT portti_published_map_usage_pkey PRIMARY KEY (id)
);

CREATE TABLE portti_published_map_statistics
(
  id serial NOT NULL,
  published_map_id bigint NOT NULL,
  count_total_lifecycle bigint NOT NULL,
  CONSTRAINT portti_published_map_statistics_pkey PRIMARY KEY (id)
);


------ Additional table not found in hsqldb - needed for publisher;

CREATE TABLE portti_terms_of_use_for_publishing
(
  userid bigint NOT NULL,
  agreed boolean NOT NULL DEFAULT false,
  "time" timestamp with time zone
);

