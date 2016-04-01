ALTER TABLE oskari_statistical_datasource_plugins ADD component_id text;

UPDATE oskari_statistical_datasource_plugins SET component_id='SotkaNET'
  WHERE class_name='fi.nls.oskari.control.statistics.plugins.sotka.SotkaStatisticalDatasourcePlugin';
UPDATE oskari_statistical_datasource_plugins SET component_id='UserStats'
  WHERE class_name='fi.nls.oskari.control.statistics.plugins.user.UserIndicatorsStatisticalDatasourcePlugin';

ALTER TABLE oskari_statistical_datasource_plugins DROP class_name;
