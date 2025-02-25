package org.oskari.print.request;

import fi.nls.oskari.domain.User;
import org.geotools.referencing.CRS;
import org.geotools.api.referencing.FactoryException;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;

import java.util.List;

public class PrintRequest {
    
    private User user;
    private double east;
    private double north;
    private String srsName;
    private CoordinateReferenceSystem crs;

    private String printoutSrsName;
    private CoordinateReferenceSystem printoutCrs;
    private double resolution;
    private int width;
    private int height;
    private int targetWidth;
    private int targetHeight;
    private PrintFormat format;
    private boolean showLogo;
    private boolean showScale;
    private boolean showDate;
    private boolean showTimeSeriesTime;
    private String title;
    private List<PrintLayer> layers;
    private String markers;
    private String scaleText;
    private String time;
    private String formattedTime;
    private String timeseriesLabel;
    private String coordinateInfo;

    private String lang;
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
        this.crs = CRS.decode(srsName, true);
    }

    public CoordinateReferenceSystem getCrs() {
        return crs;
    }

    public String getPrintoutSrsName() {
        return printoutSrsName;
    }

    public void setPrintoutSrsName(String printoutSrsName) throws FactoryException {
        this.printoutSrsName = printoutSrsName;
        this.printoutCrs = CRS.decode(printoutSrsName, true);
    }

    public CoordinateReferenceSystem getPrintoutCrs() {
        return printoutCrs;
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

    public boolean isShowTimeSeriesTime() {
        return showTimeSeriesTime;
    }

    public void setShowTimeSeriesTime(boolean showTimeSeriesTime) {
        this.showTimeSeriesTime = showTimeSeriesTime;
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

    public String getMarkers() {
        return markers;
    }

    public void setMarkers(String markers) {
        this.markers = markers;
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
    
    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getFormattedTime() {
        return formattedTime == null ? "" : formattedTime;
    }

    public void setFormattedTime(String formattedTime) {
        this.formattedTime = formattedTime;
    }

    public String getTimeseriesLabel() {
        return timeseriesLabel == null ? "" : timeseriesLabel;
    }

    public void setTimeseriesLabel(String timeseriesLabel) {
        this.timeseriesLabel = timeseriesLabel;
    }

    public void setCoordinateInfo(String coordinateInfo) {
        this.coordinateInfo = coordinateInfo;
    }
    public String getCoordinateInfo() {
        return coordinateInfo;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }
}
