package fi.nls.oskari.domain.map;

import java.util.Date;

public class BackendStatus {

    private final int id;
    private final Date ts;
    private final int mapLayerId;
    private final String status;
    private final String statusMessage;
    private final String infoUrl;
    private final String statusJson;

    public BackendStatus(int mapLayerId, String status, String statusMessage, String infoUrl, String statusJson) {
        this(-1, null, mapLayerId, status, statusMessage, infoUrl, statusJson);
    }

    public BackendStatus(int id, Date ts, int mapLayerId, String status, String statusMessage, String infoUrl, String statusJson) {
        this.id = id;
        this.ts = ts;
        this.mapLayerId = mapLayerId;
        this.status = status;
        this.statusMessage = statusMessage;
        this.infoUrl = infoUrl;
        this.statusJson = statusJson;
    }

    public int getId() {
        return id;
    }

    public Date getTs() {
        return ts;
    }

    public int getMapLayerId() {
        return mapLayerId;
    }

    public String getStatus() {
        return status;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public String getInfoUrl() {
        return infoUrl;
    }

    public String getStatusJson() {
        return statusJson;
    }

}
