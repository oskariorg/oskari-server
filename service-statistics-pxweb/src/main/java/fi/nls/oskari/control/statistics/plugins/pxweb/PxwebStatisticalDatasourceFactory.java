package fi.nls.oskari.control.statistics.plugins.pxweb;

import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.control.statistics.plugins.StatisticalDatasourceFactory;
import fi.nls.oskari.control.statistics.plugins.StatisticalDatasourcePlugin;
import fi.nls.oskari.control.statistics.plugins.db.StatisticalDatasource;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;

//  INSERT INTO oskari_statistical_datasource(locale, plugin) VALUES ('{"en":{"name":"PxWEB"}}', 'PxWEB')

@Oskari("PxWEB")
public class PxwebStatisticalDatasourceFactory extends StatisticalDatasourceFactory {
    private final static Logger log = LogFactory.getLogger(PxwebStatisticalDatasourceFactory.class);

    @Override
    public StatisticalDatasourcePlugin create(StatisticalDatasource source) {
        PxwebStatisticalDatasourcePlugin plugin = new PxwebStatisticalDatasourcePlugin();
        plugin.init(source);
        return plugin;
    }
}




