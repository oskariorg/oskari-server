package fi.nls.oskari.control.statistics.plugins.kapa.parser;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import fi.nls.oskari.control.statistics.plugins.APIException;
import fi.nls.oskari.control.statistics.plugins.IndicatorValue;
import fi.nls.oskari.control.statistics.plugins.IndicatorValueFloat;
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
            // {"region":"117","value":"16.0"}
            JSONObject valueRow = responseArray.getJSONObject(i);
            String value = valueRow.getString("value");
            try {
                // The numbers are in point-separated format.
                // This is convenient, because Javascript always uses point as decimal separator,
                // which is why we don't use the Finnish locale here.
                Number numberValue = NumberFormat.getNumberInstance(Locale.ENGLISH).parse(value);
                IndicatorValue indicatorValue = new IndicatorValueFloat(numberValue.doubleValue());
                String regionId = valueRow.getString("region");
                indicatorMap.put(regionId, indicatorValue);
            } catch (ParseException e) {
                e.printStackTrace();
                throw new APIException("Unable to parse the numbers in response: " + response);
            }
        }
        return indicatorMap;
    }
}
