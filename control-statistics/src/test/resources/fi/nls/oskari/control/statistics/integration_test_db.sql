INSERT INTO oskari_statistical_layers (oskari_layer_id, oskari_region_id_tag, oskari_name_id_tag) VALUES (
--  'oskari:kunnat2013',
  9,
  'kuntakoodi',
  'kuntanimi'
);
INSERT INTO oskari_statistical_layers (oskari_layer_id, oskari_region_id_tag, oskari_name_id_tag) VALUES (
--  'oskari:maakunta',
  10,
  'maakuntanro',
  'maakunta'
);
INSERT INTO oskari_statistical_layers (oskari_layer_id, oskari_region_id_tag, oskari_name_id_tag) VALUES (
--  'oskari:erva-alueet',
  11,
  'erva_numero',
  'erva'
);
INSERT INTO oskari_statistical_layers (oskari_layer_id, oskari_region_id_tag, oskari_name_id_tag) VALUES (
--  'oskari:avi',
  12,
  'avi_nro',
  'avi'
);
INSERT INTO oskari_statistical_layers (oskari_layer_id, oskari_region_id_tag, oskari_name_id_tag) VALUES (
--  'oskari:sairaanhoitopiiri',
  13,
  'sairaanhoitopiirinro',
  'sairaanhoitopiiri'
);
INSERT INTO oskari_statistical_layers (oskari_layer_id, oskari_region_id_tag, oskari_name_id_tag) VALUES (
--  'oskari:ely',
  14,
  'ely_nro',
  'ely'
);
INSERT INTO oskari_statistical_layers (oskari_layer_id, oskari_region_id_tag, oskari_name_id_tag) VALUES (
--  'oskari:seutukunta',
  15,
  'seutukuntanro',
  'seutukunta'
);
INSERT INTO oskari_statistical_layers (oskari_layer_id, oskari_region_id_tag, oskari_name_id_tag) VALUES (
--  'oskari:nuts1',
  16,
  'code',
  'nuts1'
);

INSERT INTO oskari_statistical_plugin_layers (plugin_id, plugin_layer_id, oskari_layer_id) VALUES (
  'fi.nls.oskari.control.statistics.plugins.sotka.SotkaStatisticalDatasourcePlugin',
  'Kunta',
--  'oskari:kunnat2013'
  9
);
INSERT INTO oskari_statistical_plugin_layers (plugin_id, plugin_layer_id, oskari_layer_id) VALUES (
  'fi.nls.oskari.control.statistics.plugins.sotka.SotkaStatisticalDatasourcePlugin',
  'Maakunta',
--  'oskari:maakunta'
  10
);
INSERT INTO oskari_statistical_plugin_layers (plugin_id, plugin_layer_id, oskari_layer_id) VALUES (
  'fi.nls.oskari.control.statistics.plugins.sotka.SotkaStatisticalDatasourcePlugin',
  'Erva',
--  'oskari:erva-alueet'
  11
);
INSERT INTO oskari_statistical_plugin_layers (plugin_id, plugin_layer_id, oskari_layer_id) VALUES (
  'fi.nls.oskari.control.statistics.plugins.sotka.SotkaStatisticalDatasourcePlugin',
  'Aluehallintovirasto',
--  'oskari:avi'
  12
);
INSERT INTO oskari_statistical_plugin_layers (plugin_id, plugin_layer_id, oskari_layer_id) VALUES (
  'fi.nls.oskari.control.statistics.plugins.sotka.SotkaStatisticalDatasourcePlugin',
  'Sairaanhoitopiiri',
--  'oskari:sairaanhoitopiiri'
  13
);
INSERT INTO oskari_statistical_plugin_layers (plugin_id, plugin_layer_id, oskari_layer_id) VALUES (
  'fi.nls.oskari.control.statistics.plugins.sotka.SotkaStatisticalDatasourcePlugin',
  'ELY-KESKUS',
--  'oskari:ely'
  14
);
INSERT INTO oskari_statistical_plugin_layers (plugin_id, plugin_layer_id, oskari_layer_id) VALUES (
  'fi.nls.oskari.control.statistics.plugins.sotka.SotkaStatisticalDatasourcePlugin',
  'Seutukunta',
--  'oskari:seutukunta'
  15
);
INSERT INTO oskari_statistical_plugin_layers (plugin_id, plugin_layer_id, oskari_layer_id) VALUES (
  'fi.nls.oskari.control.statistics.plugins.sotka.SotkaStatisticalDatasourcePlugin',
  'Nuts1',
--  'oskari:nuts1'
  16
);

INSERT INTO oskari_statistical_plugin_layers (plugin_id, plugin_layer_id, oskari_layer_id) VALUES (
  'fi.nls.oskari.control.statistics.plugins.kapa.KapaStatisticalDatasourcePlugin',
  'oskari:kunnat2013',
  9
);
INSERT INTO oskari_statistical_plugin_layers (plugin_id, plugin_layer_id, oskari_layer_id) VALUES (
  'fi.nls.oskari.control.statistics.plugins.kapa.KapaStatisticalDatasourcePlugin',
  'oskari:maakunta',
  10
);
INSERT INTO oskari_statistical_plugin_layers (plugin_id, plugin_layer_id, oskari_layer_id) VALUES (
  'fi.nls.oskari.control.statistics.plugins.kapa.KapaStatisticalDatasourcePlugin',
  'oskari:erva-alueet',
  11
);
INSERT INTO oskari_statistical_plugin_layers (plugin_id, plugin_layer_id, oskari_layer_id) VALUES (
  'fi.nls.oskari.control.statistics.plugins.kapa.KapaStatisticalDatasourcePlugin',
  'oskari:avi',
  12
);
INSERT INTO oskari_statistical_plugin_layers (plugin_id, plugin_layer_id, oskari_layer_id) VALUES (
  'fi.nls.oskari.control.statistics.plugins.kapa.KapaStatisticalDatasourcePlugin',
  'oskari:sairaanhoitopiiri',
  13
);
INSERT INTO oskari_statistical_plugin_layers (plugin_id, plugin_layer_id, oskari_layer_id) VALUES (
  'fi.nls.oskari.control.statistics.plugins.kapa.KapaStatisticalDatasourcePlugin',
  'oskari:ely',
  14
);
INSERT INTO oskari_statistical_plugin_layers (plugin_id, plugin_layer_id, oskari_layer_id) VALUES (
  'fi.nls.oskari.control.statistics.plugins.kapa.KapaStatisticalDatasourcePlugin',
  'oskari:seutukunta',
  15
);
INSERT INTO oskari_statistical_plugin_layers (plugin_id, plugin_layer_id, oskari_layer_id) VALUES (
  'fi.nls.oskari.control.statistics.plugins.kapa.KapaStatisticalDatasourcePlugin',
  'oskari:nuts1',
  16
);

