package fi.nls.oskari.routing;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

import java.util.Date;

/**
 * Created by SMAKINEN on 26.6.2015.
 */
public class RouteParams {

    private Point from;
    private Point to;
    private Date date;
    private Boolean isArriveBy;
    private String srs;
    private String lang;
    private String mode;
    private long maxWalkDistance;
    private Boolean isWheelChair;
    private Boolean showIntermediateStops;


    private Point createPoint(Double x, Double y) {
        GeometryFactory factory = new GeometryFactory();
        return factory.createPoint(new Coordinate(x,y));
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public Point getFrom() {
        return from;
    }

    public void setFrom(Double lon, Double lat) {
        this.from = createPoint(lon,lat);
    }

    public Point getTo() {
        return to;
    }

    public void setTo(Double lon, Double lat) {
        this.to = createPoint(lon,lat);
    }

    public Date getDate() {
        if(date == null) {
            return null;
        }
        return new Date(date.getTime());
    }

    public void setDate(Date date) {
        if(date != null) {
            this.date = new Date(date.getTime());
        }
    }

    public Boolean getIsArriveBy() {
        return isArriveBy;
    }

    public void setIsArriveBy(Boolean isArriveBy) {
        this.isArriveBy = isArriveBy;
    }

    public String getSrs() {
        return srs;
    }

    public void setSrs(String srs) {
        this.srs = srs;
    }


    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public long getMaxWalkDistance() {
        return maxWalkDistance;
    }

    public void setMaxWalkDistance(long maxWalkDistance) {
        this.maxWalkDistance = maxWalkDistance;
    }

    public Boolean getIsWheelChair() {
        return isWheelChair;
    }

    public void setIsWheelChair(Boolean isWheelChair) {
        this.isWheelChair = isWheelChair;
    }

    public Boolean getIsShowIntermediateStops() {
        return showIntermediateStops;
    }

    public void setIsShowIntermediateStops(Boolean showIntermediateStops) {
        this.showIntermediateStops = showIntermediateStops;
    }
}
