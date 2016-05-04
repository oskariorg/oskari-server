-- This information is now in attributes JSON in oskari_maplayer table
DROP TABLE IF EXISTS oskari_statistical_layers;
-- This is totally unnecessary
DROP TABLE IF EXISTS portti_stats_layer;

ALTER TABLE oskari_statistical_datasource_plugins ALTER class_name TYPE text;
ALTER TABLE oskari_statistical_datasource_plugins ALTER localized_name_id TYPE text;

ALTER TABLE oskari_statistical_plugin_layers ALTER plugin_id TYPE text;
ALTER TABLE oskari_statistical_plugin_layers ALTER plugin_layer_id TYPE text;

ALTER TABLE oskari_statistical_datasource_plugins DROP localized_name_id;
ALTER TABLE oskari_statistical_datasource_plugins ADD locale text;

UPDATE oskari_statistical_datasource_plugins SET locale='{"fi":{"name":"SotkaNET"},"sv":{"name":"SotkaNET"},"en":{"name":"SotkaNET"}}'
  WHERE class_name='fi.nls.oskari.control.statistics.plugins.sotka.SotkaStatisticalDatasourcePlugin';
UPDATE oskari_statistical_datasource_plugins SET locale='{"fi":{"name":"Omat indikaattorit"},"sv":{"name":"Dina indikatorer"},"en":{"name":"Your indicators"}}'
  WHERE class_name='fi.nls.oskari.control.statistics.plugins.user.UserIndicatorsStatisticalDatasourcePlugin';
