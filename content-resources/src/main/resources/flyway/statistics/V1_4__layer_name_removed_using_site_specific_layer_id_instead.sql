-- Removing all mappings, because these are site specific, configured in deployment, not provisioned by Oskari.
DROP TABLE oskari_statistical_layers;
-- Dropping out the whole table, to make another more generic one not bound to only SotkaNET.
DROP TABLE oskari_statistical_sotka_layers;

CREATE TABLE IF NOT EXISTS oskari_statistical_layers
(
  -- The oskari_maplayer_id in Oskari, for example: 9. This maps to the name in the oskari_maplayer table.
  -- We don't make reference constraints here, because during configuration the application is usable even if
  -- the database might be referentially inconsistent here.
  oskari_layer_id      BIGSERIAL NOT NULL,
  -- The attribute name for the region id in the geoserver. For example: "kuntakoodi"
  oskari_region_id_tag CHARACTER VARYING(256) NOT NULL,
  -- The attribute name for the region name in the geoserver. For example: "kuntanimi"
  oskari_name_id_tag   CHARACTER VARYING(256) NOT NULL,
  CONSTRAINT oskari_statistical_layers_pkey PRIMARY KEY (oskari_layer_id)
);

-- This table maps plug-in specific layer ids to Oskari layer ids.
CREATE TABLE IF NOT EXISTS oskari_statistical_plugin_layers
(
  -- For example: "fi.nls.oskari.control.statistics.plugins.sotka.SotkaStatisticalDatasourcePlugin"
  plugin_id        CHARACTER VARYING(256) NOT NULL,
  -- For SotkaNET, these are one of: "Kunta","Maakunta","Erva","Aluehallintovirasto","Sairaanhoitopiiri",
  -- "Seutukunta","Nuts1","ELY-KESKUS"
  -- Note: These are case insensitive and the above list is possibly not exhaustive.
  plugin_layer_id  CHARACTER VARYING(256) NOT NULL,
  -- The layer id in Oskari, for example: 9. This maps to the name in the oskari_maplayers table.
  -- We don't make reference constraints here, because during configuration the application is usable even if
  -- the database might be referentially inconsistent here.
  oskari_layer_id  BIGSERIAL NOT NULL,
  CONSTRAINT oskari_statistical_plugin_layers_pkey PRIMARY KEY (plugin_id, plugin_layer_id)
);
