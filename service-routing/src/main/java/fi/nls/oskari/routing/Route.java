
package fi.nls.oskari.routing;

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
    "requestParameters",
    "plan",
    "debugOutput"
})
public class Route {

    @JsonProperty("requestParameters")
    private RequestParameters requestParameters;
    @JsonProperty("plan")
    private Plan plan;
    @JsonProperty("debugOutput")
    private DebugOutput debugOutput;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * 
     * @return
     *     The requestParameters
     */
    @JsonProperty("requestParameters")
    public RequestParameters getRequestParameters() {
        return requestParameters;
    }

    /**
     * 
     * @param requestParameters
     *     The requestParameters
     */
    @JsonProperty("requestParameters")
    public void setRequestParameters(RequestParameters requestParameters) {
        this.requestParameters = requestParameters;
    }

    /**
     * 
     * @return
     *     The plan
     */
    @JsonProperty("plan")
    public Plan getPlan() {
        return plan;
    }

    /**
     * 
     * @param plan
     *     The plan
     */
    @JsonProperty("plan")
    public void setPlan(Plan plan) {
        this.plan = plan;
    }

    /**
     * 
     * @return
     *     The debugOutput
     */
    @JsonProperty("debugOutput")
    public DebugOutput getDebugOutput() {
        return debugOutput;
    }

    /**
     * 
     * @param debugOutput
     *     The debugOutput
     */
    @JsonProperty("debugOutput")
    public void setDebugOutput(DebugOutput debugOutput) {
        this.debugOutput = debugOutput;
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
