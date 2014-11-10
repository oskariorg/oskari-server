
CREATE TABLE oskari_maplayer_metadata
(
  id serial NOT NULL,
  metadataid character varying(256),
  wkt character varying(512) default '',
  json text default '',
  ts timestamp DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT oskari_maplayer_metadata_pkey PRIMARY KEY (id)
);
