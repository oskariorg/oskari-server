package fi.nls.oskari.control.statistics.plugins.kapa;

import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.control.statistics.plugins.StatisticalDatasourceFactory;
import fi.nls.oskari.control.statistics.plugins.StatisticalDatasourcePlugin;
import fi.nls.oskari.control.statistics.plugins.db.StatisticalDatasource;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;

@Oskari("KAPA")
public class KapaStatisticalDatasourceFactory extends StatisticalDatasourceFactory {
    public StatisticalDatasourcePlugin create(StatisticalDatasource source) {
        KapaStatisticalDatasourcePlugin plugin = new KapaStatisticalDatasourcePlugin();
        plugin.init(source);
        return plugin;
    }
}
