package fi.nls.oskari.statistics.eurostat;

import org.json.JSONObject;


public class EurostatConfig {

    private String url;


    EurostatConfig(JSONObject json) {
        url = json.optString("url");

    }

    public String getUrl() {
        return url;
    }


}
