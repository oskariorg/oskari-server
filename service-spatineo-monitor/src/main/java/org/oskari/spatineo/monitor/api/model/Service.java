package org.oskari.spatineo.monitor.api.model;

import java.util.List;

public class Service {
    
    public static final String TYPE_WFS = "WFS";
    public static final String TYPE_WMS = "WMS";

    private String id;
    private long serviceId;
    private String serviceType;
    private String serviceUrl;
    private String title;
    private List<Meter> meters;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getServiceId() {
        return serviceId;
    }

    public void setServiceId(long serviceId) {
        this.serviceId = serviceId;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getServiceUrl() {
        return serviceUrl;
    }

    public void setServiceUrl(String serviceUrl) {
        this.serviceUrl = serviceUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<Meter> getMeters() {
        return meters;
    }

    public void setMeters(List<Meter> meters) {
        this.meters = meters;
    }

}
