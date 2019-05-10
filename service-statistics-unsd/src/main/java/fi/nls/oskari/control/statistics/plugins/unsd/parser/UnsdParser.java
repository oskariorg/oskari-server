package fi.nls.oskari.control.statistics.plugins.unsd.parser;

import fi.nls.oskari.control.statistics.data.IndicatorValue;
import fi.nls.oskari.control.statistics.data.IndicatorValueFloat;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.JSONHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.*;

public class UnsdParser {

    private final static Logger LOG = LogFactory.getLogger(UnsdParser.class);

    public List<UnsdIndicator> parseGoalIndicators(String targetListResponse) {
        List<UnsdIndicator> indicatorList = new ArrayList<>();
        try {
            JSONObject goal = getFirstObject(targetListResponse);
            JSONArray targets = goal.getJSONArray("targets");
            for (int i = 0; i < targets.length(); i++) {
                JSONObject target = targets.getJSONObject(i);
                indicatorList.addAll(parseIndicators(target.getJSONArray("indicators")));
            }
        } catch (JSONException e) {
            LOG.error("Error parsing indicator codes from UNSD Target List response: " + e.getMessage(), e);
        }
        return indicatorList;
    }

    public List<UnsdIndicator> parseIndicators(String indicatorsResponse) {
        try {
            JSONArray indicators = new JSONArray(indicatorsResponse);
            return parseIndicators(indicators);
        } catch (JSONException e) {
            LOG.error("Error parsing UNSD indicators: " + e.getMessage(), e);
        }
        return Collections.emptyList();
    }

    public List<UnsdIndicator> parseIndicators(JSONArray indicators) {
        List<UnsdIndicator> indicatorList = new ArrayList<>();
        try {
            for (int i = 0; i < indicators.length(); i++) {
                UnsdIndicator indicator = new UnsdIndicator();
                indicator.parse(indicators.getJSONObject(i));
                indicatorList.add(indicator);
            }
        } catch (JSONException e) {
            LOG.error("Error parsing UNSD indicators: " + e.getMessage(), e);
        }
        return indicatorList;
    }

    public void mergeDimensions(UnsdIndicator indicator, String dimensionsResponse) {
        try {
            JSONArray dimensions = JSONHelper.createJSONArray(dimensionsResponse);
            indicator.parseDimensions(dimensions);
        } catch (JSONException e) {
            LOG.error("Error parsing selectors for indicator: " + e.getMessage(), e);
        }
    }

    public void mergeSource(UnsdIndicator indicator, String indicatorDataResponse) {
        try {
            JSONObject jsonObject = JSONHelper.createJSONObject(indicatorDataResponse);
            if (!jsonObject.has("data")) {
                return;
            }
            JSONArray data = jsonObject.getJSONArray("data");
            if (data.length() == 0) {
                return;
            }
            indicator.parseSource(data.getJSONObject(0));
        } catch (JSONException e) {
            LOG.error("Error parsing source for indicator: " + e.getMessage(), e);
        }
    }

    public Boolean isLastPage (String indicatorDataResponse) {
        try {
            JSONObject response = JSONHelper.createJSONObject(indicatorDataResponse);
            int totalPages = response.getInt("totalPages");
            int pageNumber = response.getInt("pageNumber");
            return pageNumber == totalPages;
        } catch (JSONException e) {
            LOG.error("Error parsing UNSD indicator data page info: " + e.getMessage(), e);
        }
        return null;
    }

    public Map<String, IndicatorValue> parseIndicatorData (String indicatorDataResponse) throws JSONException {
        Map<String, IndicatorValue> results = new HashMap<>();
        JSONObject response = JSONHelper.createJSONObject(indicatorDataResponse);
        JSONArray dataArray = response.getJSONArray("data");
        for (int i = 0; i < dataArray.length(); i++) {
            JSONObject data = dataArray.getJSONObject(i);
            results.put(data.getString("geoAreaCode"), new IndicatorValueFloat(data.getDouble("value")));
        }
        return results;
    }

    public JSONObject getFirstObject (String json) throws JSONException {
        JSONObject obj;
        try {
            obj = new JSONObject(json);
        } catch (JSONException ex) {
            obj = JSONHelper.createJSONArray(json).getJSONObject(0);
        }
        return obj;
    }

}
