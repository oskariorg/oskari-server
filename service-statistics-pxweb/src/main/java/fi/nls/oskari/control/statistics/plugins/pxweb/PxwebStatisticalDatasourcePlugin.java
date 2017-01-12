package fi.nls.oskari.control.statistics.plugins.pxweb;

import fi.nls.oskari.control.statistics.plugins.AbstractStatisticalDatasourcePlugin;
import fi.nls.oskari.control.statistics.plugins.StatisticalIndicator;
import fi.nls.oskari.control.statistics.plugins.db.DatasourceLayer;
import fi.nls.oskari.control.statistics.plugins.db.StatisticalDatasource;
import fi.nls.oskari.control.statistics.plugins.pxweb.parser.PxwebIndicator;
import fi.nls.oskari.control.statistics.plugins.pxweb.parser.PxwebIndicatorsParser;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;

import java.util.Collections;
import java.util.List;

public class PxwebStatisticalDatasourcePlugin extends AbstractStatisticalDatasourcePlugin {
    private final static Logger LOG = LogFactory.getLogger(PxwebStatisticalDatasourcePlugin.class);
    private PxwebIndicatorsParser indicatorsParser;

    private List<DatasourceLayer> layers;
    private PxwebConfig config;


    @Override
    public List<? extends StatisticalIndicator> getIndicators(User user) {
        try {
            List<PxwebIndicator> indicators = indicatorsParser.parse(layers);
            return indicators;
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    @Override
    public void init(StatisticalDatasource source) {
        super.init(source);
        layers = source.getLayers();

        config = new PxwebConfig(source.getConfigJSON(), source.getId());
        indicatorsParser = new PxwebIndicatorsParser(config);

        LOG.debug("pxweb layer mappings: ", layers);
    }
}
