package fi.nls.oskari.control.statistics.plugins.kapa;

import fi.nls.oskari.control.statistics.data.IndicatorValue;
import fi.nls.oskari.control.statistics.data.StatisticalIndicatorDataModel;
import fi.nls.oskari.control.statistics.data.StatisticalIndicatorLayer;
import fi.nls.oskari.control.statistics.plugins.StatisticalDatasourcePlugin;
import fi.nls.oskari.control.statistics.data.StatisticalIndicator;
import fi.nls.oskari.control.statistics.plugins.db.DatasourceLayer;
import fi.nls.oskari.control.statistics.plugins.db.StatisticalDatasource;
import fi.nls.oskari.control.statistics.plugins.kapa.parser.KapaIndicatorsParser;
import fi.nls.oskari.control.statistics.plugins.kapa.requests.KapaRequest;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KapaStatisticalDatasourcePlugin extends StatisticalDatasourcePlugin {
    private final static Logger LOG = LogFactory.getLogger(KapaStatisticalDatasourcePlugin.class);
    private KapaIndicatorsParser indicatorsParser;

    private KapaIndicatorValuesFetcher indicatorValuesFetcher = new KapaIndicatorValuesFetcher();
    /**
     * Maps the KaPa layer identifiers to Oskari layers.
     */
    private Map<String, Long> layerMappings;

    public KapaStatisticalDatasourcePlugin() {
        indicatorsParser = new KapaIndicatorsParser();
    }

    @Override
    public void update() {
        KapaRequest request = new KapaRequest();
        String jsonResponse = request.getIndicators();
        List<StatisticalIndicator> indicators = indicatorsParser.parse(jsonResponse, layerMappings);
        for(StatisticalIndicator ind: indicators) {
            onIndicatorProcessed(ind);
        }
    }

    @Override
    public Map<String, IndicatorValue> getIndicatorValues(StatisticalIndicator indicator, StatisticalIndicatorDataModel params, StatisticalIndicatorLayer regionset) {
        return indicatorValuesFetcher.get(params, indicator.getId());
    }
    @Override
    public void init(StatisticalDatasource source) {
        super.init(source);
        // Fetching the layer mapping from the database.

        final List<DatasourceLayer> layerRows = source.getLayers();
        layerMappings = new HashMap<>();

        for (DatasourceLayer layer : layerRows) {
            layerMappings.put(layer.getConfig("regionType").toLowerCase(), layer.getMaplayerId());
        }
        indicatorValuesFetcher.init();
        LOG.debug("KaPa layer mappings: ", layerMappings);
    }
}
