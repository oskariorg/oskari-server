package fi.nls.oskari.statistics.eurostat;

import fi.nls.oskari.control.statistics.data.*;
import fi.nls.oskari.control.statistics.plugins.APIException;
import fi.nls.oskari.control.statistics.plugins.StatisticalDatasourcePlugin;
import fi.nls.oskari.control.statistics.plugins.db.StatisticalDatasource;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.JSONHelper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class EurostatStatisticalDatasourcePlugin extends StatisticalDatasourcePlugin {
    private final static Logger LOG = LogFactory.getLogger(EurostatStatisticalDatasourcePlugin.class);
    private EurostatIndicatorsParser indicatorsParser;
    protected final static String KEY_REGION = "geo";

    private EurostatConfig config;

    @Override
    public void update() {
        indicatorsParser.parse(getSource().getLayers());
    }

    @Override
    public void init(StatisticalDatasource source) {
        super.init(source);
        try {
            config = new EurostatConfig(source.getConfigJSON(), source.getId());
            indicatorsParser = new EurostatIndicatorsParser(this, config);
        } catch (IOException e) {
            LOG.error(e, "Error getting indicators from Eurostat datasource:", config.getUrl());
        }
    }
    @Override
    public Map<String, IndicatorValue> getIndicatorValues(StatisticalIndicator indicator, StatisticalIndicatorDataModel params, StatisticalIndicatorLayer regionset) {
        String url = config.getURLforData(params, indicator.getId());
        Map<String, IndicatorValue> values = new HashMap<>();
        try {
            final String data = IOHelper.getURL(url);
            JSONObject json = JSONHelper.createJSONObject(data);
            JSONObject stats = json.optJSONObject("dimension").optJSONObject(KEY_REGION).optJSONObject("category").optJSONObject("index"); // pass region Key  to geo
            JSONObject responseValues = json.optJSONObject("value");
            JSONArray names = stats.names();
            for (int i = 0; i < names.length(); ++i) {
                String region = names.optString(i);
                Double val = responseValues.optDouble(""+stats.optInt(region)); // stats.optInt return index for the region
                if (val.isNaN()) {
                    continue;
                }
                IndicatorValue indicatorValue = new IndicatorValueFloat(val);
                values.put(region, indicatorValue);
            }
        } catch (IOException e) {
            throw new APIException("Couldn't get data from service/parsing failed", e);
        }

        return values;
    }
}

