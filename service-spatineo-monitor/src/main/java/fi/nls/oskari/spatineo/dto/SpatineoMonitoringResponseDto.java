package fi.nls.oskari.spatineo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.google.common.base.MoreObjects;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

/**
 * A new data transfer object for interacting with the Spatineo monitoring JSON
 * interface.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SpatineoMonitoringResponseDto {

    private static final Logger LOG = LogFactory.getLogger(SpatineoMonitoringResponseDto.class);
    private boolean error;

    public boolean isError() {
        return error;
    }
    
    /**
     * Find a Meter from Services/Meters that has the matching layerName
     * 
     * @param name Name of the layer
     * @return Meter
     */
    public Meter getMeterByLayerName(String name, String url) {
        for (Result r : result) {            
            Service s = r.service;
            if (!s.serviceUrl.equalsIgnoreCase(url)) {
                continue;
            }
            for (Meter m : s.meters) {
                if (m.layerTarget.equals(name)) {
                    return m;
                }
            }
        }
        
        // not found
        return null;
    }

    /**
     * Find a Meter from the list of Services that has the matching serviceUrl
     * 
     * @param name Name of the layer
     * @return Meter
     */
    public Meter getMeterByServiceName(String name, String url) {
        for (Result r : result) {
            Service s = r.service;
            if (s == null || s.serviceUrl == null || !s.serviceUrl.equalsIgnoreCase(url)) {
                continue;
            }
            for (Meter m : s.meters) {
                if (m.layerName.equals(name)) {
                    return m;
                }
            }
        }
        
        // not found
        return null;
    }
    
    // for debugging
    public String getLayerNames() {
        StringBuffer sb = new StringBuffer();
        for (Result r : result) {
            Service s = r.service;
            for (Meter m : s.meters) {
                sb.append(m.layerName);
                sb.append("\n");
            }
        }
        return sb.toString();
    }
    
    public class Configuration {

        public Long groupId;
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
        
        public String layerName;        // the part before ":"
        public String layerTarget;      // the part after ":"
        
        public String crs;
        public Integer imageHeight;
        public Integer imageWidth;
        public String format;
        public String operation;
        public String monitorLink;
        public Indicator indicator;
        
        @JsonSetter("layerName")
        public void setLayerName(String layerName) {
            String[] parts = layerName.split(":");
            try {
                this.layerName = URLDecoder.decode(parts[0], "UTF-8");                   
            } catch (UnsupportedEncodingException e) {
                LOG.error("Failed to url decode layerName=" + parts[0]);
            }

            // not all layernames have the target part
            if (parts.length > 1) {
                layerTarget = parts[1];
            }
        }
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
