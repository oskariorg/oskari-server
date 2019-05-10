package fi.nls.oskari.control.statistics.plugins.unsd;

import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.control.statistics.plugins.StatisticalDatasourceFactory;
import fi.nls.oskari.control.statistics.plugins.StatisticalDatasourcePlugin;
import fi.nls.oskari.control.statistics.plugins.db.StatisticalDatasource;

@Oskari("UNSD")
public class UnsdStatisticalDatasourceFactory extends StatisticalDatasourceFactory {

    public StatisticalDatasourcePlugin create(StatisticalDatasource source) {
        UnsdStatisticalDatasourcePlugin plugin = new UnsdStatisticalDatasourcePlugin();
        plugin.init(source);
        return plugin;
    }
}
