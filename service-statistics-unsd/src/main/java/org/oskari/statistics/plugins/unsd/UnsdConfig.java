package org.oskari.statistics.plugins.unsd;

import org.json.JSONArray;
import org.json.JSONObject;

import java.time.Year;
import java.util.HashSet;
import java.util.Set;

public class UnsdConfig {

    private long datasourceId;
    private String url = "https://unstats.un.org/SDGAPI/v1/sdg";
    private String goal = "1";
    private String timeVariableId = "timePeriod";
    private Set<String> years = new HashSet<>();

    public UnsdConfig() {
        this(new JSONObject(), -1l);
    }

    public UnsdConfig(JSONObject json, long id) {
        datasourceId = id;
        init(json);
    }

    private void init(JSONObject json) {
        url = json.optString("url", url);
        // allow override with db config
        goal = json.optString("goal", goal);
        timeVariableId = json.optString("timeVariable", timeVariableId);

        // setup timePeriod
        JSONArray timePeriodValues = json.optJSONArray(timeVariableId);
        if (timePeriodValues != null) {
            for (int i = 0; i < timePeriodValues.length(); i++) {
                int year = timePeriodValues.optInt(i, -1);
                if ( year != -1 ) {
                    years.add(Integer.toString(year));
                }
            }
        } else {
            int currentYear = Year.now().getValue();
            // reasonable range for data
            for (int year = 2000; year < currentYear - 1; year ++) {
                years.add(Integer.toString(year));
            }
        }
    }

    public long getId() {
        return datasourceId;
    }

    public String getUrl() {
        return url;
    }

    public String getGoal() {
        return goal;
    }

    public String getTimeVariableId() {
        return timeVariableId;
    }

    public Set<String> getTimePeriod() {
        return years;
    }
}
