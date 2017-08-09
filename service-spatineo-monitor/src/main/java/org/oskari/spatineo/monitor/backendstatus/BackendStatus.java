package org.oskari.spatineo.monitor.backendstatus;

public class BackendStatus {

    private final int mapLayerId;
    private final String status;
    private final String statusMessage;
    private final String infoUrl;

    public BackendStatus(int mapLayerId, String status, String statusMessage, String infoUrl) {
        this.mapLayerId = mapLayerId;
        this.status = status;
        this.statusMessage = statusMessage;
        this.infoUrl = infoUrl;
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

}
