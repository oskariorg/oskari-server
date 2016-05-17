package fi.nls.oskari.routing.pojo;

import java.util.HashMap;
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
        "name",
        "stopId",
        "stopCode",
        "lon",
        "lat",
        "arrival",
        "departure",
        "zoneId",
        "stopIndex",
        "stopSequence",
        "vertexType",
        "platformCode"
})
public class IntermediateStop {

    @JsonProperty("name")
    private String name;
    @JsonProperty("stopId")
    private String stopId;
    @JsonProperty("stopCode")
    private String stopCode;
    @JsonProperty("lon")
    private Double lon;
    @JsonProperty("lat")
    private Double lat;
    @JsonProperty("arrival")
    private Long arrival;
    @JsonProperty("departure")
    private Long departure;
    @JsonProperty("zoneId")
    private String zoneId;
    @JsonProperty("stopIndex")
    private Integer stopIndex;
    @JsonProperty("stopSequence")
    private Integer stopSequence;
    @JsonProperty("vertexType")
    private String vertexType;
    @JsonProperty("platformCode")
    private String platformCode;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     *
     * @return
     * The name
     */
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    /**
     *
     * @param name
     * The name
     */
    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    /**
     *
     * @return
     * The stopId
     */
    @JsonProperty("stopId")
    public String getStopId() {
        return stopId;
    }

    /**
     *
     * @param stopId
     * The stopId
     */
    @JsonProperty("stopId")
    public void setStopId(String stopId) {
        this.stopId = stopId;
    }

    /**
     *
     * @return
     * The stopCode
     */
    @JsonProperty("stopCode")
    public String getStopCode() {
        return stopCode;
    }

    /**
     *
     * @param stopCode
     * The stopCode
     */
    @JsonProperty("stopCode")
    public void setStopCode(String stopCode) {
        this.stopCode = stopCode;
    }

    /**
     *
     * @return
     * The lon
     */
    @JsonProperty("lon")
    public Double getLon() {
        return lon;
    }

    /**
     *
     * @param lon
     * The lon
     */
    @JsonProperty("lon")
    public void setLon(Double lon) {
        this.lon = lon;
    }

    /**
     *
     * @return
     * The lat
     */
    @JsonProperty("lat")
    public Double getLat() {
        return lat;
    }

    /**
     *
     * @param lat
     * The lat
     */
    @JsonProperty("lat")
    public void setLat(Double lat) {
        this.lat = lat;
    }

    /**
     *
     * @return
     * The arrival
     */
    @JsonProperty("arrival")
    public Long getArrival() {
        return arrival;
    }

    /**
     *
     * @param arrival
     * The arrival
     */
    @JsonProperty("arrival")
    public void setArrival(Long arrival) {
        this.arrival = arrival;
    }

    /**
     *
     * @return
     * The departure
     */
    @JsonProperty("departure")
    public Long getDeparture() {
        return departure;
    }

    /**
     *
     * @param departure
     * The departure
     */
    @JsonProperty("departure")
    public void setDeparture(Long departure) {
        this.departure = departure;
    }

    /**
     *
     * @return
     * The zoneId
     */
    @JsonProperty("zoneId")
    public String getZoneId() {
        return zoneId;
    }

    /**
     *
     * @param zoneId
     * The zoneId
     */
    @JsonProperty("zoneId")
    public void setZoneId(String zoneId) {
        this.zoneId = zoneId;
    }

    /**
     *
     * @return
     * The stopIndex
     */
    @JsonProperty("stopIndex")
    public Integer getStopIndex() {
        return stopIndex;
    }

    /**
     *
     * @param stopIndex
     * The stopIndex
     */
    @JsonProperty("stopIndex")
    public void setStopIndex(Integer stopIndex) {
        this.stopIndex = stopIndex;
    }

    /**
     *
     * @return
     * The stopSequence
     */
    @JsonProperty("stopSequence")
    public Integer getStopSequence() {
        return stopSequence;
    }

    /**
     *
     * @param stopSequence
     * The stopSequence
     */
    @JsonProperty("stopSequence")
    public void setStopSequence(Integer stopSequence) {
        this.stopSequence = stopSequence;
    }

    /**
     *
     * @return
     * The vertexType
     */
    @JsonProperty("vertexType")
    public String getVertexType() {
        return vertexType;
    }

    /**
     *
     * @param vertexType
     * The vertexType
     */
    @JsonProperty("vertexType")
    public void setVertexType(String vertexType) {
        this.vertexType = vertexType;
    }

    /**
     *
     * @return
     * The platformCode
     */
    @JsonProperty("platformCode")
    public String getPlatformCode() {
        return platformCode;
    }

    /**
     *
     * @param platformCode
     * The platformCode
     */
    @JsonProperty("platformCode")
    public void setPlatformCode(String platformCode) {
        this.platformCode = platformCode;
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