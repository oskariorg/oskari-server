package fi.nls.oskari.control.statistics.plugins.pxweb.json;

/**
 * Created by JacksonGenerator on 19.9.2016.
 */
import com.fasterxml.jackson.annotation.JsonProperty;


public class PxwebItem {
    @JsonProperty("id")
    public String id;
    @JsonProperty("text")
    public String text;
    @JsonProperty("type")
    public String type;
    @JsonProperty("updated")
    public String updated;
}