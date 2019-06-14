package org.oskari.statistics.plugins.unsd;

import fi.nls.oskari.control.statistics.data.IdNamePair;
import fi.nls.oskari.control.statistics.data.IndicatorValue;
import fi.nls.oskari.control.statistics.data.IndicatorValueFloat;
import fi.nls.oskari.control.statistics.data.StatisticalIndicatorDataDimension;
import fi.nls.oskari.control.statistics.data.StatisticalIndicatorDataModel;
import fi.nls.oskari.control.statistics.plugins.APIException;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.JSONHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UnsdDataParser {

    private static final String DATA_KEY = "data";
    private static final String TIME_PERIOD_START_KEY = "timePeriodStart";
    private static final Logger LOG = LogFactory.getLogger(UnsdDataParser.class);

    private UnsdConfig config;

    public UnsdDataParser(UnsdConfig config) {
        this.config = config;
    }

    protected static boolean isLastPage(JSONObject response) throws JSONException {
        int totalPages = response.getInt("totalPages");
        if (totalPages < 1) {
            return true;
        }
        int pageNumber = response.getInt("pageNumber");
        return pageNumber >= totalPages;
    }

    protected static Map<String, IndicatorValue> parseIndicatorData(JSONObject response) throws JSONException {
        Map<String, IndicatorValue> results = new HashMap<>();
        JSONArray dataArray = response.getJSONArray("data");
        for (int i = 0; i < dataArray.length(); i++) {
            JSONObject data = dataArray.getJSONObject(i);
            results.put(data.getString("geoAreaCode"), new IndicatorValueFloat(data.getDouble("value")));
        }
        return results;
    }

    /**
     * @param selectors Used to query UNSD with.
     * @param indicator The indicator we want.
     * @param areaCodes List of areas we are interested in
     * @return
     */
    public Map<String, IndicatorValue> get(StatisticalIndicatorDataModel selectors, String indicator, String[] areaCodes) {
        UnsdRequest request = new UnsdRequest(config);
        request.setIndicator(indicator);
        request.setAreaCodes(areaCodes);

        Map<String, IndicatorValue> result = new HashMap<>();
        try {
            while (true) {
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

    public StatisticalIndicatorDataDimension getTimeperiodDimensionFromIndicatorData(String timePeriodDimensionId,
            String indicator) {
        UnsdRequest request = new UnsdRequest(config);
        request.setIndicator(indicator);

        StatisticalIndicatorDataDimension selector = new StatisticalIndicatorDataDimension(timePeriodDimensionId);
        Map<Integer, Integer> countOfAreaCodesForYear = new HashMap<>();

        try {
            while (true) {
                JSONObject response = JSONHelper.createJSONObject(request.getIndicatorData(null));
                parseTimePeriod(countOfAreaCodesForYear, response);
                if (isLastPage(response)) {
                    break;
                }
                request.nextPage();
            }

            List<IdNamePair> allowedValues = getSortedListOfYearsThatBelongToSeveralGeoAreas(
                    countOfAreaCodesForYear);

            selector.setAllowedValues(allowedValues);
            return selector;

        } catch (JSONException ex) {
            throw new APIException("Error during parsing UNSD data response.", ex);
        }
    }

    public static void parseTimePeriod(Map<Integer, Integer> countOfAreaCodesForYear, JSONObject response) {
        try {
            JSONArray data = response.getJSONArray(DATA_KEY);
            for (int i = 0; i < data.length(); i++) {
                JSONObject o = (JSONObject) data.get(i);
                Integer year = getTimeperiodYear(o);

                if (countOfAreaCodesForYear.containsKey(year)) {
                    Integer count = countOfAreaCodesForYear.get(year);
                    count++;
                    countOfAreaCodesForYear.put(year, count);
                } else {
                    countOfAreaCodesForYear.put(year, 1);
                }
            }
        } catch (JSONException e) {
            LOG.error("Error parsing time period selectors for indicator: " + e.getMessage(), e);
        }
    }

    private static Integer getTimeperiodYear(JSONObject o) throws JSONException {
        return ((Double) o.get(TIME_PERIOD_START_KEY)).intValue();
    }

    private List<IdNamePair> getSortedListOfYearsThatBelongToSeveralGeoAreas(
            Map<Integer, Integer> countOfAreaCodesForYear) {
        return (List<IdNamePair>) (countOfAreaCodesForYear.entrySet().stream().filter(e -> e.getValue() > 1)
                .map(e -> new IdNamePair(String.valueOf(e.getKey()), null))).sorted().collect(Collectors.toList());
    }
}
