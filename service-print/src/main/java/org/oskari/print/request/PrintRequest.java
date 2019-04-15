package org.oskari.print.request;

import java.util.List;

import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.oskari.service.wfs.client.OskariFeatureClient;

import fi.nls.oskari.domain.User;

public class PrintRequest {

    private OskariFeatureClient featureClient;
    private User user;
    private double east;
    private double north;
    private String srsName;
    private CoordinateReferenceSystem crs;
    private double resolution;
    private int width;
    private int height;
    private int targetWidth;
    private int targetHeight;
    private PrintFormat format;
    private boolean showLogo;
    private boolean showScale;
    private boolean showDate;
    private String title;
    private List<PrintLayer> layers;
    private String scaleText;
    
    public OskariFeatureClient getFeatureClient() {
        return featureClient;
    }

    public void setFeatureClient(OskariFeatureClient featureClient) {
        this.featureClient = featureClient;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public double getEast() {
        return east;
    }

    public void setEast(double east) {
        this.east = east;
    }

    public double getNorth() {
        return north;
    }

    public void setNorth(double north) {
        this.north = north;
    }

    public String getSrsName() {
        return srsName;
    }

    public void setSrsName(String srsName) throws FactoryException {
        this.srsName = srsName;
        this.crs = CRS.decode(srsName);
    }

    public CoordinateReferenceSystem getCrs() {
        return crs;
    }

    public double getResolution() {
        return resolution;
    }

    public void setResolution(double resolution) {
        this.resolution = resolution;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getTargetWidth() {
        return targetWidth;
    }

    public void setTargetWidth(int targetWidth) {
        this.targetWidth = targetWidth;
    }

    public int getTargetHeight() {
        return targetHeight;
    }

    public void setTargetHeight(int targetHeight) {
        this.targetHeight = targetHeight;
    }

    public PrintFormat getFormat() {
        return format;
    }

    public void setFormat(PrintFormat format) {
        this.format = format;
    }

    public boolean isShowScale() {
        return showScale;
    }

    public void setShowScale(boolean showScale) {
        this.showScale = showScale;
    }

    public boolean isShowDate() {
        return showDate;
    }

    public void setShowDate(boolean showDate) {
        this.showDate = showDate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<PrintLayer> getLayers() {
        return layers;
    }

    public void setLayers(List<PrintLayer> layers) {
        this.layers = layers;
    }

    public boolean isShowLogo() {
        return showLogo;
    }

    public void setShowLogo(boolean showLogo) {
        this.showLogo = showLogo;
    }

    public String getScaleText() {
        return scaleText;
    }

    public void setScaleText(String scaleText) {
        this.scaleText = scaleText;
    }

    public boolean isScaleText(){
        return (this.scaleText != null && !this.scaleText.isEmpty());
    }
    
    public double[] getBoundingBox() {
        double halfResolution = resolution * 0.5;

        double widthHalf = width * halfResolution;
        double heightHalf = height * halfResolution;

        return new double[] {
                east - widthHalf,
                north - heightHalf,
                east + widthHalf,
                north + heightHalf
        };
    }
    
}
