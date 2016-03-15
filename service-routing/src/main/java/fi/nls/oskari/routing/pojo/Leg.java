
package fi.nls.oskari.routing.pojo;

import com.fasterxml.jackson.annotation.*;

import javax.annotation.Generated;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "startTime",
    "endTime",
    "departureDelay",
    "arrivalDelay",
    "realTime",
    "distance",
    "pathway",
    "mode",
    "route",
    "agencyTimeZoneOffset",
    "interlineWithPreviousLeg",
    "from",
    "to",
    "legGeometry",
    "rentedBike",
    "transitLeg",
    "duration",
    "intermediateStops",
    "steps",
    "agencyName",
    "agencyUrl",
    "routeType",
    "routeId",
    "headsign",
    "agencyId",
    "tripId",
    "serviceDate",
    "routeShortName",
    "routeLongName"
})
public class Leg {

    @JsonProperty("startTime")
    private Long startTime;
    @JsonProperty("endTime")
    private Long endTime;
    @JsonProperty("departureDelay")
    private Long departureDelay;
    @JsonProperty("arrivalDelay")
    private Long arrivalDelay;
    @JsonProperty("realTime")
    private Boolean realTime;
    @JsonProperty("distance")
    private Double distance;
    @JsonProperty("pathway")
    private Boolean pathway;
    @JsonProperty("mode")
    private String mode;
    @JsonProperty("route")
    private String route;
    @JsonProperty("agencyTimeZoneOffset")
    private Long agencyTimeZoneOffset;
    @JsonProperty("interlineWithPreviousLeg")
    private Boolean interlineWithPreviousLeg;
    @JsonProperty("from")
    private From_ from;
    @JsonProperty("to")
    private To_ to;
    @JsonProperty("legGeometry")
    private LegGeometry legGeometry;
    @JsonProperty("rentedBike")
    private Boolean rentedBike;
    @JsonProperty("transitLeg")
    private Boolean transitLeg;
    @JsonProperty("duration")
    private Long duration;
    @JsonProperty("intermediateStops")
    private List<IntermediateStop> intermediateStops = new ArrayList<IntermediateStop>();

    @JsonProperty("steps")
    private List<Object> steps = new ArrayList<Object>();
    @JsonProperty("agencyName")
    private String agencyName;
    @JsonProperty("agencyUrl")
    private String agencyUrl;
    @JsonProperty("routeType")
    private Long routeType;
    @JsonProperty("routeId")
    private String routeId;
    @JsonProperty("headsign")
    private String headsign;
    @JsonProperty("agencyId")
    private String agencyId;
    @JsonProperty("tripId")
    private String tripId;
    @JsonProperty("serviceDate")
    private String serviceDate;
    @JsonProperty("routeShortName")
    private String routeShortName;
    @JsonProperty("routeLongName")
    private String routeLongName;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

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
     *     The departureDelay
     */
    @JsonProperty("departureDelay")
    public Long getDepartureDelay() {
        return departureDelay;
    }

    /**
     * 
     * @param departureDelay
     *     The departureDelay
     */
    @JsonProperty("departureDelay")
    public void setDepartureDelay(Long departureDelay) {
        this.departureDelay = departureDelay;
    }

    /**
     * 
     * @return
     *     The arrivalDelay
     */
    @JsonProperty("arrivalDelay")
    public Long getArrivalDelay() {
        return arrivalDelay;
    }

    /**
     * 
     * @param arrivalDelay
     *     The arrivalDelay
     */
    @JsonProperty("arrivalDelay")
    public void setArrivalDelay(Long arrivalDelay) {
        this.arrivalDelay = arrivalDelay;
    }

    /**
     * 
     * @return
     *     The realTime
     */
    @JsonProperty("realTime")
    public Boolean getRealTime() {
        return realTime;
    }

    /**
     * 
     * @param realTime
     *     The realTime
     */
    @JsonProperty("realTime")
    public void setRealTime(Boolean realTime) {
        this.realTime = realTime;
    }

    /**
     * 
     * @return
     *     The distance
     */
    @JsonProperty("distance")
    public Double getDistance() {
        return distance;
    }

