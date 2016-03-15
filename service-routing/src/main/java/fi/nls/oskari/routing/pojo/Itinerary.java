
package fi.nls.oskari.routing.pojo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "duration",
    "startTime",
    "endTime",
    "walkTime",
    "transitTime",
    "waitingTime",
    "walkDistance",
    "walkLimitExceeded",
    "elevationLost",
    "elevationGained",
    "transfers",
    "legs",
    "tooSloped"
})
public class Itinerary {

    @JsonProperty("duration")
    private Long duration;
    @JsonProperty("startTime")
    private Long startTime;
    @JsonProperty("endTime")
    private Long endTime;
    @JsonProperty("walkTime")
    private Long walkTime;
    @JsonProperty("transitTime")
    private Long transitTime;
    @JsonProperty("waitingTime")
    private Long waitingTime;
    @JsonProperty("walkDistance")
    private Double walkDistance;
    @JsonProperty("walkLimitExceeded")
    private Boolean walkLimitExceeded;
    @JsonProperty("elevationLost")
    private Long elevationLost;
    @JsonProperty("elevationGained")
    private Long elevationGained;
    @JsonProperty("transfers")
    private Long transfers;
    @JsonProperty("legs")
    private List<Leg> legs = new ArrayList<Leg>();
    @JsonProperty("tooSloped")
    private Boolean tooSloped;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * 
     * @return
     *     The duration
     */
    @JsonProperty("duration")
    public Long getDuration() {
        return duration;
    }

    /**
     * 
     * @param duration
     *     The duration
     */
    @JsonProperty("duration")
    public void setDuration(Long duration) {
        this.duration = duration;
    }

    /**
     * 
     * @return
     *     The startTime
     */
    @JsonProperty("startTime")
    public Long getStartTime() {
        return startTime;
    }

    /**
     * 
     * @param startTime
     *     The startTime
     */
    @JsonProperty("startTime")
    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    /**
     * 
     * @return
     *     The endTime
     */
    @JsonProperty("endTime")
    public Long getEndTime() {
        return endTime;
    }

    /**
     * 
     * @param endTime
     *     The endTime
     */
    @JsonProperty("endTime")
    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    /**
     * 
     * @return
     *     The walkTime
     */
    @JsonProperty("walkTime")
    public Long getWalkTime() {
        return walkTime;
    }

    /**
     * 
     * @param walkTime
     *     The walkTime
     */
    @JsonProperty("walkTime")
    public void setWalkTime(Long walkTime) {
        this.walkTime = walkTime;
    }

    /**
     * 
     * @return
     *     The transitTime
     */
    @JsonProperty("transitTime")
    public Long getTransitTime() {
        return transitTime;
    }

    /**
     * 
     * @param transitTime
     *     The transitTime
     */
    @JsonProperty("transitTime")
    public void setTransitTime(Long transitTime) {
        this.transitTime = transitTime;
    }

    /**
     * 
     * @return
     *     The waitingTime
     */
    @JsonProperty("waitingTime")
    public Long getWaitingTime() {
        return waitingTime;
    }

    /**
     * 
     * @param waitingTime
     *     The waitingTime
     */
    @JsonProperty("waitingTime")
    public void setWaitingTime(Long waitingTime) {
        this.waitingTime = waitingTime;
    }

    /**
     * 
     * @return
     *     The walkDistance
     */
    @JsonProperty("walkDistance")
    public Double getWalkDistance() {
        return walkDistance;
    }

    /**
     * 
     * @param walkDistance
     *     The walkDistance
     */
    @JsonProperty("walkDistance")
    public void setWalkDistance(Double walkDistance) {
        this.walkDistance = walkDistance;
    }

    /**
     * 
     * @return
     *     The walkLimitExceeded
     */
    @JsonProperty("walkLimitExceeded")
    public Boolean getWalkLimitExceeded() {
        return walkLimitExceeded;
    }

    /**
     * 
     * @param walkLimitExceeded
     *     The walkLimitExceeded
     */
    @JsonProperty("walkLimitExceeded")
    public void setWalkLimitExceeded(Boolean walkLimitExceeded) {
        this.walkLimitExceeded = walkLimitExceeded;
    }

    /**
     * 
     * @return
     *     The elevationLost
     */
    @JsonProperty("elevationLost")
    public Long getElevationLost() {
        return elevationLost;
    }

    /**
     * 
     * @param elevationLost
     *     The elevationLost
     */
    @JsonProperty("elevationLost")
    public void setElevationLost(Long elevationLost) {
        this.elevationLost = elevationLost;
    }

    /**
     * 
     * @return
     *     The elevationGained
     */
    @JsonProperty("elevationGained")
    public Long getElevationGained() {
        return elevationGained;
    }

    /**
     * 
     * @param elevationGained
     *     The elevationGained
     */
    @JsonProperty("elevationGained")
    public void setElevationGained(Long elevationGained) {
        this.elevationGained = elevationGained;
    }

    /**
     * 
     * @return
     *     The transfers
     */
    @JsonProperty("transfers")
    public Long getTransfers() {
        return transfers;
    }

    /**
     * 
     * @param transfers
     *     The transfers
     */
    @JsonProperty("transfers")
    public void setTransfers(Long transfers) {
        this.transfers = transfers;
    }

    /**
     * 
     * @return
     *     The legs
     */
    @JsonProperty("legs")
    public List<Leg> getLegs() {
        return legs;
    }

    /**
     * 
     * @param legs
     *     The legs
     */
    @JsonProperty("legs")
    public void setLegs(List<Leg> legs) {
        this.legs = legs;
    }

    /**
     * 
     * @return
     *     The tooSloped
     */
    @JsonProperty("tooSloped")
    public Boolean getTooSloped() {
        return tooSloped;
    }

    /**
     * 
     * @param tooSloped
     *     The tooSloped
     */
    @JsonProperty("tooSloped")
    public void setTooSloped(Boolean tooSloped) {
        this.tooSloped = tooSloped;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
