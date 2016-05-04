package fi.nls.oskari.control.statistics.plugins.sotka;

import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.control.statistics.plugins.StatisticalDatasourceFactory;
import fi.nls.oskari.control.statistics.plugins.StatisticalDatasourcePlugin;
import fi.nls.oskari.control.statistics.plugins.db.StatisticalDatasource;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.OskariComponent;

@Oskari("SotkaNET")
public class SotkaStatisticalDatasourceFactory extends StatisticalDatasourceFactory {
    private final static Logger LOG = LogFactory.getLogger(SotkaStatisticalDatasourceFactory.class);

    public StatisticalDatasourcePlugin create(StatisticalDatasource source) {
        SotkaStatisticalDatasourcePlugin plugin = new SotkaStatisticalDatasourcePlugin();
        plugin.init(source);
        return plugin;
    }
}
