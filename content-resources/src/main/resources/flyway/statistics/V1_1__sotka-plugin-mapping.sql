CREATE TABLE IF NOT EXISTS oskari_statistical_sotka_layers
(
  -- One of: "Kunta","Maakunta","Erva","Aluehallintovirasto","Sairaanhoitopiiri","Maa","Suuralue","Seutukunta","Nuts1","ELY-KESKUS"
  -- Note: These are case independent and the above list is possibly not exhaustive.
  sotka_layer_id       CHARACTER VARYING(256) NOT NULL,
  -- The layer name in Oskari, for example: "oskari:kunnat2013". This maps to the name in the oskari_maplayers table.
  -- We don't make reference constraints here, because during configuration the application is usable even if
  -- the database might be referentially inconsistent here.
  oskari_layer_name    CHARACTER VARYING(256) NOT NULL,
  -- The attribute name for the region id in the geoserver. For example: "kuntakoodi"
  oskari_region_id_tag CHARACTER VARYING(256) NOT NULL,
  -- The attribute name for the region name in the geoserver. For example: "kuntanimi"
  oskari_name_id_tag   CHARACTER VARYING(256) NOT NULL,
  CONSTRAINT oskari_statistical_sotka_layers_pkey PRIMARY KEY (sotka_layer_id)
);

INSERT INTO oskari_statistical_sotka_layers (sotka_layer_id, oskari_layer_name, oskari_region_id_tag, oskari_name_id_tag) VALUES (
  'Kunta',
  'oskari:kunnat2013',
  'kuntakoodi',
  'kuntanimi'
);
INSERT INTO oskari_statistical_sotka_layers (sotka_layer_id, oskari_layer_name, oskari_region_id_tag, oskari_name_id_tag) VALUES (
  'Maakunta',
  'oskari:maakunta',
  'maakuntanro',
  'maakunta'
);
INSERT INTO oskari_statistical_sotka_layers (sotka_layer_id, oskari_layer_name, oskari_region_id_tag, oskari_name_id_tag) VALUES (
  'Erva',
  'oskari:erva-alueet',
  'erva_numero',
  'erva'
);
INSERT INTO oskari_statistical_sotka_layers (sotka_layer_id, oskari_layer_name, oskari_region_id_tag, oskari_name_id_tag) VALUES (
  'Aluehallintovirasto',
  'oskari:avi',
  'avi_nro',
  'avi'
);
INSERT INTO oskari_statistical_sotka_layers (sotka_layer_id, oskari_layer_name, oskari_region_id_tag, oskari_name_id_tag) VALUES (
  'Sairaanhoitopiiri',
  'oskari:sairaanhoitopiiri',
  'sairaanhoitopiirinro',
  'sairaanhoitopiiri'
);
INSERT INTO oskari_statistical_sotka_layers (sotka_layer_id, oskari_layer_name, oskari_region_id_tag, oskari_name_id_tag) VALUES (
  'ELY-KESKUS',
  'oskari:ely',
  'ely_nro',
  'ely'
);
INSERT INTO oskari_statistical_sotka_layers (sotka_layer_id, oskari_layer_name, oskari_region_id_tag, oskari_name_id_tag) VALUES (
  'Seutukunta',
  'oskari:seutukunta',
  'seutukuntanro',
  'seutukunta'
);
INSERT INTO oskari_statistical_sotka_layers (sotka_layer_id, oskari_layer_name, oskari_region_id_tag, oskari_name_id_tag) VALUES (
  'Nuts1',
  'oskari:nuts1',
  'code',
  'nuts1'
);
