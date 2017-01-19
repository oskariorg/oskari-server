package fi.nls.oskari.control.statistics.plugins.sotka;

import fi.nls.oskari.control.statistics.data.IndicatorValue;
import fi.nls.oskari.control.statistics.data.StatisticalIndicator;
import fi.nls.oskari.control.statistics.data.StatisticalIndicatorDataModel;
import fi.nls.oskari.control.statistics.data.StatisticalIndicatorLayer;
import fi.nls.oskari.control.statistics.plugins.StatisticalDatasourcePlugin;
import fi.nls.oskari.control.statistics.plugins.db.DatasourceLayer;
import fi.nls.oskari.control.statistics.plugins.db.StatisticalDatasource;
import fi.nls.oskari.control.statistics.plugins.sotka.parser.SotkaIndicatorParser;
import fi.nls.oskari.control.statistics.plugins.sotka.requests.Indicators;
import fi.nls.oskari.control.statistics.plugins.sotka.requests.SotkaRequest;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SotkaStatisticalDatasourcePlugin extends StatisticalDatasourcePlugin {
    private final static Logger LOG = LogFactory.getLogger(SotkaStatisticalDatasourcePlugin.class);

    private SotkaConfig config;
    private SotkaIndicatorValuesFetcher indicatorValuesFetcher = new SotkaIndicatorValuesFetcher();

    /**
     * Maps the SotkaNET layer identifiers to Oskari layers.
     */
    private Map<String, Long> sotkaToLayerMappings;
    private Map<Long, String> layerToSotkaMappings;

    public SotkaStatisticalDatasourcePlugin() {
    }

    @Override
    public void update() {
        // get the indicator listing
        SotkaRequest request = SotkaRequest.getInstance(Indicators.NAME);
        request.setBaseURL(config.getUrl());
        String data  = request.getData();
        // parse data
        try {
            JSONArray responseJSON = new JSONArray(data);
            SotkaIndicatorParser parser = new SotkaIndicatorParser(config);
            LOG.info("Parsing indicator response of length: " + responseJSON.length());
            for (int i = 0; i < responseJSON.length(); i++) {
                StatisticalIndicator indicator = parser.parse(responseJSON.getJSONObject(i), sotkaToLayerMappings);
                if(indicator != null) {
                    onIndicatorProcessed(indicator);
                }
            }
            LOG.info("Parsed indicator response.");
        } catch (JSONException e) {
            LOG.error("Error in mapping Sotka Indicators response to Oskari model: " + e.getMessage(), e);
        }
    }

    @Override
    public void init(StatisticalDatasource source) {
        super.init(source);
        config = new SotkaConfig(source.getConfigJSON(), source.getId());
        final List<DatasourceLayer> layerRows = source.getLayers();
        sotkaToLayerMappings = new HashMap<>();
        layerToSotkaMappings = new HashMap<>();

        for (DatasourceLayer layer : layerRows) {
            sotkaToLayerMappings.put(layer.getConfig("regionType").toLowerCase(), layer.getMaplayerId());
            layerToSotkaMappings.put(layer.getMaplayerId(), layer.getConfig("regionType").toLowerCase());
        }
        indicatorValuesFetcher.init(config);
        LOG.debug("SotkaNET layer mappings: ", sotkaToLayerMappings);
    }

    @Override
    public Map<String, IndicatorValue> getIndicatorValues(StatisticalIndicator indicator, StatisticalIndicatorDataModel params, StatisticalIndicatorLayer regionset) {
        String sotkaRegionsetName = layerToSotkaMappings.get(regionset.getOskariLayerId());
        return indicatorValuesFetcher.get(params, indicator.getId(), sotkaRegionsetName);
    }
}
