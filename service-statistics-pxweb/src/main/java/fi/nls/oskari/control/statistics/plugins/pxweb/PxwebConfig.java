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
    private Set<String> ignoredVariables = new HashSet<>();

    PxwebConfig(JSONObject json, long id) {
        datasourceId = id;
        url = json.optString("url");
        regionKey = json.optString("regionKey");
        JSONArray ignored = json.optJSONArray("ignoredVariables");
        if(ignored != null) {
            for (int i = 0; i < ignored.length(); i++) {
                ignoredVariables.add(ignored.optString(i));
            }
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

    public Set<String> getIgnoredVariables() {
        return ignoredVariables;
    }
}
