package org.oskari.spatineo.monitor.api.model;

public class Meter {

    private String id;
    private String layerName;
    private String crs;
    private String imageHeight;
    private String imageWidth;
    private String format;
    private String operation;
    private String monitorLink;
    private Indicator indicator;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLayerName() {
        return layerName;
    }

    public void setLayerName(String layerName) {
        this.layerName = layerName;
    }

    public String getCrs() {
        return crs;
    }

    public void setCrs(String crs) {
        this.crs = crs;
    }

    public String getImageHeight() {
        return imageHeight;
    }

    public void setImageHeight(String imageHeight) {
        this.imageHeight = imageHeight;
    }

    public String getImageWidth() {
        return imageWidth;
    }

    public void setImageWidth(String imageWidth) {
        this.imageWidth = imageWidth;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getMonitorLink() {
        return monitorLink;
    }

    public void setMonitorLink(String monitorLink) {
        this.monitorLink = monitorLink;
    }

    public Indicator getIndicator() {
        return indicator;
    }

    public void setIndicator(Indicator indicator) {
        this.indicator = indicator;
    }

}
