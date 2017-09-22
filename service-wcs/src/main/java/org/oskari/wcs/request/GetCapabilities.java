package org.oskari.wcs.request;

import org.oskari.wcs.util.small.SmallMap;

import java.util.Map;

public class GetCapabilities {

    private static final Map<String, String> QUERY_PARAMS = new SmallMap("service", "WCS",
            "request", "GetCapabilities");

    private GetCapabilities() { /* Block */
    }

    /**
     * Get query parameters for a GET/KVP encoded GetCapabilities request
     * 
     * @return Map with query parameters
     */
    public static Map<String, String> toQueryParameters() {
        return QUERY_PARAMS;
    }

}
