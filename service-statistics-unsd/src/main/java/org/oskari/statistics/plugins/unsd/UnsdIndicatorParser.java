package org.oskari.statistics.plugins.unsd;

import fi.nls.oskari.control.statistics.data.StatisticalIndicator;
import fi.nls.oskari.control.statistics.data.StatisticalIndicatorDataDimension;
import fi.nls.oskari.control.statistics.data.StatisticalIndicatorDataModel;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.JSONHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

public class UnsdIndicatorParser {

    private final static Logger LOG = LogFactory.getLogger(UnsdIndicatorParser.class);

    private static final String LANG = "en";

    public static List<StatisticalIndicator> parseIndicators(String indicatorsResponse) {
        try {
            JSONArray indicators = new JSONArray(indicatorsResponse);
            return parseIndicators(indicators);
        } catch (JSONException e) {
            LOG.error("Error parsing UNSD indicators: " + e.getMessage(), e);
        }
        return Collections.emptyList();
    }

    public static List<StatisticalIndicator> parseIndicators(JSONArray goals) {
        // array of goal.targets[target.indicators]
        List<StatisticalIndicator> indicatorList = new ArrayList<>();
        if (goals == null) {
            return indicatorList;
        }
        for (int i = 0; i < goals.length(); i++) {
            parseIndicatorsResultItem(goals.optJSONObject(i))
                    .ifPresent(list -> indicatorList.addAll(list));
        }
        return indicatorList;
    }

    private static Optional<List<StatisticalIndicator>> parseIndicatorsResultItem(JSONObject item) {
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

    private static boolean isIndicator(JSONObject item) {
        if (item == null) {
            return false;
        }
        String id = item.optString("code");
        if (id == null) {
            return false;
        }
        return id.split("\\.").length == 3;
    }

    private static List<StatisticalIndicator> parseFromGoal(JSONObject goal) {
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

    private static List<StatisticalIndicator> parseFromTarget(JSONObject target, String goalDescription) {
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

    private static Optional<StatisticalIndicator> parseIndicator(JSONObject item, String... parentDescriptions) {
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

    // Includes dimensions from getIndicatorData response
    // Timeperiod is added by calling generateTimePeriod() after this has been called
    public static StatisticalIndicatorDataModel parseDimensions(JSONObject jsonObject) {
        try {
            JSONArray dimensions = jsonObject.getJSONArray("dimensions");
            StatisticalIndicatorDataModel selectors = new StatisticalIndicatorDataModel();
            for (int i = 0; i < dimensions.length(); i++) {
                JSONObject dimension = dimensions.getJSONObject(i);
                String id = dimension.optString("id");
                JSONArray codes = dimension.optJSONArray("codes");
                StatisticalIndicatorDataDimension selector = new StatisticalIndicatorDataDimension(id);
                for (int j = 0; j < codes.length(); j++) {
                    JSONObject code = codes.getJSONObject(j);
                    selector.addAllowedValue(code.getString("code"), code.getString("description"));
                }
                if (!selector.getAllowedValues().isEmpty()) {
                    selectors.addDimension(selector);
                }
            }
            return selectors;
        } catch (JSONException e) {
            LOG.error("Error parsing selectors for indicator: " + e.getMessage(), e);
        }
        return null;
    }

    // Generates a timePeriod dimension for selectors.
    // The datasource doesn't report this as being selector like it does for other selectors in the metadata.
    public static StatisticalIndicatorDataDimension generateTimePeriod(String name, Set<String> years) {
        StatisticalIndicatorDataDimension selector = new StatisticalIndicatorDataDimension(name);
        for (String year: years) {
            selector.addAllowedValue(year);
        }
        return selector;
    }

    public static Map<String, String> parseSource(JSONObject jsonObject) {
        Map<String, String> sources = new HashMap<>();
        try {
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
}
