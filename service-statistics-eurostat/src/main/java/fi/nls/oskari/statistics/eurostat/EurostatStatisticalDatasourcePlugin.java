package fi.nls.oskari.statistics.eurostat;

import fi.nls.oskari.control.statistics.plugins.AbstractStatisticalDatasourcePlugin;
import fi.nls.oskari.control.statistics.data.StatisticalIndicator;
import fi.nls.oskari.control.statistics.plugins.db.DatasourceLayer;
import fi.nls.oskari.control.statistics.plugins.db.StatisticalDatasource;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class EurostatStatisticalDatasourcePlugin extends AbstractStatisticalDatasourcePlugin {
    private final static Logger LOG = LogFactory.getLogger(EurostatStatisticalDatasourcePlugin.class);
    private EurostatIndicatorsParser indicatorsParser;

    private List<DatasourceLayer> layers;
    private EurostatConfig config;


    @Override
    public List<StatisticalIndicator> getIndicators(User user) {
        try {
            List<StatisticalIndicator> indicators = indicatorsParser.parse(layers);
            return indicators;
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    @Override
    public void init(StatisticalDatasource source) {
        super.init(source);
        try {
            layers = source.getLayers();
            config = new EurostatConfig(source.getConfigJSON(), source.getId());
            indicatorsParser = new EurostatIndicatorsParser(config);

            LOG.debug("Eurostat layer mappings: ", layers);
        } catch (IOException e) {
            LOG.error(e, "Error getting indicators from Eurostat datasource:", config.getUrl());
        }
    }
}

