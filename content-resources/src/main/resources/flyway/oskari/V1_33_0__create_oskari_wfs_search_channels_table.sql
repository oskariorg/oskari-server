
CREATE TABLE IF NOT EXISTS oskari_wfs_search_channels
(
  id BIGSERIAL NOT NULL,
  wfs_layer_id integer NOT NULL,
  topic text NOT NULL,
  description text,
  params_for_search text NOT NULL,
  is_default boolean,
  is_address boolean,
  CONSTRAINT portti_wfs_search_channels_pkey PRIMARY KEY (id)
);


