package fi.nls.oskari.control.statistics.plugins.pxweb;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by SMAKINEN on 19.9.2016.
 */
public class PxwebConfig {

    private long datasourceId;
    private String url;
    private String regionKey;
    private String indicatorKey;
    private Set<String> ignoredVariables = new HashSet<>();
    private String timeVariableId = null;

    public PxwebConfig(JSONObject json, long id) {
        datasourceId = id;
        url = json.optString("url");
        regionKey = json.optString("regionKey");
        indicatorKey = json.optString("indicatorKey");

        // allow override with db config
        timeVariableId = json.optString("timeVariable", timeVariableId);
        JSONArray ignored = json.optJSONArray("ignoredVariables");
        if(ignored != null) {
            for (int i = 0; i < ignored.length(); i++) {
                ignoredVariables.add(ignored.optString(i));
            }
        }
        if(getRegionKey() != null) {
            ignoredVariables.add(getRegionKey());
        }
        if(getIndicatorKey() != null) {
            ignoredVariables.add(getIndicatorKey());
        }
    }

    public long getId() {
        return datasourceId;
    }
    public String getUrl() {
        return url;
    }

    public String getRegionKey() {
        return regionKey;
    }

    public String getIndicatorKey() {
        return indicatorKey;
    }

    public boolean hasIndicatorKey() {
        return indicatorKey != null && !indicatorKey.isEmpty();
    }

    public Set<String> getIgnoredVariables() {
        return ignoredVariables;
    }

    public String getTimeVariableId() {
        return timeVariableId;
    }
}
