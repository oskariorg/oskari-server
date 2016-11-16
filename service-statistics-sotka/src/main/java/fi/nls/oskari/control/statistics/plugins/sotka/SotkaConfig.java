package fi.nls.oskari.control.statistics.plugins.sotka;

import org.json.JSONObject;

/**
 * Created by SMAKINEN on 30.3.2016.
 */
public class SotkaConfig {

    private long datasourceId;
    private String url;

    public SotkaConfig(JSONObject json, long id) {
        datasourceId = id;
        url = json.optString("url");
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
}
