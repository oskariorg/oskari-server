package fi.nls.oskari.routing.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ElevationProfileComponent {
    @JsonProperty("distance")
    private float distance;
    @JsonProperty("elevation")
    private float elevation;
}
