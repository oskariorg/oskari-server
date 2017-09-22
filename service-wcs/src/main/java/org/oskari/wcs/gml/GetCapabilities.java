package org.oskari.wcs.gml;

import java.util.Map;
import java.util.TreeMap;

public class GetCapabilities {

    /**
     * Create query parameters for a GET encoded GetCapabilities request
     * @return Map with query parameters
     */
    public static Map<String, String> toQueryParameters() {
        Map<String, String> params = new TreeMap<>();
        params.put("service", "WCS");
        params.put("version", "2.0.1");
        params.put("request", "GetCapabilities");
        return params;
    }
    
}
