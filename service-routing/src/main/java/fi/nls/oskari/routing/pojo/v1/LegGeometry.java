
package fi.nls.oskari.routing.pojo.v1;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import jakarta.annotation.Generated;
import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "points",
    "length"
})
public class LegGeometry {

    @JsonProperty("points")
    private String points;
    @JsonProperty("length")
    private Long length;

    /**
     * 
     * @return
     *     The points
     */
    @JsonProperty("points")
    public String getPoints() {
        return points;
    }

    /**
     * 
     * @param points
     *     The points
     */
    @JsonProperty("points")
    public void setPoints(String points) {
        this.points = points;
    }

    /**
     * 
     * @return
     *     The length
     */
    @JsonProperty("length")
    public Long getLength() {
        return length;
    }

    /**
     * 
     * @param length
     *     The length
     */
    @JsonProperty("length")
    public void setLength(Long length) {
        this.length = length;
    }
}
