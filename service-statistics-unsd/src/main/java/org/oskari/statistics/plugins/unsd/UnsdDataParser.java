package org.oskari.statistics.plugins.unsd;

import java.util.*;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import fi.nls.oskari.control.statistics.data.IdNamePair;
import fi.nls.oskari.control.statistics.data.IndicatorValue;
import fi.nls.oskari.control.statistics.data.IndicatorValueFloat;
import fi.nls.oskari.control.statistics.data.StatisticalIndicatorDataDimension;
import fi.nls.oskari.control.statistics.data.StatisticalIndicatorDataModel;
import fi.nls.oskari.control.statistics.plugins.APIException;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.JSONHelper;

public class UnsdDataParser {

    private static final String DATA_KEY = "data";
    private static final String TIME_PERIOD_START_KEY = "timePeriodStart";
    private static final String GEO_AREA_CODE_KEY = "geoAreaCode";
    private static final String VALUE_TYPE_KEY  = "valueType";
    private static final String SUPPORTED_VALUE_TYPE = "Float";
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
            String valueType = data.optString(VALUE_TYPE_KEY);
            if(dataObjectValueTypeIsSupported(valueType)){
                results.put(data.getString(GEO_AREA_CODE_KEY), new IndicatorValueFloat(data.getDouble("value")));
            } else {
                LOG.error(String.format("Not supported valueType %s received.",valueType));
            }
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
            String indicator, String[] areaCodes) {
        UnsdRequest request = new UnsdRequest(config);
        request.setIndicator(indicator);
        request.setAreaCodes(areaCodes);

        StatisticalIndicatorDataDimension selector = new StatisticalIndicatorDataDimension(timePeriodDimensionId);
        Map<Integer,  Set<Integer>> geoAreaCodesForYears = new HashMap<>();

        try {
            while (true) {
                JSONObject response = JSONHelper.createJSONObject(request.getIndicatorData(null));
                parseTimePeriod(geoAreaCodesForYears, response);
                if (isLastPage(response)) {
                    break;
                }
                request.nextPage();
            }

            List<IdNamePair> allowedValues = getSortedListOfYearsThatBelongToSeveralGeoAreas(
                    geoAreaCodesForYears);

            selector.setAllowedValues(allowedValues);
            return selector;

        } catch (JSONException ex) {
            throw new APIException("Error during parsing UNSD data response.", ex);
        }
    }

    public static void parseTimePeriod(Map<Integer, Set<Integer>> geoAreaCodesForYears, JSONObject response) {
        try {
            JSONArray data = response.getJSONArray(DATA_KEY);
            for (int i = 0; i < data.length(); i++) {
                JSONObject o = (JSONObject) data.get(i);
                String valueType = o.optString(VALUE_TYPE_KEY);
 
                if(dataObjectValueTypeIsSupported(valueType)) {
                    Integer year = o.getInt(TIME_PERIOD_START_KEY);
                    Integer geoAreaCode = o.getInt(GEO_AREA_CODE_KEY);
                    geoAreaCodesForYears.computeIfAbsent(year, k -> new HashSet<>()).add(geoAreaCode);
                } else {
                    LOG.error(String.format("Not supported valueType %s received.",valueType));
                }
            }
        } catch (JSONException e) {
            LOG.error("Error parsing time period selectors for indicator: " + e.getMessage(), e);
        }
    }
    
    private static boolean dataObjectValueTypeIsSupported(String valueType) {
        return SUPPORTED_VALUE_TYPE.equals(valueType);
    }

    public static List<IdNamePair> getSortedListOfYearsThatBelongToSeveralGeoAreas(
            Map<Integer,  Set<Integer>> countOfAreaCodesForYear) {
        return countOfAreaCodesForYear.entrySet().stream()
                .filter(e -> e.getValue().size() > 1)
                .map(e -> e.getKey())
                .sorted(Comparator.reverseOrder())
                .map(year -> new IdNamePair(String.valueOf(year), null))
                .collect(Collectors.toList());
    }
}
