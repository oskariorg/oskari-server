
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
    "mode",
    "date",
    "wheelchair",
    "showIntermediateStops",
    "arriveBy",
    "fromPlace",
    "toPlace",
    "maxWalkDistance",
    "time",
    "locale"
})
public class RequestParameters {

    @JsonProperty("mode")
    private String mode;
    @JsonProperty("date")
    private String date;
    @JsonProperty("wheelchair")
    private String wheelchair;
    @JsonProperty("showIntermediateStops")
    private String showIntermediateStops;
    @JsonProperty("arriveBy")
    private String arriveBy;
    @JsonProperty("fromPlace")
    private String fromPlace;
    @JsonProperty("toPlace")
    private String toPlace;
    @JsonProperty("maxWalkDistance")
    private String maxWalkDistance;
    @JsonProperty("time")
    private String time;
    @JsonProperty("locale")
    private String locale;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

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
     *     The date
     */
    @JsonProperty("date")
    public String getDate() {
        return date;
    }

    /**
     * 
     * @param date
     *     The date
     */
    @JsonProperty("date")
    public void setDate(String date) {
        this.date = date;
    }

    /**
     * 
     * @return
     *     The wheelchair
     */
    @JsonProperty("wheelchair")
    public String getWheelchair() {
        return wheelchair;
    }

    /**
     * 
     * @param wheelchair
     *     The wheelchair
     */
    @JsonProperty("wheelchair")
    public void setWheelchair(String wheelchair) {
        this.wheelchair = wheelchair;
    }

    /**
     *
     * @return
     * The showIntermediateStops
     */
    @JsonProperty("showIntermediateStops")
    public String getShowIntermediateStops() {
        return showIntermediateStops;
    }

    /**
     *
     * @param showIntermediateStops
     * The showIntermediateStops
     */
    @JsonProperty("showIntermediateStops")
    public void setShowIntermediateStops(String showIntermediateStops) {
        this.showIntermediateStops = showIntermediateStops;
    }
    /**
     * 
     * @return
     *     The arriveBy
     */
    @JsonProperty("arriveBy")
    public String getArriveBy() {
        return arriveBy;
    }

    /**
     * 
     * @param arriveBy
     *     The arriveBy
     */
    @JsonProperty("arriveBy")
    public void setArriveBy(String arriveBy) {
        this.arriveBy = arriveBy;
    }

    /**
     * 
     * @return
     *     The fromPlace
     */
    @JsonProperty("fromPlace")
    public String getFromPlace() {
        return fromPlace;
    }

    /**
     * 
     * @param fromPlace
     *     The fromPlace
     */
    @JsonProperty("fromPlace")
    public void setFromPlace(String fromPlace) {
        this.fromPlace = fromPlace;
    }

    /**
     * 
     * @return
     *     The toPlace
     */
    @JsonProperty("toPlace")
    public String getToPlace() {
        return toPlace;
    }

    /**
     * 
     * @param toPlace
     *     The toPlace
     */
    @JsonProperty("toPlace")
    public void setToPlace(String toPlace) {
        this.toPlace = toPlace;
    }

    /**
     * 
     * @return
     *     The maxWalkDistance
     */
    @JsonProperty("maxWalkDistance")
    public String getMaxWalkDistance() {
        return maxWalkDistance;
    }

    /**
     * 
     * @param maxWalkDistance
     *     The maxWalkDistance
     */
    @JsonProperty("maxWalkDistance")
    public void setMaxWalkDistance(String maxWalkDistance) {
        this.maxWalkDistance = maxWalkDistance;
    }

    /**
     * 
     * @return
     *     The time
     */
    @JsonProperty("time")
    public String getTime() {
        return time;
    }

    /**
     * 
     * @param time
     *     The time
     */
    @JsonProperty("time")
    public void setTime(String time) {
        this.time = time;
    }

    /**
     * 
     * @return
     *     The locale
     */
    @JsonProperty("locale")
    public String getLocale() {
        return locale;
    }

    /**
     * 
     * @param locale
     *     The locale
     */
    @JsonProperty("locale")
    public void setLocale(String locale) {
        this.locale = locale;
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
