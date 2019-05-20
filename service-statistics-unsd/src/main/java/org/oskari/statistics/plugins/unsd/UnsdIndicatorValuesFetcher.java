package org.oskari.statistics.plugins.unsd;

import fi.nls.oskari.control.statistics.data.IndicatorValue;
import fi.nls.oskari.control.statistics.data.IndicatorValueFloat;
import fi.nls.oskari.control.statistics.data.StatisticalIndicatorDataModel;
import fi.nls.oskari.control.statistics.plugins.APIException;
import fi.nls.oskari.service.ServiceRuntimeException;
import fi.nls.oskari.util.JSONHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class UnsdIndicatorValuesFetcher {
    private UnsdConfig config;

    public void init(UnsdConfig config) {
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
        boolean allFetched = false;
        String response;
        while(!allFetched) {
            try {
                response = request.getIndicatorData(selectors);
                result.putAll(parseIndicatorData(response));
                allFetched = isLastPage(response);
                if (!allFetched) {
                    request.nextPage();
                    response = request.getIndicatorData(selectors);
                }
            } catch (JSONException ex) {
                throw new APIException("Error during parsing UNSD data response.", ex);
            }
        }
        return result;
    }

    protected boolean isLastPage (String indicatorDataResponse) {
        try {
            JSONObject response = JSONHelper.createJSONObject(indicatorDataResponse);
            int totalPages = response.getInt("totalPages");
            if (totalPages < 1) {
                return true;
            }
            int pageNumber = response.getInt("pageNumber");
            return pageNumber == totalPages;
        } catch (JSONException e) {
            throw new ServiceRuntimeException("Error parsing UNSD indicator data page info: " + e.getMessage(), e);
        }
    }

    protected Map<String, IndicatorValue> parseIndicatorData (String indicatorDataResponse) throws JSONException {
        Map<String, IndicatorValue> results = new HashMap<>();
        JSONObject response = JSONHelper.createJSONObject(indicatorDataResponse);
        JSONArray dataArray = response.getJSONArray("data");
        for (int i = 0; i < dataArray.length(); i++) {
            JSONObject data = dataArray.getJSONObject(i);
            results.put(data.getString("geoAreaCode"), new IndicatorValueFloat(data.getDouble("value")));
        }
        return results;
    }
}
