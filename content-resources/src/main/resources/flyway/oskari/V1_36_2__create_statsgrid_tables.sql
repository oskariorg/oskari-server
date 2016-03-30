
CREATE TABLE IF NOT EXISTS oskari_statistical_datasource
(
  id      BIGSERIAL NOT NULL,
  -- Name for the User interface
  locale  text,
  -- config can contain things like baseurl for api endpoint
  config  text DEFAULT '{}'::text,
  -- name of the Oskari annotated component that functions as a plugin between datasource and Oskari
  plugin  text,
  CONSTRAINT oskari_statistical_datasource_pkey PRIMARY KEY (id)
);


CREATE TABLE IF NOT EXISTS oskari_statistical_layer
(
  datasource_id   integer NOT NULL,
  layer_id        integer,
  -- property in datasource that has the value matching layer_property
  source_property text,
  layer_property  text DEFAULT 'id'::text,
  CONSTRAINT oskari_statistical_layer_pkey PRIMARY KEY (datasource_id, layer_id)
);
