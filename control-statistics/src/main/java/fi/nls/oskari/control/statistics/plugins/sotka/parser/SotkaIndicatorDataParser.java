package fi.nls.oskari.control.statistics.plugins.sotka.parser;

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

public class SotkaIndicatorDataParser {
    private final static Logger LOG = LogFactory.getLogger(SotkaIndicatorDataParser.class);

    public SotkaIndicatorDataParser() {
    }
    
    /**
     * The response is indexed by region id.
     * @param response
     * @return
     * @throws JSONException
     */
    public Map<Integer, IndicatorValue> parse(String response) throws JSONException {
        Map<Integer, IndicatorValue> indicatorMap = new HashMap<>();
        // The response is a String JSON array with JSONObjects with attributes:
        // "region", "indicator", "primary value", "gender", "year", "absolute value"

        JSONArray responseArray = new JSONArray(response);
        for (int i = 0; i < responseArray.length(); i++) {
            // Example row:
            // {"region":"117","indicator":"4","primary value":"6,0","gender":"male","year":"1996","absolute value":"16"}
            JSONObject valueRow = responseArray.getJSONObject(i);
            String value = valueRow.getString("primary value");
            // TODO: We are ignoring the absolute value. This could be handled as a separate indicator.
            try {
                // The numbers are in Finnish format, so we will convert if necessary.
                // Note: We will interpret integers also as floats, because some indicators give integers, others floats.
                // TODO: It might be possible to use heuristics to map the SotkaNET number types to floats and integers,
                //       but this would easily break with new indicators, considering the nature of Sotka types.
                Number numberValue = NumberFormat.getNumberInstance(Locale.forLanguageTag("fi_FI")).parse(value);
                IndicatorValue indicatorValue = new IndicatorValueFloat(numberValue.doubleValue());
                Integer id = Integer.valueOf(valueRow.getString("region"));
                indicatorMap.put(id, indicatorValue);
            } catch (ParseException e) {
                e.printStackTrace();
                throw new APIException("Unable to parse the numbers in response: " + response);
            }
        }
        return indicatorMap;
    }
}
