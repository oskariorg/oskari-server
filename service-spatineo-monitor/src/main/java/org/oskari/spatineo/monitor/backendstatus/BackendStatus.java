package org.oskari.spatineo.monitor.backendstatus;

public class BackendStatus {

    private final long mapLayerId;
    private final String status;
    private final String statusMessage;
    private final String infoUrl;

    public BackendStatus(long mapLayerId, String status, String statusMessage, String infoUrl) {
        this.mapLayerId = mapLayerId;
        this.status = status;
        this.statusMessage = statusMessage;
        this.infoUrl = infoUrl;
    }

    public long getMapLayerId() {
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

}
