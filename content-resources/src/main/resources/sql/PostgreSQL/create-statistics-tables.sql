
DROP TABLE IF EXISTS portti_stats_layer;
DROP TABLE IF EXISTS oskari_user_indicator;

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
