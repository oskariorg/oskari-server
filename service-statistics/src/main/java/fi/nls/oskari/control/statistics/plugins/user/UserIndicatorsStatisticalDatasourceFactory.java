package fi.nls.oskari.control.statistics.plugins.user;

import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.control.statistics.plugins.StatisticalDatasourceFactory;
import fi.nls.oskari.control.statistics.plugins.StatisticalDatasourcePlugin;
import fi.nls.oskari.control.statistics.plugins.db.StatisticalDatasource;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;

@Oskari("UserStats")
public class UserIndicatorsStatisticalDatasourceFactory extends StatisticalDatasourceFactory {
    private final static Logger LOG = LogFactory.getLogger(UserIndicatorsStatisticalDatasourceFactory.class);

    public StatisticalDatasourcePlugin create(StatisticalDatasource source) {
        UserIndicatorsStatisticalDatasourcePlugin plugin = new UserIndicatorsStatisticalDatasourcePlugin();
        plugin.init(source);
        return plugin;
    }
}
