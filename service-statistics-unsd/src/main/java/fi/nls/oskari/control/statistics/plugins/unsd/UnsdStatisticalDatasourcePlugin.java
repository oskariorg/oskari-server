package fi.nls.oskari.control.statistics.plugins.unsd;

import fi.nls.oskari.control.statistics.data.*;
import fi.nls.oskari.control.statistics.plugins.StatisticalDatasourcePlugin;
import fi.nls.oskari.control.statistics.plugins.db.DatasourceLayer;
import fi.nls.oskari.control.statistics.plugins.db.StatisticalDatasource;
import fi.nls.oskari.control.statistics.plugins.unsd.parser.RegionMapper;
import fi.nls.oskari.control.statistics.plugins.unsd.parser.UnsdParser;
import fi.nls.oskari.control.statistics.plugins.unsd.requests.UnsdRequest;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.JSONHelper;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UnsdStatisticalDatasourcePlugin extends StatisticalDatasourcePlugin {
    private final static Logger LOG = LogFactory.getLogger(UnsdStatisticalDatasourcePlugin.class);

    private UnsdConfig config;
    private UnsdParser parser;
    private UnsdIndicatorValuesFetcher indicatorValuesFetcher;
    private RegionMapper regionMapper;

    /**
     * Maps the UNSD area codes to Oskari layers.
     */
    private Map<Long, String[]> layerAreaCodes = new HashMap<>();

    @Override
    public void update() {
        // get the indicator listing
        UnsdRequest request = new UnsdRequest(config);
        request.setGoal(config.getGoal());
        String targetsResponse = request.getTargets();
        List<StatisticalIndicator> indicators = parser.parseIndicators(targetsResponse);

        // all indicators under goal have same dimensions
        String dimensions = request.getDimensions();
        for (StatisticalIndicator ind : indicators) {
            request.setIndicator(ind.getId());
            // we parse it multiple times to make copies
            ind.setDataModel(parser.parseDimensions(dimensions));
            ind.setSource(parser.parseSource(request.getIndicatorData(null)));
            getSource().getLayers().stream().forEach(l -> ind.addLayer(l));
            onIndicatorProcessed(ind);
        }
        LOG.info("Parsed indicator response.");
    }

    @Override
    public void init(StatisticalDatasource source) {
        super.init(source);
        config = new UnsdConfig(source.getConfigJSON(), source.getId());
        parser = new UnsdParser();
        indicatorValuesFetcher = new UnsdIndicatorValuesFetcher();
        indicatorValuesFetcher.init(config);
        regionMapper = new RegionMapper();
        /*
        try {
            // optimization for getting just the countries we need
            initAreaCodes(source.getLayers());
        } catch (JSONException e) {
            LOG.error("Error parsing UNSD statistical layer regions: " + e.getMessage(), e);
        }
        */
    }

    private void initAreaCodes (List<DatasourceLayer> layers) throws JSONException {
        for (DatasourceLayer layer : layers) {
            JSONArray regions = JSONHelper.createJSONArray(layer.getConfig("regions"));
            if (regions == null) {
                return;
            }
            List<String> areaCodes = new ArrayList<>();
            for (int i = 0; i < 0; i++) {
                areaCodes.add(regions.getString(i));
            }
            // FIXME: need to map country code to a digit in the datasource......
            // TODO; Get codes from RegionSetHelper
            // RegionSet - layerId
            // RegionSetHelper - RegionSet
            layerAreaCodes.put(layer.getMaplayerId(), areaCodes.stream()
                    .map(code -> regionMapper.getUNSDAreaCode(code))
                    .filter(code -> !code.isEmpty())
                    .collect(Collectors.toList())
            );
        }
    }

    @Override
    public Map<String, IndicatorValue> getIndicatorValues(
            StatisticalIndicator indicator,
            StatisticalIndicatorDataModel params,
            StatisticalIndicatorLayer regionset) {

        String[] areaCodes = layerAreaCodes.get(regionset.getOskariLayerId());
        return indicatorValuesFetcher.get(params, indicator.getId(), areaCodes);
    }
}
