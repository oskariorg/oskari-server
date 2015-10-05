package fi.nls.oskari.routing;

import com.fasterxml.jackson.annotation.*;

import javax.annotation.Generated;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
        "length",
        "duration",
        "type",
        "locs",
        "shape",
        "code"
})
public class Leg {

    @JsonProperty("length")
    private Integer length;
    @JsonProperty("duration")
    private Number duration;
    @JsonProperty("type")
    private String type;
    @JsonProperty("locs")
    private List<Loc> locs = new ArrayList<Loc>();
    @JsonProperty("shape")
    private List<Shape> shape = new ArrayList<Shape>();
    @JsonProperty("code")
    private String code;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     *
     * @return
     * The length
     */
    @JsonProperty("length")
    public Integer getLength() {
        return length;
    }

    /**
     *
     * @param length
     * The length
     */
    @JsonProperty("length")
    public void setLength(Integer length) {
        this.length = length;
    }

    /**
     *
     * @return
     * The duration
     */
    @JsonProperty("duration")
    public Number getDuration() {
        return duration;
    }

    /**
     *
     * @param duration
     * The duration
     */
    @JsonProperty("duration")
    public void setDuration(Number duration) {
        this.duration = duration;
    }

    /**
     *
     * @return
     * The type
     */
    @JsonProperty("type")
    public String getType() {
        return type;
    }

    /**
     *
     * @param type
     * The type
     */
    @JsonProperty("type")
    public void setType(String type) {
        this.type = type;
    }

    /**
     *
     * @return
     * The locs
     */
    @JsonProperty("locs")
    public List<Loc> getLocs() {
        return locs;
    }

    /**
     *
     * @param locs
     * The locs
     */
    @JsonProperty("locs")
    public void setLocs(List<Loc> locs) {
        this.locs = locs;
    }

    /**
     *
     * @return
     * The shape
     */
    @JsonProperty("shape")
    public List<Shape> getShape() {
        return shape;
    }

    /**
     *
     * @param shape
     * The shape
     */
    @JsonProperty("shape")
    public void setShape(List<Shape> shape) {
        this.shape = shape;
    }

    /**
     *
     * @return
     * The code
     */
    @JsonProperty("code")
    public String getCode() {
        return code;
    }

    /**
     *
     * @param code
     * The code
     */
    @JsonProperty("code")
    public void setCode(String code) {
        this.code = code;
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
