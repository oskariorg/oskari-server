package fi.nls.oskari.control.statistics.plugins.unsd.parser;

import fi.nls.oskari.control.statistics.data.*;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.ServiceRuntimeException;
import fi.nls.oskari.util.JSONHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

public class UnsdParser {

    private final static Logger LOG = LogFactory.getLogger(UnsdParser.class);

    private static final String LANG = "en";

    public List<StatisticalIndicator> parseIndicators(String indicatorsResponse) {
        try {
            JSONArray indicators = new JSONArray(indicatorsResponse);
            return parseIndicators(indicators);
        } catch (JSONException e) {
            LOG.error("Error parsing UNSD indicators: " + e.getMessage(), e);
        }
        return Collections.emptyList();
    }

    public List<StatisticalIndicator> parseIndicators(JSONArray goals) {
        // array of goal.targets[target.indicators]
        List<StatisticalIndicator> indicatorList = new ArrayList<>();
        if (goals == null) {
            return indicatorList;
        }
        try {
            for (int i = 0; i < goals.length(); i++) {
                JSONObject item = goals.getJSONObject(i);
                parseIndicatorsResultItem(item)
                        .ifPresent(list -> indicatorList.addAll(list));
            }
        } catch (JSONException e) {
            LOG.error("Error parsing UNSD indicators: " + e.getMessage(), e);
        }
        return indicatorList;
    }

    private Optional<List<StatisticalIndicator>> parseIndicatorsResultItem(JSONObject item) {
        if (item == null) {
            return Optional.empty();
        }
        if (item.has("targets")) {
            // this is goal level
            return Optional.of(parseFromGoal(item));
        } else if (item.has("indicators")) {
            // target level
            return Optional.of(parseFromTarget(item, null));
        }
        return parseIndicator(item).map(ind -> Collections.singletonList(ind));
    }

    private boolean isIndicator(JSONObject item) {
        if (item == null) {
            return false;
        }
        String id = item.optString("code");
        if (id == null) {
            return false;
        }
        return id.split("\\.").length == 3;
    }

    private List<StatisticalIndicator> parseFromGoal(JSONObject goal) {
        List<StatisticalIndicator> indicatorList = new ArrayList<>();
        if (goal == null || !goal.has("targets")) {
            return indicatorList;
        }
        String goalDescription = goal.optString("description");
        JSONArray targets = goal.optJSONArray("targets");
        for (int i = 0; i < targets.length(); i++) {
            JSONObject item = targets.optJSONObject(i);
            indicatorList.addAll(parseFromTarget(item, goalDescription));
        }
        return indicatorList;
    }

    private List<StatisticalIndicator> parseFromTarget(JSONObject target, String goalDescription) {
        List<StatisticalIndicator> indicatorList = new ArrayList<>();
        if (target == null || !target.has("indicators")) {
            return indicatorList;
        }

        String targetDescription = target.optString("description");
        JSONArray indicatorsList = target.optJSONArray("indicators");
        for (int i = 0; i < indicatorsList.length(); i++) {
            JSONObject item = indicatorsList.optJSONObject(i);
            parseIndicator(item, goalDescription, targetDescription).ifPresent(ind -> indicatorList.add(ind));
        }
        return indicatorList;
    }

    private Optional<StatisticalIndicator> parseIndicator(JSONObject item, String... parentDescriptions) {
        if (!isIndicator(item)) {
            return Optional.empty();
        }
        StatisticalIndicator ind = new StatisticalIndicator();
        ind.setId(item.optString("code"));
        String description = item.optString("description");
        ind.addDescription(LANG, String.join("\r\n", parentDescriptions));
        ind.addName(LANG, ind.getId() + " " + description);
        return Optional.of(ind);
    }


    public StatisticalIndicatorDataModel parseDimensions(String dimensionsResponse) {
        try {
            JSONArray dimensions = JSONHelper.createJSONArray(dimensionsResponse);
            StatisticalIndicatorDataModel selectors = new StatisticalIndicatorDataModel();
            for (int i = 0; i < dimensions.length(); i++) {
                JSONObject dimension = dimensions.getJSONObject(i);
                String id = dimension.optString("id");
                JSONArray codes = dimension.optJSONArray("codes");
                Collection<String> allowedValues = new ArrayList<>();
                for (int j = 0; j < codes.length(); j++) {
                    JSONObject code = codes.getJSONObject(j);
                    allowedValues.add(code.getString("code"));
                }
                if (allowedValues.size() > 0) {
                    StatisticalIndicatorDataDimension selector = new StatisticalIndicatorDataDimension(id, allowedValues);
                    selectors.addDimension(selector);
                }
            }
            return selectors;
        } catch (JSONException e) {
            LOG.error("Error parsing selectors for indicator: " + e.getMessage(), e);
        }
        return null;
    }

    public Map<String, String> parseSource(String indicatorDataResponse) {
        Map<String, String> sources = new HashMap<>();
        try {
            JSONObject jsonObject = JSONHelper.createJSONObject(indicatorDataResponse);
            if (!jsonObject.has("data")) {
                return sources;
            }
            JSONArray data = jsonObject.getJSONArray("data");
            if (data.length() == 0) {
                return sources;
            }
            sources.put(LANG, data.getJSONObject(0).getString("source"));
        } catch (JSONException e) {
            LOG.error("Error parsing source for indicator: " + e.getMessage(), e);
        }
        return sources;
    }

    public boolean isLastPage (String indicatorDataResponse) {
        try {
            JSONObject response = JSONHelper.createJSONObject(indicatorDataResponse);
            int totalPages = response.getInt("totalPages");
            int pageNumber = response.getInt("pageNumber");
            return pageNumber == totalPages;
        } catch (JSONException e) {
            throw new ServiceRuntimeException("Error parsing UNSD indicator data page info: " + e.getMessage(), e);
        }
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

}
