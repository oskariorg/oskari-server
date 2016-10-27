package fi.nls.oskari.statistics.eurostat;

import fi.nls.oskari.control.statistics.plugins.*;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.JSONHelper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class EurostatStatisticalIndicatorLayer implements StatisticalIndicatorLayer {

    private long id;
    private String indicatorId;
    private String baseUrl;
    private String regionKey;

    public EurostatStatisticalIndicatorLayer(long id, String indicatorId, String baseUrl) {
        this.id = id;
        this.indicatorId = indicatorId;
        this.baseUrl = baseUrl;
        this.regionKey = "geo";
    }

    @Override
    public long getOskariLayerId() {
        return id;
    }

    @Override
    public IndicatorValueType getIndicatorValueType() {
        return null;
    }

    @Override
    public Map<String, IndicatorValue> getIndicatorValues(StatisticalIndicatorSelectors selectors) {
        Map<String, String> params = new HashMap<>();
        for (StatisticalIndicatorSelector selector : selectors.getSelectors()) {
            if (regionKey.equalsIgnoreCase(selector.getId())) {
                // skip Alue
                continue;
            }
            params.put(selector.getId(), selector.getValue());
        }
        String url = IOHelper.constructUrl(baseUrl +"/wdds/rest/data/v2.1/json/en/" + indicatorId, params);

        Map<String, IndicatorValue> values = new HashMap<>();
        try {
            final String data = IOHelper.getURL(url);
            // TODO: parsing
            JSONObject json = JSONHelper.createJSONObject(data);
            JSONObject stats = json.optJSONObject("dimension").optJSONObject(regionKey).optJSONObject("category").optJSONObject("index"); // pass region Key  to geo
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
