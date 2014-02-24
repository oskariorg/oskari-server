DROP TABLE IF EXISTS portti_wfs_layers_styles;
DROP TABLE IF EXISTS portti_wfs_layer_style;
DROP TABLE IF EXISTS portti_wfs_layer;
DROP TABLE IF EXISTS portti_wfs_template_model;

CREATE TABLE portti_wfs_layer
(
  id serial NOT NULL,
  maplayer_id bigint NOT NULL,
  layer_name character varying(256),
  url character varying(512),
  username character varying(256),
  "password" character varying(512),
  gml_geometry_property character varying(256),
  gml_version character varying(64),
  gml2_separator boolean NOT NULL DEFAULT false,
  get_highlight_image boolean NOT NULL DEFAULT true,
  wfs_version character varying(64),
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
  srs_name character varying,
  feature_element character varying(512),
  output_format character varying(256),
  feature_namespace_uri character varying(512),
  geometry_namespace_uri character varying(512),
  schema_changed timestamp with time zone,
  schema_last timestamp with time zone,
  schema_status character varying(512),
  custom_parser boolean NOT NULL DEFAULT false,
  test_location character varying(512) default '[]',
  test_zoom integer NOT NULL DEFAULT 9,
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
  CONSTRAINT portti_wfs_template_model_pkey PRIMARY KEY (id)
)
WITH (
OIDS=FALSE
);