package fi.nls.oskari.domain.map;

import java.sql.Timestamp;
import java.util.Date;

public class BackendStatus {

    private final int mapLayerId;
    private final String status;
    private final String statusMessage;
    private final String infoUrl;
    private final Date timeStamp;

    public BackendStatus(int mapLayerId, String status, String statusMessage, String infoUrl) {
        this(mapLayerId, status, statusMessage, infoUrl, null);
    }

    public BackendStatus(int mapLayerId, String status, String statusMessage, String infoUrl, Timestamp timeStamp) {
        this.mapLayerId = mapLayerId;
        this.status = status;
        this.statusMessage = statusMessage;
        this.infoUrl = infoUrl;
        this.timeStamp = timeStamp;
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

    public Date getTimestamp() {
        return timeStamp;
    }

}