    /**
     * 
     * @param distance
     *     The distance
     */
    @JsonProperty("distance")
    public void setDistance(Double distance) {
        this.distance = distance;
    }

    /**
     * 
     * @return
     *     The pathway
     */
    @JsonProperty("pathway")
    public Boolean getPathway() {
        return pathway;
    }

    /**
     * 
     * @param pathway
     *     The pathway
     */
    @JsonProperty("pathway")
    public void setPathway(Boolean pathway) {
        this.pathway = pathway;
    }

    /**
     * 
     * @return
     *     The mode
     */
    @JsonProperty("mode")
    public String getMode() {
        return mode;
    }

    /**
     * 
     * @param mode
     *     The mode
     */
    @JsonProperty("mode")
    public void setMode(String mode) {
        this.mode = mode;
    }

    /**
     * 
     * @return
     *     The route
     */
    @JsonProperty("route")
    public String getRoute() {
        return route;
    }

    /**
     * 
     * @param route
     *     The route
     */
    @JsonProperty("route")
    public void setRoute(String route) {
        this.route = route;
    }

    /**
     * 
     * @return
     *     The agencyTimeZoneOffset
     */
    @JsonProperty("agencyTimeZoneOffset")
    public Long getAgencyTimeZoneOffset() {
        return agencyTimeZoneOffset;
    }

    /**
     * 
     * @param agencyTimeZoneOffset
     *     The agencyTimeZoneOffset
     */
    @JsonProperty("agencyTimeZoneOffset")
    public void setAgencyTimeZoneOffset(Long agencyTimeZoneOffset) {
        this.agencyTimeZoneOffset = agencyTimeZoneOffset;
    }

    /**
     * 
     * @return
     *     The interlineWithPreviousLeg
     */
    @JsonProperty("interlineWithPreviousLeg")
    public Boolean getInterlineWithPreviousLeg() {
        return interlineWithPreviousLeg;
    }

    /**
     * 
     * @param interlineWithPreviousLeg
     *     The interlineWithPreviousLeg
     */
    @JsonProperty("interlineWithPreviousLeg")
    public void setInterlineWithPreviousLeg(Boolean interlineWithPreviousLeg) {
        this.interlineWithPreviousLeg = interlineWithPreviousLeg;
    }

    /**
     * 
     * @return
     *     The from
     */
    @JsonProperty("from")
    public From_ getFrom() {
        return from;
    }

    /**
     * 
     * @param from
     *     The from
     */
    @JsonProperty("from")
    public void setFrom(From_ from) {
        this.from = from;
    }

    /**
     * 
     * @return
     *     The to
     */
    @JsonProperty("to")
    public To_ getTo() {
        return to;
    }

    /**
     * 
     * @param to
     *     The to
     */
    @JsonProperty("to")
    public void setTo(To_ to) {
        this.to = to;
    }

    /**
     * 
     * @return
     *     The legGeometry
     */
    @JsonProperty("legGeometry")
    public LegGeometry getLegGeometry() {
        return legGeometry;
    }

    /**
     * 
     * @param legGeometry
     *     The legGeometry
     */
    @JsonProperty("legGeometry")
    public void setLegGeometry(LegGeometry legGeometry) {
        this.legGeometry = legGeometry;
    }

    /**
     * 
     * @return
     *     The rentedBike
     */
    @JsonProperty("rentedBike")
    public Boolean getRentedBike() {
        return rentedBike;
    }

    /**
     * 
     * @param rentedBike
     *     The rentedBike
     */
    @JsonProperty("rentedBike")
    public void setRentedBike(Boolean rentedBike) {
        this.rentedBike = rentedBike;
    }

    /**
     * 
     * @return
     *     The transitLeg
     */
    @JsonProperty("transitLeg")
    public Boolean getTransitLeg() {
        return transitLeg;
    }

    /**
     * 
     * @param transitLeg
     *     The transitLeg
     */
    @JsonProperty("transitLeg")
    public void setTransitLeg(Boolean transitLeg) {
        this.transitLeg = transitLeg;
    }

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
     * The intermediateStops
     */
    @JsonProperty("intermediateStops")
    public List<IntermediateStop> getIntermediateStops() {
        return intermediateStops;
    }

