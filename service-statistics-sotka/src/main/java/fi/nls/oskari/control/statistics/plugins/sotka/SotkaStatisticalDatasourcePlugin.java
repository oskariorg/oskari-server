package fi.nls.oskari.control.statistics.plugins.sotka;

import fi.nls.oskari.control.statistics.plugins.APIException;
import fi.nls.oskari.control.statistics.plugins.StatisticalDatasourcePlugin;
import fi.nls.oskari.control.statistics.plugins.StatisticalIndicator;
import fi.nls.oskari.control.statistics.plugins.db.DatasourceLayer;
import fi.nls.oskari.control.statistics.plugins.db.StatisticalDatasource;
import fi.nls.oskari.control.statistics.plugins.sotka.parser.SotkaIndicatorsParser;
import fi.nls.oskari.control.statistics.plugins.sotka.requests.Indicators;
import fi.nls.oskari.control.statistics.plugins.sotka.requests.SotkaRequest;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SotkaStatisticalDatasourcePlugin implements StatisticalDatasourcePlugin {
    private final static Logger LOG = LogFactory.getLogger(SotkaStatisticalDatasourcePlugin.class);

    private SotkaIndicatorsParser indicatorsParser = null;
    private SotkaConfig config = new SotkaConfig();

    /**
     * Maps the SotkaNET layer identifiers to Oskari layers.
     */
    private Map<String, Long> layerMappings;

    public SotkaStatisticalDatasourcePlugin() {
        indicatorsParser = new SotkaIndicatorsParser();
    }

    @Override
    public List<? extends StatisticalIndicator> getIndicators(User user) {
        try {
            // First getting general information of all the indicator layers.
            // Note that some mandatory information about the layers is not given here,
            // for example the year range, but must be requested separately for each indicator.
            SotkaRequest request = SotkaRequest.getInstance(Indicators.NAME);
            request.setBaseURL(getBaseURL());
            String jsonResponse = request.getData();

            // We will later need to add the year range information to the preliminary information using separate requests.
            return indicatorsParser.parse(jsonResponse, layerMappings);
        } catch (APIException e) {
            throw e;
        } catch (Throwable e) {
            e.printStackTrace();
            throw new APIException("Something went wrong calling SotkaNET Indicators interface.", e);
        }
    }

    private void setupConfig(JSONObject obj) {
        if (obj == null) {
            return;
        }
        config.setUrl(obj.optString("url"));
    }

    String getBaseURL() {
        return config.getUrl();
    }

    @Override
    public void init(StatisticalDatasource source) {
        setupConfig(source.getConfigJSON());
        indicatorsParser.setConfig(config);
        final List<DatasourceLayer> layerRows = source.getLayers();
        layerMappings = new HashMap<>();

        for (DatasourceLayer row : layerRows) {
            layerMappings.put(row.getSourceProperty().toLowerCase(), row.getMaplayerId());
        }
        LOG.debug("SotkaNET layer mappings: ", layerMappings);
    }

    @Override
    public boolean canCache() {
        return true;
    }
}
