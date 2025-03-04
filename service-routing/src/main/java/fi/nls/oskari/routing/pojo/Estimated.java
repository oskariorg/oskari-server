package fi.nls.oskari.routing.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Duration;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Estimated {
    @JsonProperty("delay")
    private String delay;

    public String getDelay() {
        return delay;
    }

    public void setDelay(String delay) {
        this.delay = delay;
    }

    public long getDelayMilliseconds() {
        return Duration.parse(this.delay).toMillis();
    }
}
