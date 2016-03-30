
CREATE TABLE IF NOT EXISTS oskari_statistical_datasource
(
  id      BIGSERIAL NOT NULL,
  -- Name for the User interface
  locale  text NOT NULL DEFAULT '{}'::text,
  -- config can contain things like baseurl for api endpoint
  config  text DEFAULT '{}'::text,
  -- name of the Oskari annotated component that functions as a plugin between datasource and Oskari
  plugin  text NOT NULL,
  CONSTRAINT oskari_statistical_datasource_pkey PRIMARY KEY (id)
);


CREATE TABLE IF NOT EXISTS oskari_statistical_layer
(
  datasource_id   integer REFERENCES oskari_statistical_datasource (id) ON DELETE CASCADE ON UPDATE CASCADE,
  layer_id        integer REFERENCES oskari_maplayer (id)  ON DELETE CASCADE ON UPDATE CASCADE,
  -- property in datasource that has the value matching layer_property
  source_property text NOT NULL DEFAULT 'id'::text,
  layer_property  text NOT NULL DEFAULT 'id'::text,
  CONSTRAINT oskari_statistical_layer_pkey PRIMARY KEY (datasource_id, layer_id)
);
