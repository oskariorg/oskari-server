package fi.nls.oskari.spatineo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.util.ArrayList;
import java.util.List;

/**
 * A new data transfer object for interacting with the Spatineo monitoring JSON
 * interface.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SpatineoMonitoringResponseDto {

    private boolean error;

    public boolean isError() {
        return error;
    }

    /**
     * Find a Meter from the list of Services that has the matching layerName
     * @param name Name of the layer
     * @return Meter
     */
    public Meter getMeterByLayerName(String name) {
        for (Result r : result) {
            Service s = r.service;
                for (Meter m : s.meters) {
                    if (m.layerName.equals(name)) {
                        return m;
                    }
                }
            }
        
        // not found
        return null;
    }
    
    public class Configuration {

        public Integer groupId;
    }

    public String version;

    @JsonProperty("status")
    public String status;

    @JsonProperty("status")
    public String getStatus() {
        return status;
    }

    @JsonProperty("status")
    public void setId(String status) {
        this.status = status;
        if (status.equals("ERROR")) {
            error = true;
        }
    }

    public String statusMessage;
    public Configuration configuration;
    public List<Result> result;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Result {

        public Service service;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Service {

        public String id;
        public Integer serviceId;
        public String serviceType;
        public String serviceUrl;
        public String title;
        public List<Meter> meters = new ArrayList<Meter>();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Meter {

        public String id;
        public String layerName;
        public String crs;
        public Integer imageHeight;
        public Integer imageWidth;
        public String format;
        public String operation;
        public String monitorLink;
        public Indicator indicator;
    }
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Indicator {

        public String status;
        public String lastChange;
        public String name;
    }
    
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("status", status)
                .add("version", version)
                .add("status", result)
                .toString();
    }
}
