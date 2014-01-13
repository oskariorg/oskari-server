
DROP VIEW IF EXISTS preparcel_set;
DROP TABLE IF EXISTS preparcel;
DROP TABLE IF EXISTS preparcel_data;

CREATE TABLE preparcel
(
  id bigserial NOT NULL, -- Kohteen yksilöivä id
  uuid character varying(64),
  kvp_uid character varying(64),
  preparcel_id character varying(64) NOT NULL,
  title text,
  subtitle text,
  "desc" text,
  parent_property_id character varying(64),
  parent_property_quality character varying(32),
  reporter character varying(128),
  area numeric,
  area_unit character varying(10),
  created timestamp with time zone NOT NULL,
  updated timestamp with time zone,
  CONSTRAINT preparcel_pkey PRIMARY KEY (id),
  CONSTRAINT kvpuid_preparcel_id_key UNIQUE (kvp_uid, preparcel_id)
)
WITH (
  OIDS=FALSE
);


CREATE TABLE preparcel_data
(
  id bigserial NOT NULL,
  preparcel_id bigint NOT NULL,
  uuid character varying(64),
  geom_type character varying(32),
  geometry geometry NOT NULL,
  created timestamp with time zone NOT NULL,
  updated timestamp with time zone,
  CONSTRAINT "preparcel_data_pKey" PRIMARY KEY (id),
  CONSTRAINT preparcel_data_preparcel_fkey FOREIGN KEY (preparcel_id)
      REFERENCES preparcel (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);


CREATE OR REPLACE VIEW preparcel_set AS 
 SELECT ad.id, 
    a.uuid, 
    a.kvp_uid, 
    a.preparcel_id, 
    a.title, 
    a.subtitle, 
    a."desc", 
    a.parent_property_id, 
    a.parent_property_quality, 
    a.reporter, 
    a.area, 
    a.area_unit, 
    ad.geom_type, 
    ad.geometry, 
    a.created, 
    a.updated
   FROM preparcel_data ad, 
    preparcel a
  WHERE ad.preparcel_id = a.id;