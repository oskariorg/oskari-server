CREATE TABLE IF NOT EXISTS oskari_statistical_datasource_plugins
(
  class_name        CHARACTER VARYING(256) NOT NULL,
  localized_name_id CHARACTER VARYING(256) NOT NULL,
  -- This is just for guaranteeing uniqueness, this table will never have more than a few rows, all fetched at once.
  CONSTRAINT oskari_statistical_datasource_plugins_pkey PRIMARY KEY (class_name)
);

INSERT INTO oskari_statistical_datasource_plugins (class_name, localized_name_id) VALUES (
  'fi.nls.oskari.control.statistics.plugins.sotka.SotkaStatisticalDatasourcePlugin',
  'fi.nls.oskari.control.statistics.plugins.sotka.plugin_name');
