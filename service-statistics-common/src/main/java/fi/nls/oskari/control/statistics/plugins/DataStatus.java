package fi.nls.oskari.control.statistics.plugins;

import fi.nls.oskari.util.JSONHelper;
import org.json.JSONObject;

import java.util.Date;

/**
 * Holds datasource remote sync status for statistical datasources
 */
public class DataStatus {

    private static final String KEY_COMPLETE = "complete";
    private static final String KEY_LAST = "lastUpdate";
    private static final String KEY_START = "updateStart";

    private long lastUpdate = -1;
    private long updateStarted = -1;
    private boolean isUpdating = false;

    public DataStatus(String status) {
        this(JSONHelper.createJSONObject(status));
    }

    public DataStatus(JSONObject status) {
        if(status == null) {
            return;
        }
        isUpdating = !status.optBoolean(KEY_COMPLETE);
        lastUpdate = status.optLong(KEY_LAST, -1);
        updateStarted = status.optLong(KEY_START, -1);
    }

    public JSONObject toJSON() {
        JSONObject val = JSONHelper.createJSONObject(KEY_COMPLETE, !isUpdating());
        JSONHelper.putValue(val, KEY_LAST, lastUpdate);
        JSONHelper.putValue(val, KEY_START, updateStarted);
        return val;
    }

    private Date getDate(long ts) {
        if(ts == -1) {
            return null;
        }
        return new Date(ts);
    }

    public boolean shouldUpdate(long refreshPeriodms) {
        if(isUpdating()) {
            return false;
        }
        if(lastUpdate == -1) {
            return true;
        }
        return System.currentTimeMillis() > lastUpdate + refreshPeriodms;
    }

    public Date getLastUpdate() {
        return getDate(lastUpdate);
    }

    public void setLastUpdate() {
        setLastUpdate(new Date());
    }
    public void setLastUpdate(Date date) {
        setLastUpdate(date.getTime());
    }

    public void setLastUpdate(long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public Date getUpdateStarted() {
        return getDate(updateStarted);
    }

    public void setUpdateStarted() {
        setUpdateStarted(new Date());
    }
    public void setUpdateStarted(Date date) {
        setUpdateStarted(date.getTime());
    }
    public void setUpdateStarted(long updateStarted) {
        this.updateStarted = updateStarted;
    }

    public boolean isUpdating() {
        return isUpdating;
    }

    public void setUpdating(boolean updating) {
        isUpdating = updating;
    }
}
