package fi.nls.oskari.control.statistics.plugins.pxweb;

import fi.nls.oskari.control.statistics.plugins.StatisticalDatasourcePlugin;
import fi.nls.oskari.control.statistics.plugins.StatisticalIndicator;
import fi.nls.oskari.control.statistics.plugins.db.DatasourceLayer;
import fi.nls.oskari.control.statistics.plugins.db.StatisticalDatasource;
import fi.nls.oskari.control.statistics.plugins.pxweb.parser.PxwebIndicator;
import fi.nls.oskari.control.statistics.plugins.pxweb.parser.PxwebIndicatorsParser;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.IOHelper;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PxwebStatisticalDatasourcePlugin implements StatisticalDatasourcePlugin {
    private final static Logger LOG = LogFactory.getLogger(PxwebStatisticalDatasourcePlugin.class);
    private PxwebIndicatorsParser indicatorsParser;

    private Map<String, Long> layerMappings;
    private PxwebConfig config;


    @Override
    public List<? extends StatisticalIndicator> getIndicators(User user) {
        try {
            List<PxwebIndicator> indicators = indicatorsParser.parse(layerMappings);
            return indicators;
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    @Override
    public void init(StatisticalDatasource source) {
        final List<DatasourceLayer> layerRows = source.getLayers();
        layerMappings = new HashMap<>();
        config = new PxwebConfig(source.getConfigJSON());
        indicatorsParser = new PxwebIndicatorsParser(config);

        for (DatasourceLayer row : layerRows) {
            layerMappings.put(row.getSourceProperty().toLowerCase(), row.getMaplayerId());
        }
        LOG.debug("pxweb layer mappings: ", layerMappings);
    }

    @Override
    public boolean canCache() {
        return true;
    }
}
