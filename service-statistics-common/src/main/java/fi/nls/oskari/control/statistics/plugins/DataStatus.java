package fi.nls.oskari.control.statistics.plugins;

import fi.nls.oskari.util.JSONHelper;
import org.json.JSONObject;

import java.util.Date;

/**
 * Created by SMAKINEN on 10.1.2017.
 */
public class DataStatus {

    private Date lastUpdate;
    private Date updateStarted;
    private boolean isUpdating = false;

    public DataStatus(String status) {
        this(JSONHelper.createJSONObject(status));
    }

    public DataStatus(JSONObject status) {
        if(status == null) {
            return;
        }
        isUpdating = !status.optBoolean("complete");
        lastUpdate = getDate(status.optLong("lastUpdate", -1));
        updateStarted = getDate(status.optLong("updateStart", -1));
    }

    private Date getDate(long ts) {
        if(ts == -1) {
            return null;
        }
        return new Date(ts);
    }
    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public Date getUpdateStarted() {
        return updateStarted;
    }

    public void setUpdateStarted(Date updateStarted) {
        this.updateStarted = updateStarted;
    }

    public boolean isUpdating() {
        return isUpdating;
    }

    public void setUpdating(boolean updating) {
        isUpdating = updating;
    }
}
