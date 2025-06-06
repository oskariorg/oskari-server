
package fi.nls.oskari.routing.pojo.v1;

import java.util.HashMap;
import java.util.Map;
import jakarta.annotation.Generated;
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
    "lon",
    "lat",
    "orig",
    "vertexType"
})
public class To {

    @JsonProperty("name")
    private String name;
    @JsonProperty("lon")
    private Double lon;
    @JsonProperty("lat")
    private Double lat;
    @JsonProperty("orig")
    private String orig;
    @JsonProperty("vertexType")
    private String vertexType;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * 
     * @return
     *     The name
     */
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    /**
     * 
     * @param name
     *     The name
     */
    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 
     * @return
     *     The lon
     */
    @JsonProperty("lon")
    public Double getLon() {
        return lon;
    }

    /**
     * 
     * @param lon
     *     The lon
     */
    @JsonProperty("lon")
    public void setLon(Double lon) {
        this.lon = lon;
    }

    /**
     * 
     * @return
     *     The lat
     */
    @JsonProperty("lat")
    public Double getLat() {
        return lat;
    }

    /**
     * 
     * @param lat
     *     The lat
     */
    @JsonProperty("lat")
    public void setLat(Double lat) {
        this.lat = lat;
    }

    /**
     * 
     * @return
     *     The orig
     */
    @JsonProperty("orig")
    public String getOrig() {
        return orig;
    }

    /**
     * 
     * @param orig
     *     The orig
     */
    @JsonProperty("orig")
    public void setOrig(String orig) {
        this.orig = orig;
    }

    /**
     * 
     * @return
     *     The vertexType
     */
    @JsonProperty("vertexType")
    public String getVertexType() {
        return vertexType;
    }

    /**
     * 
     * @param vertexType
     *     The vertexType
     */
    @JsonProperty("vertexType")
    public void setVertexType(String vertexType) {
        this.vertexType = vertexType;
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
