package fi.nls.oskari.statistics.eurostat;

import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.control.statistics.plugins.StatisticalDatasourceFactory;
import fi.nls.oskari.control.statistics.plugins.StatisticalDatasourcePlugin;
import fi.nls.oskari.control.statistics.plugins.db.StatisticalDatasource;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;

//  INSERT INTO oskari_statistical_datasource(locale, plugin) VALUES ('{"en":{"name":"Eurostat"}}', 'Eurostat')
// "Eurostat" is the database table : Oskari_statistic_datasource, plugin field.
@Oskari("Eurostat")
public class EurostatStatisticalDatasourceFactory extends StatisticalDatasourceFactory {
    private final static Logger log = LogFactory.getLogger(EurostatStatisticalDatasourceFactory.class);

    @Override
    public StatisticalDatasourcePlugin create(StatisticalDatasource source) {
        EurostatStatisticalDatasourcePlugin plugin = new EurostatStatisticalDatasourcePlugin();
        plugin.init(source);
        return plugin;
    }
}




