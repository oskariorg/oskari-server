package fi.nls.oskari.routing.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class Leg {

    @JsonProperty("mode")
    private String mode;

    @JsonProperty("distance")
    private Double distance;

    @JsonProperty("duration")
    private Double duration;

    @JsonProperty("headsign")
    private String headsign;

    @JsonProperty("interlineWithPreviousLeg")
    private boolean interlineWithPreviousLeg;

    @JsonProperty("realTime")
    private boolean realTime;

    @JsonProperty("rentedBike")
    private boolean rentedBike;

    @JsonProperty("serviceDate")
    private String serviceDate;

    @JsonProperty("transitLeg")
    private boolean transitLeg;

    @JsonProperty("trip")
    private Trip trip;
    @JsonProperty("route")
    private Route route;

    @JsonProperty("legGeometry")
    private LegGeometry legGeometry;

    @JsonProperty("start")
    private ScheduledTime start;
    @JsonProperty("end")
    private ScheduledTime end;

    @JsonProperty("from")
    private Place from;

    @JsonProperty("to")
    private Place to;

    @JsonProperty("agency")
    private Agency agency;

    @JsonProperty("steps")
    private List<Step> steps = new ArrayList();

    @JsonProperty("intermediatePlaces")
    private List<Place> intermediatePlaces;

    public LegGeometry getLegGeometry() {
        return legGeometry;
    }

    public void setLegGeometry(LegGeometry legGeometry) {
        this.legGeometry = legGeometry;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public Double getDistance() {
        return distance;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }

    public ScheduledTime getStart() {
        return start;
    }

    public void setStart(ScheduledTime start) {
        this.start = start;
    }

    public ScheduledTime getEnd() {
        return end;
    }

    public void setEnd(ScheduledTime end) {
        this.end = end;
    }

    public List<Step> getSteps() {
        return steps;
    }

    public void setSteps(List<Step> steps) {
        this.steps = steps;
    }

    public Place getFrom() {
        return from;
    }

    public void setFrom(Place from) {
        this.from = from;
    }

    public Place getTo() {
        return to;
    }

    public void setTo(Place to) {
        this.to = to;
    }

    public Agency getAgency() {
        return agency;
    }

    public void setAgency(Agency agency) {
        this.agency = agency;
    }

    public Double getDuration() {
        return duration;
    }

    public void setDuration(Double duration) {
        this.duration = duration;
    }

    public String getHeadsign() {
        return headsign;
    }

    public void setHeadsign(String headsign) {
        this.headsign = headsign;
    }

    public boolean isInterlineWithPreviousLeg() {
        return interlineWithPreviousLeg;
    }

    public void setInterlineWithPreviousLeg(boolean interlineWithPreviousLeg) {
        this.interlineWithPreviousLeg = interlineWithPreviousLeg;
    }

    public boolean isRealTime() {
        return realTime;
    }

    public void setRealTime(boolean realTime) {
        this.realTime = realTime;
    }

    public boolean isRentedBike() {
        return rentedBike;
    }

    public void setRentedBike(boolean rentedBike) {
        this.rentedBike = rentedBike;
    }

    public Route getRoute() {
        return route;
    }

    public void setRoute(Route route) {
        this.route = route;
    }

    public String getServiceDate() {
        return serviceDate;
    }

    public void setServiceDate(String serviceDate) {
        this.serviceDate = serviceDate;
    }

    public boolean isTransitLeg() {
        return transitLeg;
    }

    public void setTransitLeg(boolean transitLeg) {
        this.transitLeg = transitLeg;
    }

    public Trip getTrip() {
        return trip;
    }

    public void setTrip(Trip trip) {
        this.trip = trip;
    }

    public List<Place> getIntermediatePlaces() {
        return intermediatePlaces;
    }

    public void setIntermediatePlaces(List<Place> intermediatePlaces) {
        this.intermediatePlaces = intermediatePlaces;
    }
}
