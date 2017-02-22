package fi.nls.oskari.control.statistics.plugins.kapa.parser;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import fi.nls.oskari.control.statistics.data.IndicatorValue;
import fi.nls.oskari.control.statistics.data.IndicatorValueFloat;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;

public class KapaIndicatorDataParser {
    private final static Logger LOG = LogFactory.getLogger(KapaIndicatorDataParser.class);

    public KapaIndicatorDataParser() {
    }
    
    public Map<String, IndicatorValue> parse(String response) throws JSONException {
        Map<String, IndicatorValue> indicatorMap = new HashMap<>();
        // The response is a String JSON array with JSONObjects with attributes:
        // "region", "primary value"

        JSONArray responseArray = new JSONArray(response);
        for (int i = 0; i < responseArray.length(); i++) {
            // Example row:
            // {"region": "117","value": 16.0}
            JSONObject valueRow = responseArray.getJSONObject(i);
            Double value = valueRow.getDouble("value");
            // The numbers are in point-separated format in JSON.
            IndicatorValue indicatorValue = new IndicatorValueFloat(value);
            String regionId = valueRow.getString("region");
            indicatorMap.put(regionId, indicatorValue);
        }
        return indicatorMap;
    }
}
