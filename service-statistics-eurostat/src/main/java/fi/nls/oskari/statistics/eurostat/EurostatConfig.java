package fi.nls.oskari.statistics.eurostat;

import org.json.JSONObject;


public class EurostatConfig {

    private long datasourceId;
    private String url;


    EurostatConfig(JSONObject json, long id) {
        datasourceId = id;
        url = json.optString("url");

    }

    public String getUrl() {
        return url;
    }
    public long getId() {
        return datasourceId;
    }


}
