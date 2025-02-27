package fi.nls.oskari.routing.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Node {

    @JsonProperty("elevationLost")
    private Float elevationLost;

    @JsonProperty("elevationGained")
    private Float elevationGained;

    @JsonProperty("waitingTime")
    private Float waitingTime;

    @JsonProperty("walkTime")
    private Float walkTime;

    @JsonProperty("walkDistance")
    private Float walkDistance;

    @JsonProperty("duration")
    private Float duration;

    @JsonProperty("numberOfTransfers")
    private Float numberOfTransfers;

    @JsonProperty("start")
    private String start;

    @JsonProperty("end")
    private String end;

    @JsonProperty("legs")
    List<Leg> legs;

    public Float getElevationLost() {
        return elevationLost;
    }

    public void setElevationLost(Float elevationLost) {
        this.elevationLost = elevationLost;
    }

    public Float getElevationGained() {
        return elevationGained;
    }

    public void setElevationGained(Float elevationGained) {
        this.elevationGained = elevationGained;
    }

    public Float getWaitingTime() {
        return waitingTime;
    }

    public void setWaitingTime(Float waitingTime) {
        this.waitingTime = waitingTime;
    }

    public Float getWalkTime() {
        return walkTime;
    }

    public void setWalkTime(Float walkTime) {
        this.walkTime = walkTime;
    }

    public Float getWalkDistance() {
        return walkDistance;
    }

    public void setWalkDistance(Float walkDistance) {
        this.walkDistance = walkDistance;
    }

    public Float getDuration() {
        return duration;
    }

    public void setDuration(Float duration) {
        this.duration = duration;
    }

    public Float getNumberOfTransfers() {
        return numberOfTransfers;
    }

    public void setNumberOfTransfers(Float numberOfTransfers) {
        this.numberOfTransfers = numberOfTransfers;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public List<Leg> getLegs() {
        return legs;
    }

    public void setLegs(List<Leg> legs) {
        this.legs = legs;
    }
}
