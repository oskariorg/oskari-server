package fi.nls.oskari.control.statistics.plugins;

import java.time.Instant;

import org.json.JSONObject;

import fi.nls.oskari.util.JSONHelper;

/**
 * Holds datasource remote sync status for statistical datasources
 */
public class DataStatus {

    private static final String KEY_LAST = "lastUpdate";
    private static final String KEY_START = "updateStart";

    private Instant lastUpdate;
    private Instant updateStarted;

    public DataStatus(String status) {
        this(JSONHelper.createJSONObject(status));
    }

    private DataStatus(JSONObject status) {
        if (status == null) {
            return;
        }
        long last = status.optLong(KEY_LAST, -1);
        long start = status.optLong(KEY_START, -1);
        this.lastUpdate = last == -1 ? null : Instant.ofEpochMilli(last);
        this.updateStarted = start == -1 ? null : Instant.ofEpochMilli(start);
    }

    @Override
    public String toString() {
        return toJSON().toString();
    }

    private JSONObject toJSON() {
        JSONObject json = new JSONObject();
        JSONHelper.putValue(json, KEY_LAST, lastUpdate == null ? -1 : lastUpdate.toEpochMilli());
        JSONHelper.putValue(json, KEY_START, updateStarted == null ? -1 : updateStarted.toEpochMilli());
        return json;
    }

    public Instant getLastUpdate() {
        return lastUpdate;
    }

    public Instant getUpdateStarted() {
        return updateStarted;
    }

    public void startUpdate() {
        this.updateStarted = Instant.now();
    }

    public void finishUpdate() {
        this.lastUpdate = Instant.now();
        this.updateStarted = null;
    }

}
