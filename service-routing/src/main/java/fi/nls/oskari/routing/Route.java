package fi.nls.oskari.routing;

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
        "length",
        "duration",
        "legs"
})
public class Route {

    @JsonProperty("length")
    private Double length;
    @JsonProperty("duration")
    private Integer duration;
    @JsonProperty("legs")
    private List<Leg> legs = new ArrayList<Leg>();
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     *
     * @return
     * The length
     */
    @JsonProperty("length")
    public Double getLength() {
        return length;
    }

    /**
     *
     * @param length
     * The length
     */
    @JsonProperty("length")
    public void setLength(Double length) {
        this.length = length;
    }

    /**
     *
     * @return
     * The duration
     */
    @JsonProperty("duration")
    public Integer getDuration() {
        return duration;
    }

    /**
     *
     * @param duration
     * The duration
     */
    @JsonProperty("duration")
    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    /**
     *
     * @return
     * The legs
     */
    @JsonProperty("legs")
    public List<Leg> getLegs() {
        return legs;
    }

    /**
     *
     * @param legs
     * The legs
     */
    @JsonProperty("legs")
    public void setLegs(List<Leg> legs) {
        this.legs = legs;
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

