package fi.nls.oskari.routing.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;


public class StopPosition {

    // Either we will have position set or next + previous set but not both
    @JsonProperty("position")
    private Integer position;

    @JsonProperty("nextPosition")
    private Integer nextPosition;

    @JsonProperty("previousPosition")
    private Integer previousPosition;

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public Integer getNextPosition() {
        return nextPosition;
    }

    public void setNextPosition(Integer nextPosition) {
        this.nextPosition = nextPosition;
    }

    public Integer getPreviousPosition() {
        return previousPosition;
    }

    public void setPreviousPosition(Integer previousPosition) {
        this.previousPosition = previousPosition;
    }
}
