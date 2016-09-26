package fi.nls.oskari.statistics.eurostat;

import fi.nls.oskari.control.statistics.plugins.StatisticalDatasourcePlugin;
import fi.nls.oskari.control.statistics.plugins.StatisticalIndicator;
import fi.nls.oskari.control.statistics.plugins.db.DatasourceLayer;
import fi.nls.oskari.control.statistics.plugins.db.StatisticalDatasource;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EurostatStatisticalDatasourcePlugin implements StatisticalDatasourcePlugin {
    private final static Logger LOG = LogFactory.getLogger(EurostatStatisticalDatasourcePlugin.class);

    /**
     * Maps the Eurostat layer identifiers to Oskari layers.
     */
    private Map<String, Long> layerMappings;

    @Override
    public List<? extends StatisticalIndicator> getIndicators(User user) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public void init(StatisticalDatasource source) {
        final List<DatasourceLayer> layerRows = source.getLayers();
        layerMappings = new HashMap<>();

        for (DatasourceLayer row : layerRows) {
            layerMappings.put(row.getSourceProperty().toLowerCase(), row.getMaplayerId());
        }
        LOG.debug("Eurostat layer mappings: ", layerMappings);
    }

    @Override
    public boolean canCache() {
        return true;
    }
}
