package fi.nls.oskari.control.statistics.plugins.sotka;

import fi.nls.oskari.cache.JedisManager;
import fi.nls.oskari.control.statistics.plugins.APIException;
import fi.nls.oskari.control.statistics.plugins.StatisticalDatasourcePlugin;
import fi.nls.oskari.control.statistics.data.StatisticalIndicator;
import fi.nls.oskari.control.statistics.plugins.db.DatasourceLayer;
import fi.nls.oskari.control.statistics.plugins.db.StatisticalDatasource;
import fi.nls.oskari.control.statistics.plugins.sotka.parser.SotkaIndicatorsParser;
import fi.nls.oskari.control.statistics.plugins.sotka.requests.Indicators;
import fi.nls.oskari.control.statistics.plugins.sotka.requests.SotkaRequest;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SotkaStatisticalDatasourcePlugin extends StatisticalDatasourcePlugin {
    private final static Logger LOG = LogFactory.getLogger(SotkaStatisticalDatasourcePlugin.class);

    private SotkaIndicatorsParser indicatorsParser = null;
    private SotkaConfig config;

    /**
     * Maps the SotkaNET layer identifiers to Oskari layers.
     */
    private Map<String, Long> layerMappings;

    public SotkaStatisticalDatasourcePlugin() {
        indicatorsParser = new SotkaIndicatorsParser();
    }

    @Override
    public void update() {
        List<StatisticalIndicator> indicators = getIndicators();
        for(StatisticalIndicator ind: indicators) {
            onIndicatorProcessed(ind);
        }
    }

    private List<StatisticalIndicator> getIndicators() {
        try {
            final String cacheKey = "stats:" + config.getId() + ":indicatorlist";
            String data = JedisManager.get(cacheKey + config.getUrl());

            if (data == null) {
                // First getting general information of all the indicator layers.
                // Note that some mandatory information about the layers is not given here,
                // for example the year range, but must be requested separately for each indicator.
                SotkaRequest request = SotkaRequest.getInstance(Indicators.NAME);
                request.setBaseURL(config.getUrl());
                data = request.getData();
                JedisManager.setex(cacheKey, JedisManager.EXPIRY_TIME_DAY, data);
            }

            // We will later need to add the year range information to the preliminary information using separate requests.
            return indicatorsParser.parse(data, layerMappings);
        } catch (APIException e) {
            throw e;
        } catch (Exception e) {
            throw new APIException("Something went wrong calling SotkaNET Indicators interface.", e);
        }
    }

    @Override
    public void init(StatisticalDatasource source) {
        super.init(source);
        config = new SotkaConfig(source.getConfigJSON(), source.getId());
        indicatorsParser.setConfig(config);
        final List<DatasourceLayer> layerRows = source.getLayers();
        layerMappings = new HashMap<>();

        for (DatasourceLayer layer : layerRows) {
            layerMappings.put(layer.getConfig("regionType").toLowerCase(), layer.getMaplayerId());
        }
        LOG.debug("SotkaNET layer mappings: ", layerMappings);
    }
}
