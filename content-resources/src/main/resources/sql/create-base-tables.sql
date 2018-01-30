-- NOTE!;
-- THE FILE IS TOKENIZED WITH SEMICOLON CHARACTER!;
-- EACH COMMENT _NEED_ TO END WITH A SEMICOLON OR OTHERWISE THE NEXT ACTUAL SQL IS NOT RUN!;
-- ----------------------------------------------------------------------------------------;

CREATE TABLE portti_capabilities_cache
(
  layer_id serial NOT NULL,
  data text,
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
  gfi_content text,
  realtime boolean DEFAULT false,
  refresh_rate integer DEFAULT 0,
  created timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
  updated timestamp with time zone,
  username character varying(256),
  password character varying(256),
  srs_name character varying,
  version character varying(64),
  attributes text DEFAULT '{}',
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

CREATE TABLE oskari_maplayer_metadata
(
  id serial NOT NULL,
  metadataid character varying(256),
  wkt character varying(512) default '',
  json text default '',
  ts timestamp DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT oskari_maplayer_metadata_pkey PRIMARY KEY (id)
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


CREATE TABLE portti_view (
   uuid             UUID,
   id               bigserial NOT NULL,
   name             VARCHAR(128)  NOT NULL,
   is_default       BOOLEAN       DEFAULT FALSE,
   type		    varchar(16)	  DEFAULT 'USER',
   description   VARCHAR(2000) ,
   page character varying(128) DEFAULT 'index',
   application character varying(128) DEFAULT 'servlet',
   application_dev_prefix character varying(256) DEFAULT '/applications/sample',
   only_uuid boolean DEFAULT FALSE,
   creator bigint DEFAULT (-1),
   domain character varying(512) DEFAULT ''::character varying,
   lang character varying(2) DEFAULT 'en'::character varying,
   is_public boolean DEFAULT FALSE,
   metadata TEXT DEFAULT '{}'::TEXT,
   old_id bigint DEFAULT (-1),
   created timestamp DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT portti_view_pkey PRIMARY KEY (id),
  CONSTRAINT portti_view_uuid_key UNIQUE (uuid)
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


CREATE TABLE portti_terms_of_use_for_publishing
(
  userid bigint NOT NULL,
  agreed boolean NOT NULL DEFAULT false,
  "time" timestamp with time zone
);


-- ----------------------------------------------------------------------------------------;
-- WFS tables;
-- ----------------------------------------------------------------------------------------;
CREATE TABLE portti_wfs_layer
(
  id serial NOT NULL,
  maplayer_id bigint NOT NULL,
  layer_name character varying(256),
  gml_geometry_property character varying(256),
  gml_version character varying(64),
  gml2_separator boolean NOT NULL DEFAULT false,
  get_highlight_image boolean NOT NULL DEFAULT true,
  max_features integer NOT NULL DEFAULT 100,
  feature_namespace character varying DEFAULT 512,
  wfs_template_model_id integer,
  feature_type character varying(4000),
  selected_feature_params character varying(4000) default '{}',
  feature_params_locales text,
  properties character varying(4000),
  geometry_type character varying(8),
  selection_sld_style_id integer,
  get_map_tiles boolean NOT NULL DEFAULT true,
  get_feature_info boolean NOT NULL DEFAULT true,
  tile_request boolean NOT NULL DEFAULT false,
  tile_buffer character varying(512) default '{}',
  wms_layer_id integer,
  wps_params character varying(256) default '{}',
  feature_element character varying(512),
  output_format character varying(256),
  feature_namespace_uri character varying(512),
  geometry_namespace_uri character varying(512),
  job_type character varying(256),
  request_impulse character varying(256),
  CONSTRAINT portti_wfs_layer_pkey PRIMARY KEY (id)
)
WITH (
OIDS=FALSE
);

CREATE TABLE portti_wfs_layer_style
(
  id serial NOT NULL,
  "name" character varying(256),
  sld_style text,
  CONSTRAINT portti_wfs_layer_style_pkey PRIMARY KEY (id)
)
WITH (
OIDS=FALSE
);

CREATE TABLE portti_wfs_layers_styles
(
  id serial NOT NULL,
  wfs_layer_id bigint NOT NULL,
  wfs_layer_style_id integer NOT NULL,
  CONSTRAINT portti_wfs_layers_styles_pkey PRIMARY KEY (id),
  CONSTRAINT portti_wfs_layers_styles_wfs_layer_fkey FOREIGN KEY (wfs_layer_id)
  REFERENCES portti_wfs_layer (id) MATCH SIMPLE
  ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT portti_wfs_layers_styles_wfs_layer_style_fkey FOREIGN KEY (wfs_layer_style_id)
  REFERENCES portti_wfs_layer_style (id) MATCH SIMPLE
  ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
OIDS=FALSE
);

CREATE INDEX fki_portti_wfs_layers_styles_wfs_layer_style_fkey
ON portti_wfs_layers_styles
USING btree
(wfs_layer_style_id);

CREATE TABLE portti_wfs_template_model
(
  id serial NOT NULL,
  "name" character varying(256),
  description character varying(4000),
  "type" character varying(64),
  request_template text,
  response_template text,
  parse_config text,
  CONSTRAINT portti_wfs_template_model_pkey PRIMARY KEY (id)
)
WITH (
OIDS=FALSE
);


CREATE TABLE oskari_wfs_parser_config
(
  id serial NOT NULL,
  name character varying(128),
  type character varying(64),
  request_template text,
  response_template text,
  parse_config text,
  sld_style text,
  CONSTRAINT oskari_wfs_parser_config_pkey PRIMARY KEY (id)
)
WITH (
  OIDS=FALSE
);

-- ----------------------------------------------------------------------------------------;
-- Thematic maps tables;
-- ----------------------------------------------------------------------------------------;

CREATE TABLE portti_stats_layer
(
  id bigserial NOT NULL,
  maplayer_id integer,
  name text,
  visualization character varying(1000),
  classes character varying(2000),
  colors character varying(1000),
  layername character varying(1000),
  filterproperty character varying(200),
  geometryproperty character varying(200),
  externalid character varying(200),
  CONSTRAINT portti_stats_layer_pkey PRIMARY KEY (id),
  CONSTRAINT portti_stats_layer_maplayer_id_fkey FOREIGN KEY (maplayer_id)
  REFERENCES oskari_maplayer (id) MATCH FULL
  ON UPDATE NO ACTION ON DELETE CASCADE
);

CREATE TABLE oskari_user_indicator
(
  id serial NOT NULL,
  user_id bigint,
  title character varying(1000),
  source character varying(1000),
  layer_id bigint,
  description character varying(1000),
  year bigint,
  data text,
  published BOOLEAN,
  category character varying(100)
);

-- ----------------------------------------------------------------------------------------;
-- Keyword tables;
-- ----------------------------------------------------------------------------------------;

CREATE TABLE portti_keywords
(
  id serial NOT NULL,
  keyword character varying(2000),
  uri character varying(2000),
  lang character varying(10),
  editable boolean,
  CONSTRAINT portti_keywords_pkey PRIMARY KEY (id)
);

CREATE TABLE portti_layer_keywords
(
  keyid bigint NOT NULL,
  layerid bigint NOT NULL,
  CONSTRAINT oskari_layer_keywords_layerid_fkey FOREIGN KEY (layerid)
  REFERENCES oskari_maplayer (id) MATCH SIMPLE
  ON UPDATE NO ACTION ON DELETE CASCADE,
  CONSTRAINT portti_layer_keywords_keyid_fkey FOREIGN KEY (keyid)
  REFERENCES portti_keywords (id) MATCH SIMPLE
  ON UPDATE NO ACTION ON DELETE CASCADE
);

CREATE TABLE portti_keyword_association
(
  keyid1 bigint NOT NULL,
  keyid2 bigint NOT NULL,
  type character varying(10),
  CONSTRAINT portti_keyword_association_keyid1_fkey FOREIGN KEY (keyid1)
  REFERENCES portti_keywords (id) MATCH SIMPLE
  ON UPDATE NO ACTION ON DELETE CASCADE,
  CONSTRAINT portti_keyword_association_keyid2_fkey FOREIGN KEY (keyid2)
  REFERENCES portti_keywords (id) MATCH SIMPLE
  ON UPDATE NO ACTION ON DELETE CASCADE,
  CONSTRAINT unique_all_columns UNIQUE (keyid1, keyid2, type)
);

-- ----------------------------------------------------------------------------------------;
-- User data;
-- ----------------------------------------------------------------------------------------;

-- contains user information;
CREATE TABLE oskari_users (
  id serial NOT NULL,
  user_name character varying(128) NOT NULL,
  first_name character varying(128),
  last_name character varying(128),
  email character varying(256),
  uuid character varying(64),
  attributes text DEFAULT '{}',
  CONSTRAINT oskari_users_pkey PRIMARY KEY (id),
  CONSTRAINT oskari_users_user_name_key UNIQUE (user_name),
  CONSTRAINT oskari_users_uuid_key UNIQUE (uuid)
);

-- contains roles used in Oskari;
CREATE TABLE oskari_roles (
  id serial NOT NULL,
  name character varying(25) NOT NULL,
  is_guest boolean default FALSE,
  CONSTRAINT oskari_roles_pkey PRIMARY KEY (id),
  UNIQUE (name)
);

-- maps Oskari roles to users;
CREATE TABLE oskari_role_oskari_user
(
  id serial NOT NULL,
  role_id integer,
  user_id bigint,
  CONSTRAINT oskari_role_oskari_user_pkey PRIMARY KEY (id),
  CONSTRAINT oskari_role_oskari_user_role_id_fkey FOREIGN KEY (role_id)
  REFERENCES oskari_roles (id) MATCH SIMPLE
  ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT oskari_role_oskari_user_user_id_fkey FOREIGN KEY (user_id)
  REFERENCES oskari_users (id) MATCH SIMPLE
  ON UPDATE NO ACTION ON DELETE NO ACTION
);

-- maps external role name to Oskari role;
CREATE TABLE oskari_role_external_mapping (
  roleid  bigint NOT NULL,
  name character varying(50) NOT NULL,
  external_type character varying(50) NOT NULL default '',
  CONSTRAINT oskari_role_external_mapping_fkey FOREIGN KEY (roleid)
  REFERENCES oskari_roles (id) MATCH SIMPLE
  ON UPDATE NO ACTION ON DELETE CASCADE
);

-- credentials for users;
CREATE TABLE oskari_jaas_users (
  id serial NOT NULL,
  login character varying(25) NOT NULL,
  password character varying(50) NOT NULL,
  CONSTRAINT oskari_jaas_users_pkey PRIMARY KEY (id),
  UNIQUE (login)
);

CREATE TABLE oskari_jaas_roles (
  id serial NOT NULL,
  login character varying(25) NOT NULL,
  role character varying(50) NOT NULL,
  CONSTRAINT oskari_jaas_roles_pkey PRIMARY KEY (id)
);
