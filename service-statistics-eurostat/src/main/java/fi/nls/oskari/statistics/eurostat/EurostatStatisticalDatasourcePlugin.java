package fi.nls.oskari.statistics.eurostat;

import fi.nls.oskari.control.statistics.plugins.StatisticalDatasourcePlugin;
import fi.nls.oskari.control.statistics.data.StatisticalIndicator;
import fi.nls.oskari.control.statistics.plugins.db.DatasourceLayer;
import fi.nls.oskari.control.statistics.plugins.db.StatisticalDatasource;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class EurostatStatisticalDatasourcePlugin extends StatisticalDatasourcePlugin {
    private final static Logger LOG = LogFactory.getLogger(EurostatStatisticalDatasourcePlugin.class);
    private EurostatIndicatorsParser indicatorsParser;

    private EurostatConfig config;

    @Override
    public void update() {
        List<StatisticalIndicator> indicators = indicatorsParser.parse(getSource().getLayers());
        for(StatisticalIndicator ind: indicators) {
            onIndicatorProcessed(ind);
        }
    }

    @Override
    public void init(StatisticalDatasource source) {
        super.init(source);
        try {
            config = new EurostatConfig(source.getConfigJSON(), source.getId());
            indicatorsParser = new EurostatIndicatorsParser(config);
        } catch (IOException e) {
            LOG.error(e, "Error getting indicators from Eurostat datasource:", config.getUrl());
        }
    }
}

