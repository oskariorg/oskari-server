CREATE TABLE IF NOT EXISTS oskari_statistical_sotka_layers
(
  -- One of: "Kunta","Maakunta","Erva","Aluehallintovirasto","Sairaanhoitopiiri","Seutukunta","Nuts1","ELY-KESKUS"
  -- Note: These are case independent and the above list is possibly not exhaustive.
  sotka_layer_id       CHARACTER VARYING(256) NOT NULL,
  -- The layer name in Oskari, for example: "oskari:kunnat2013". This maps to the name in the oskari_maplayers table.
  -- We don't make reference constraints here, because during configuration the application is usable even if
  -- the database might be referentially inconsistent here.
  oskari_layer_name    CHARACTER VARYING(256) NOT NULL,
  CONSTRAINT oskari_statistical_sotka_layers_pkey PRIMARY KEY (sotka_layer_id)
);

INSERT INTO oskari_statistical_sotka_layers (sotka_layer_id, oskari_layer_name) VALUES (
  'Kunta',
  'oskari:kunnat2013'
);
INSERT INTO oskari_statistical_sotka_layers (sotka_layer_id, oskari_layer_name) VALUES (
  'Maakunta',
  'oskari:maakunta'
);
INSERT INTO oskari_statistical_sotka_layers (sotka_layer_id, oskari_layer_name) VALUES (
  'Erva',
  'oskari:erva-alueet'
);
INSERT INTO oskari_statistical_sotka_layers (sotka_layer_id, oskari_layer_name) VALUES (
  'Aluehallintovirasto',
  'oskari:avi'
);
INSERT INTO oskari_statistical_sotka_layers (sotka_layer_id, oskari_layer_name) VALUES (
  'Sairaanhoitopiiri',
  'oskari:sairaanhoitopiiri'
);
INSERT INTO oskari_statistical_sotka_layers (sotka_layer_id, oskari_layer_name) VALUES (
  'ELY-KESKUS',
  'oskari:ely'
);
INSERT INTO oskari_statistical_sotka_layers (sotka_layer_id, oskari_layer_name) VALUES (
  'Seutukunta',
  'oskari:seutukunta'
);
INSERT INTO oskari_statistical_sotka_layers (sotka_layer_id, oskari_layer_name) VALUES (
  'Nuts1',
  'oskari:nuts1'
);
