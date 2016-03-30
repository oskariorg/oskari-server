package fi.nls.oskari.control.statistics.plugins.sotka;

/**
 * Created by SMAKINEN on 30.3.2016.
 */
public class SotkaConfig {

    private String url;

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
