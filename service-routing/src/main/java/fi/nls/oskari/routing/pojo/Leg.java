package fi.nls.oskari.routing.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import fi.nls.oskari.routing.pojo.v1.LegGeometry;

import java.util.ArrayList;
import java.util.List;

public class Leg {

    @JsonProperty("legGeometry")
    private LegGeometry legGeometry;

    @JsonProperty("mode")
    private String mode;

    @JsonProperty("distance")
    private Double distance;

    @JsonProperty("start")
    private ScheduledTime start;
    @JsonProperty("end")
    private ScheduledTime end;

    @JsonProperty("steps")
    private List<Step> steps = new ArrayList();

    public LegGeometry getLegGeometry() {
        return legGeometry;
    }

    public void setLegGeometry(LegGeometry legGeometry) {
        this.legGeometry = legGeometry;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public Double getDistance() {
        return distance;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }

    public ScheduledTime getStart() {
        return start;
    }

    public void setStart(ScheduledTime start) {
        this.start = start;
    }

    public ScheduledTime getEnd() {
        return end;
    }

    public void setEnd(ScheduledTime end) {
        this.end = end;
    }

    public List<Step> getSteps() {
        return steps;
    }

    public void setSteps(List<Step> steps) {
        this.steps = steps;
    }
}
