package org.oskari.statistics.plugins.unsd;

import fi.nls.oskari.control.statistics.data.IndicatorValue;
import fi.nls.oskari.control.statistics.data.IndicatorValueFloat;
import fi.nls.oskari.control.statistics.data.StatisticalIndicatorDataModel;
import fi.nls.oskari.control.statistics.plugins.APIException;
import fi.nls.oskari.util.JSONHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class UnsdDataParser {
    private UnsdConfig config;

    public UnsdDataParser(UnsdConfig config) {
        this.config = config;
    }

    /**
     * @param selectors        Used to query UNSD with.
     * @param indicator        The indicator we want.
     * @param areaCodes        List of areas we are interested in
     * @return
     */
    public Map<String, IndicatorValue> get(StatisticalIndicatorDataModel selectors, String indicator, String[] areaCodes) {
        UnsdRequest request = new UnsdRequest(config);
        request.setIndicator(indicator);
        request.setAreaCodes(areaCodes);

        Map<String, IndicatorValue> result = new HashMap<>();
        try {
            while(true) {
                JSONObject response = JSONHelper.createJSONObject(request.getIndicatorData(selectors));
                result.putAll(parseIndicatorData(response));
                if (isLastPage(response)) {
                    break;
                }
                request.nextPage();
            }
        } catch (JSONException ex) {
            throw new APIException("Error during parsing UNSD data response.", ex);
        }
        return result;
    }

    protected static boolean isLastPage (JSONObject response) throws JSONException {
        int totalPages = response.getInt("totalPages");
        if (totalPages < 1) {
            return true;
        }
        int pageNumber = response.getInt("pageNumber");
        return pageNumber >= totalPages;
    }

    protected static Map<String, IndicatorValue> parseIndicatorData (JSONObject response) throws JSONException {
        Map<String, IndicatorValue> results = new HashMap<>();
        JSONArray dataArray = response.getJSONArray("data");
        for (int i = 0; i < dataArray.length(); i++) {
            JSONObject data = dataArray.getJSONObject(i);
            results.put(data.getString("geoAreaCode"), new IndicatorValueFloat(data.getDouble("value")));
        }
        return results;
    }
}
