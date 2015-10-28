package fi.nls.oskari.routing.pojo;

import com.fasterxml.jackson.annotation.*;

import javax.annotation.Generated;
import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
        "coord",
        "arrTime",
        "depTime",
        "name",
        "code",
        "shortCode",
        "stopAddress",
        "platformNumber",
        "shortName",
        "terminal_code",
        "terminal_name"
})
public class Loc {

    @JsonProperty("coord")
    private Coord coord;
    @JsonProperty("arrTime")
    private String arrTime;
    @JsonProperty("depTime")
    private String depTime;
    @JsonProperty("name")
    private Object name;
    @JsonProperty("code")
    private String code;
    @JsonProperty("shortCode")
    private String shortCode;
    @JsonProperty("stopAddress")
    private String stopAddress;
    @JsonProperty("platformNumber")
    private String platformNumber;
    @JsonProperty("shortName")
    private String shortName;
    @JsonProperty("terminal_code")
    private Integer terminal_code;
    @JsonProperty("terminal_name")
    private String terminal_name;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     *
     * @return
     * The coord
     */
    @JsonProperty("coord")
    public Coord getCoord() {
        return coord;
    }

    /**
     *
     * @param coord
     * The coord
     */
    @JsonProperty("coord")
    public void setCoord(Coord coord) {
        this.coord = coord;
    }

    /**
     *
     * @return
     * The arrTime
     */
    @JsonProperty("arrTime")
    public String getArrTime() {
        return arrTime;
    }

    /**
     *
     * @param arrTime
     * The arrTime
     */
    @JsonProperty("arrTime")
    public void setArrTime(String arrTime) {
        this.arrTime = arrTime;
    }

    /**
     *
     * @return
     * The depTime
     */
    @JsonProperty("depTime")
    public String getDepTime() {
        return depTime;
    }

    /**
     *
     * @param depTime
     * The depTime
     */
    @JsonProperty("depTime")
    public void setDepTime(String depTime) {
        this.depTime = depTime;
    }

    /**
     *
     * @return
     * The name
     */
    @JsonProperty("name")
    public Object getName() {
        return name;
    }

    /**
     *
     * @param name
     * The name
     */
    @JsonProperty("name")
    public void setName(Object name) {
        this.name = name;
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

    /**
     *
     * @return
     * The shortCode
     */
    @JsonProperty("shortCode")
    public String getShortCode() {
        return shortCode;
    }

    /**
     *
     * @param shortCode
     * The shortCode
     */
    @JsonProperty("shortCode")
    public void setShortCode(String shortCode) {
        this.shortCode = shortCode;
    }

    /**
     *
     * @return
     * The stopAddress
     */
    @JsonProperty("stopAddress")
    public String getStopAddress() {
        return stopAddress;
    }

    /**
     *
     * @param stopAddress
     * The stopAddress
     */
    @JsonProperty("stopAddress")
    public void setStopAddress(String stopAddress) {
        this.stopAddress = stopAddress;
    }

    /**
     *
     * @return
     * The platformNumber
     */
    @JsonProperty("platformNumber")
    public String getPlatformNumber() {
        return platformNumber;
    }

    /**
     *
     * @param platformNumber
     * The platformNumber
     */
    @JsonProperty("platformNumber")
    public void setPlatformNumber(String platformNumber) {
        this.platformNumber = platformNumber;
    }

    /**
     *
     * @return
     * The shortName
     */
    @JsonProperty("shortName")
    public String getShortName() {
        return shortName;
    }

    /**
     *
     * @param shortName
     * The shortName
     */
    @JsonProperty("shortName")
    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    /**
     *
     * @return
     * The terminal_code
     */
    @JsonProperty("terminal_code")
    public Integer getTerminal_code() {
        return terminal_code;
    }

    /**
     *
     * @param terminal_code
     * The terminal_code
     */
    @JsonProperty("terminal_code")
    public void setTerminal_code(Integer terminal_code) {
        this.terminal_code = terminal_code;
    }

    /**
     *
     * @return
     * The terminal_name
     */
    @JsonProperty("terminal_name")
    public String getTerminal_name() {
        return terminal_name;
    }

    /**
     *
     * @param terminal_name
     * The terminal_name
     */
    @JsonProperty("terminal_name")
    public void setTerminal_name(String terminal_name) {
        this.terminal_name = terminal_name;
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