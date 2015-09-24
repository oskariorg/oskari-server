
-- replacing portti_capabilities_cache
CREATE TABLE IF NOT EXISTS oskari_capabilities_cache (
  id 			BIGSERIAL NOT NULL,
  layertype 	CHARACTER VARYING(64) NOT NULL,
  url 		CHARACTER VARYING(2048) NOT NULL,
  data 		TEXT NOT NULL,
  created     TIMESTAMP WITH TIME ZONE DEFAULT current_timestamp,
  updated     TIMESTAMP WITH TIME ZONE,
  CONSTRAINT oskari_capabilities_cache_pkey PRIMARY KEY (id)
);

-- support for multiple projections/maplayer
CREATE TABLE IF NOT EXISTS oskari_maplayer_projections (
  id 			BIGSERIAL NOT NULL,
  name	 	CHARACTER VARYING(64) NOT NULL,
  maplayerid  INTEGER NOT NULL,
  CONSTRAINT oskari_maplayer_id_fkey FOREIGN KEY (maplayerid)
  REFERENCES oskari_maplayer (id) MATCH SIMPLE
  ON UPDATE NO ACTION ON DELETE CASCADE
);