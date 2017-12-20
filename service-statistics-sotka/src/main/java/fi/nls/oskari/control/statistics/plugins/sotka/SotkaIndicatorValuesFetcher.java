package fi.nls.oskari.control.statistics.plugins.sotka;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import fi.nls.oskari.control.statistics.data.IndicatorValueFloat;
import fi.nls.oskari.control.statistics.plugins.sotka.requests.IndicatorDataJSON;
import fi.nls.oskari.service.ServiceRuntimeException;
import org.json.JSONArray;
import org.json.JSONException;

import fi.nls.oskari.control.statistics.plugins.APIException;
import fi.nls.oskari.control.statistics.data.IndicatorValue;
import fi.nls.oskari.control.statistics.data.StatisticalIndicatorDataDimension;
import fi.nls.oskari.control.statistics.data.StatisticalIndicatorDataModel;
import fi.nls.oskari.control.statistics.plugins.sotka.parser.SotkaRegionParser;
import fi.nls.oskari.control.statistics.plugins.sotka.requests.SotkaRequest;
import org.json.JSONObject;

/**
 * This fetches the indicator value tables transparently from Sotka.
 * We don't want to make a separate call to the plugin interface for this, because some
 * APIs / plugins might give all the information in the same response, or divide and key the responses differently.
 */
public class SotkaIndicatorValuesFetcher {
    private SotkaRegionParser regionParser;
    private SotkaConfig config;

    public void init(SotkaConfig config) {
        this.config = config;
        // We need to filter by region category and index by codes. Codes are unique within
        if (this.regionParser == null) {
            this.regionParser = new SotkaRegionParser(config);
        }
        regionParser.getData();
    }

    /**
     * @param selectors        Used to query SotkaNET with.
     * @param indicator        The indicator we want.
     * @param regionCategoryId The oskari layer we are interested in. For example: "KUNTA"
     * @return
     */
    public Map<String, IndicatorValue> get(StatisticalIndicatorDataModel selectors, String indicator,
                                           String regionCategoryId) {
        if(regionCategoryId == null) {
            throw new ServiceRuntimeException("Unknown regionset");
        }
        Map<Integer, IndicatorValue> allValues = fetchDataForAllRegionsets(selectors, indicator);
        Map<String, IndicatorValue> filteredValues = new HashMap<>();
        for (Entry<Integer, IndicatorValue> entry : allValues.entrySet()) {
            Integer sotkaRegionId = entry.getKey();
            // SotkaNET gives "Kunta" in some places, "KUNTA" in others... type is determined for each region
            String category = regionParser.getCategoryById(sotkaRegionId);
            if (!regionCategoryId.equalsIgnoreCase(category)) {
                // include only regions belonging to the requested regionset
                continue;
            }
            // Map sotkanet internal region id to the region id in Oskari regionset/layer
            String oskariRegionId = regionParser.getCode(sotkaRegionId);
            if (oskariRegionId != null) {
                // only add ones that we can successfully map to the regionset
                filteredValues.put(oskariRegionId, entry.getValue());
            }
        }
        return filteredValues;
    }

    /**
     * This returns the indicator data for all the layers, for every region category, from "maakunta" to "kunta", etc.
     * Note: Indexed by id, because id is unique, code is not.
     *
     * @param selectors
     * @param indicator
     * @return
     */
    protected Map<Integer, IndicatorValue> fetchDataForAllRegionsets(StatisticalIndicatorDataModel selectors, String indicator) {
        SotkaRequest request = SotkaRequest.getInstance(IndicatorDataJSON.NAME);
        request.setBaseURL(config.getUrl());
        // If there is no defined values for gender or year, we will use "total" and an empty list.
        String gender = "total";
        List<String> years = new ArrayList<>();
        for (StatisticalIndicatorDataDimension selector : selectors.getDimensions()) {
            switch (selector.getId()) {
                case "sex":
                    gender = selector.getValue();
                    break;
                case "year":
                    // Even though SotkaNET API supports fetching multiple years the current frontend implementation
                    // only sends one year. Parsing expects we request data for only one year.
                    years.add(selector.getValue());
                    break;
                default:
            }
        }
        try {
            request.setGender(gender);
            String[] yearsArray = years.toArray(new String[years.size()]);
            request.setYears(yearsArray);
            request.setIndicator(indicator);
            return parseJSON(request.getData());
        } catch (JSONException e) {
            throw new APIException("Something went wrong parsing JSON from SotkaNET getIndicatorValues API.", e);
        }
    }

    /**
     * The response is indexed by region id.
     *
     * @param response
     * @return
     * @throws JSONException
     */
    private Map<Integer, IndicatorValue> parseJSON(String response) throws JSONException {
        Map<Integer, IndicatorValue> indicatorMap = new HashMap<>();
        // The response is a String JSON array with JSONObjects
        JSONArray responseArray = new JSONArray(response);
        for (int i = 0; i < responseArray.length(); i++) {
            // Example row: {"indicator" : 4,"region": 231,"year": 2012,"gender": "total","value": 3.4,"absValue": 9}
            JSONObject valueRow = responseArray.getJSONObject(i);
            Double doubleValue = valueRow.optDouble("value");
            // sotkaRegionId is NOT the same as region id. It needs to be mapped to a region id later in the code
            int sotkaRegionId = valueRow.optInt("region", -1);
            if (sotkaRegionId != -1 && Double.NaN != doubleValue) {
                indicatorMap.put(sotkaRegionId, new IndicatorValueFloat(doubleValue));
            }
        }
        return indicatorMap;
    }
}
