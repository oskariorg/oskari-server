package fi.nls.oskari.routing.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;
import java.time.ZoneId;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Agency {
    @JsonProperty("name")
    private String name;

    @JsonProperty("gtfsId")
    private String gtfsId;

    @JsonProperty("timezone")
    private String timezone;

    @JsonProperty("url")
    private String url;

    public String getGtfsId() {
        return gtfsId;
    }

    public void setGtfsId(String gtfsId) {
        this.gtfsId = gtfsId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public Long getTimeZoneOffset() {
        Long timezoneOffsetMillis = (long) (OffsetDateTime.now(ZoneId.of(this.timezone)).getOffset().getTotalSeconds() * 1000);
        return timezoneOffsetMillis;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
