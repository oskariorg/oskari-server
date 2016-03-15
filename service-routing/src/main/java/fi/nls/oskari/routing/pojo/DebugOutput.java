
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
    "precalculationTime",
    "pathCalculationTime",
    "pathTimes",
    "renderingTime",
    "totalTime",
    "timedOut"
})
public class DebugOutput {

    @JsonProperty("precalculationTime")
    private Long precalculationTime;
    @JsonProperty("pathCalculationTime")
    private Long pathCalculationTime;
    @JsonProperty("pathTimes")
    private List<Long> pathTimes = new ArrayList<Long>();
    @JsonProperty("renderingTime")
    private Long renderingTime;
    @JsonProperty("totalTime")
    private Long totalTime;
    @JsonProperty("timedOut")
    private Boolean timedOut;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * 
     * @return
     *     The precalculationTime
     */
    @JsonProperty("precalculationTime")
    public Long getPrecalculationTime() {
        return precalculationTime;
    }

    /**
     * 
     * @param precalculationTime
     *     The precalculationTime
     */
    @JsonProperty("precalculationTime")
    public void setPrecalculationTime(Long precalculationTime) {
        this.precalculationTime = precalculationTime;
    }

    /**
     * 
     * @return
     *     The pathCalculationTime
     */
    @JsonProperty("pathCalculationTime")
    public Long getPathCalculationTime() {
        return pathCalculationTime;
    }

    /**
     * 
     * @param pathCalculationTime
     *     The pathCalculationTime
     */
    @JsonProperty("pathCalculationTime")
    public void setPathCalculationTime(Long pathCalculationTime) {
        this.pathCalculationTime = pathCalculationTime;
    }

    /**
     * 
     * @return
     *     The pathTimes
     */
    @JsonProperty("pathTimes")
    public List<Long> getPathTimes() {
        return pathTimes;
    }

    /**
     * 
     * @param pathTimes
     *     The pathTimes
     */
    @JsonProperty("pathTimes")
    public void setPathTimes(List<Long> pathTimes) {
        this.pathTimes = pathTimes;
    }

    /**
     * 
     * @return
     *     The renderingTime
     */
    @JsonProperty("renderingTime")
    public Long getRenderingTime() {
        return renderingTime;
    }

    /**
     * 
     * @param renderingTime
     *     The renderingTime
     */
    @JsonProperty("renderingTime")
    public void setRenderingTime(Long renderingTime) {
        this.renderingTime = renderingTime;
    }

    /**
     * 
     * @return
     *     The totalTime
     */
    @JsonProperty("totalTime")
    public Long getTotalTime() {
        return totalTime;
    }

    /**
     * 
     * @param totalTime
     *     The totalTime
     */
    @JsonProperty("totalTime")
    public void setTotalTime(Long totalTime) {
        this.totalTime = totalTime;
    }

    /**
     * 
     * @return
     *     The timedOut
     */
    @JsonProperty("timedOut")
    public Boolean getTimedOut() {
        return timedOut;
    }

    /**
     * 
     * @param timedOut
     *     The timedOut
     */
    @JsonProperty("timedOut")
    public void setTimedOut(Boolean timedOut) {
        this.timedOut = timedOut;
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
