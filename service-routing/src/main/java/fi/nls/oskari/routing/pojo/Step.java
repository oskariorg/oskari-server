package fi.nls.oskari.routing.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Step {
    @JsonProperty("area")
    private boolean area;

    @JsonProperty("elevationProfile")
    List<ElevationProfileComponent> elevationProfile;

    @JsonProperty("streetName")
    private String streetName;
    @JsonProperty("distance")
    private float distance;
    @JsonProperty("bogusName")
    private String bogusName;

    @JsonProperty("stayOn")
    private boolean stayOn;

    @JsonProperty("lon")
    private float lon;
    @JsonProperty("lat")
    private float lat;
    @JsonProperty("absoluteDirection")
    private String absoluteDirection;
    @JsonProperty("relativeDirection")
    private String relativeDirection;

    public boolean isArea() {
        return area;
    }

    public void setArea(boolean area) {
        this.area = area;
    }

    public List<ElevationProfileComponent> getElevationProfile() {
        return elevationProfile;
    }

    public void setElevationProfile(List<ElevationProfileComponent> elevationProfile) {
        this.elevationProfile = elevationProfile;
    }

    public String getStreetName() {
        return streetName;
    }

    public void setStreetName(String streetName) {
        this.streetName = streetName;
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public String getBogusName() {
        return bogusName;
    }

    public void setBogusName(String bogusName) {
        this.bogusName = bogusName;
    }

    public boolean isStayOn() {
        return stayOn;
    }

    public void setStayOn(boolean stayOn) {
        this.stayOn = stayOn;
    }

    public float getLon() {
        return lon;
    }

    public void setLon(float lon) {
        this.lon = lon;
    }

    public float getLat() {
        return lat;
    }

    public void setLat(float lat) {
        this.lat = lat;
    }

    public String getAbsoluteDirection() {
        return absoluteDirection;
    }

    public void setAbsoluteDirection(String absoluteDirection) {
        this.absoluteDirection = absoluteDirection;
    }

    public String getRelativeDirection() {
        return relativeDirection;
    }

    public void setRelativeDirection(String relativeDirection) {
        this.relativeDirection = relativeDirection;
    }
}
