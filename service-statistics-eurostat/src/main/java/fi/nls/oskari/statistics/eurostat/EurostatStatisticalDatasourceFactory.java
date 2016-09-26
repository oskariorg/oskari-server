package fi.nls.oskari.statistics.eurostat;

import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.control.statistics.plugins.StatisticalDatasourceFactory;
import fi.nls.oskari.control.statistics.plugins.StatisticalDatasourcePlugin;
import fi.nls.oskari.control.statistics.plugins.db.StatisticalDatasource;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;

@Oskari("Eurostat")
public class EurostatStatisticalDatasourceFactory extends StatisticalDatasourceFactory {
    private final static Logger LOG = LogFactory.getLogger(EurostatStatisticalDatasourceFactory.class);

    public StatisticalDatasourcePlugin create(StatisticalDatasource source) {
        EurostatStatisticalDatasourcePlugin plugin = new EurostatStatisticalDatasourcePlugin();
        plugin.init(source);
        return plugin;
    }
}