    /**
     *
     * @param intermediateStops
     * The intermediateStops
     */
    @JsonProperty("intermediateStops")
    public void setIntermediateStops(List<IntermediateStop> intermediateStops) {
        this.intermediateStops = intermediateStops;
    }

    /**
     * 
     * @return
     *     The steps
     */
    @JsonProperty("steps")
    public List<Object> getSteps() {
        return steps;
    }

    /**
     * 
     * @param steps
     *     The steps
     */
    @JsonProperty("steps")
    public void setSteps(List<Object> steps) {
        this.steps = steps;
    }

    /**
     * 
     * @return
     *     The agencyName
     */
    @JsonProperty("agencyName")
    public String getAgencyName() {
        return agencyName;
    }

    /**
     * 
     * @param agencyName
     *     The agencyName
     */
    @JsonProperty("agencyName")
    public void setAgencyName(String agencyName) {
        this.agencyName = agencyName;
    }

    /**
     * 
     * @return
     *     The agencyUrl
     */
    @JsonProperty("agencyUrl")
    public String getAgencyUrl() {
        return agencyUrl;
    }

    /**
     * 
     * @param agencyUrl
     *     The agencyUrl
     */
    @JsonProperty("agencyUrl")
    public void setAgencyUrl(String agencyUrl) {
        this.agencyUrl = agencyUrl;
    }

    /**
     * 
     * @return
     *     The routeType
     */
    @JsonProperty("routeType")
    public Long getRouteType() {
        return routeType;
    }

    /**
     * 
     * @param routeType
     *     The routeType
     */
    @JsonProperty("routeType")
    public void setRouteType(Long routeType) {
        this.routeType = routeType;
    }

    /**
     * 
     * @return
     *     The routeId
     */
    @JsonProperty("routeId")
    public String getRouteId() {
        return routeId;
    }

    /**
     * 
     * @param routeId
     *     The routeId
     */
    @JsonProperty("routeId")
    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    /**
     * 
     * @return
     *     The headsign
     */
    @JsonProperty("headsign")
    public String getHeadsign() {
        return headsign;
    }

    /**
     * 
     * @param headsign
     *     The headsign
     */
    @JsonProperty("headsign")
    public void setHeadsign(String headsign) {
        this.headsign = headsign;
    }

    /**
     * 
     * @return
     *     The agencyId
     */
    @JsonProperty("agencyId")
    public String getAgencyId() {
        return agencyId;
    }

    /**
     * 
     * @param agencyId
     *     The agencyId
     */
    @JsonProperty("agencyId")
    public void setAgencyId(String agencyId) {
        this.agencyId = agencyId;
    }

    /**
     * 
     * @return
     *     The tripId
     */
    @JsonProperty("tripId")
    public String getTripId() {
        return tripId;
    }

    /**
     * 
     * @param tripId
     *     The tripId
     */
    @JsonProperty("tripId")
    public void setTripId(String tripId) {
        this.tripId = tripId;
    }

    /**
     * 
     * @return
     *     The serviceDate
     */
    @JsonProperty("serviceDate")
    public String getServiceDate() {
        return serviceDate;
    }

    /**
     * 
     * @param serviceDate
     *     The serviceDate
     */
    @JsonProperty("serviceDate")
    public void setServiceDate(String serviceDate) {
        this.serviceDate = serviceDate;
    }

    /**
     * 
     * @return
     *     The routeShortName
     */
    @JsonProperty("routeShortName")
    public String getRouteShortName() {
        return routeShortName;
    }

    /**
     * 
     * @param routeShortName
     *     The routeShortName
     */
    @JsonProperty("routeShortName")
    public void setRouteShortName(String routeShortName) {
        this.routeShortName = routeShortName;
    }

    /**
     * 
     * @return
     *     The routeLongName
     */
    @JsonProperty("routeLongName")
    public String getRouteLongName() {
        return routeLongName;
    }

    /**
     * 
     * @param routeLongName
     *     The routeLongName
     */
    @JsonProperty("routeLongName")
    public void setRouteLongName(String routeLongName) {
        this.routeLongName = routeLongName;
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
