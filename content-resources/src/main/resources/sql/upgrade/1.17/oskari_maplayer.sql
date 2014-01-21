
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
  base_map boolean DEFAULT false,
  groupId integer DEFAULT -1 NOT NULL,
  name character varying(2000),
  url character varying(2000),
  locale text DEFAULT '{}'::text,
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