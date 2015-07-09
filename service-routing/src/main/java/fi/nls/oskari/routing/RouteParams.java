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
    private Point via;
    private Date date;
    private Boolean isDepartureTime;
    private int minutesSpentInVia;
    private String ticketZone;
    private String transportTypes;
    private String srs;
    private String lang;

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

    public void setFrom(Double lat, Double lon) {
        this.from = createPoint(lat,lon);
    }

    public Point getTo() {
        return to;
    }

    public void setTo(Double lat, Double lon) {
        this.to = createPoint(lat,lon);
    }

    public Point getVia() {
        return via;
    }

    public void setVia(Double lat, Double lon) {
        this.via = createPoint(lat,lon);
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

    public Boolean getIsDepartureTime() {
        return isDepartureTime;
    }

    public void setIsDepartureTime(Boolean isDepartureTime) {
        this.isDepartureTime = isDepartureTime;
    }

    public int getMinutesSpentInVia() {
        return minutesSpentInVia;
    }

    public void setMinutesSpentInVia(int minutesSpentInVia) {
        this.minutesSpentInVia = minutesSpentInVia;
    }

    public String getTicketZone() {
        return ticketZone;
    }
    public void setTicketZone(String ticketZone) {
        this.ticketZone = ticketZone;
    }

    public String getTransportTypes() {
        return transportTypes;
    }

    public void setTransportTypes(String transportTypes) {
        this.transportTypes = transportTypes;
    }

    public String getSrs() {
        return srs;
    }

    public void setSrs(String srs) {
        this.srs = srs;
    }




}
