package fi.nls.oskari.control.statistics.plugins.sotka;

import org.json.JSONObject;

public class SotkaConfig {

    private long datasourceId;
    private String url;
    // default to year as it's the time variable on sotkanet
    private String timeVariableId = "year";

    public SotkaConfig(JSONObject json, long id) {
        datasourceId = id;
        url = json.optString("url");
        // allow override with db config
        timeVariableId = json.optString("timeVariable", timeVariableId);
    }

    public long getId() {
        return datasourceId;
    }
    public String getUrl() {
        if(url == null) {
            // default
            return "http://www.sotkanet.fi/rest";
        }
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTimeVariableId() {
        return timeVariableId;
    }
}
