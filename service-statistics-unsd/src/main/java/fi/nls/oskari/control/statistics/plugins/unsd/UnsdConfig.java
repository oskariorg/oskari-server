package fi.nls.oskari.control.statistics.plugins.unsd;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import org.json.JSONObject;

public class UnsdConfig {

    private final static Logger LOG = LogFactory.getLogger(UnsdConfig.class);

    private long datasourceId;
    private String url = "https://unstats.un.org/SDGAPI/v1/sdg";
    private String goal = "1";
    private String timeVariableId = "timePeriod";

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
    }

    public long getId() {
        return datasourceId;
    }
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getGoal() {
        return goal;
    }

    public void setGoal(String goal) {
        this.goal = goal;
    }

    public String getTimeVariableId() {
        return timeVariableId;
    }
}
