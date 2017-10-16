package org.oskari.wcs.request;

import java.util.Map;
import org.oskari.utils.common.Maps;

public class GetCapabilities {

    private static final Map<String, String> QUERY_PARAMS = Maps.of(
            "service", "WCS",
            "request", "GetCapabilities");

    private GetCapabilities() { /* Block */ }

    /**
     * Get query parameters for a GET/KVP encoded GetCapabilities request
     * @return Map with query parameters
     */
    public static Map<String, String> toQueryParameters() {
        return QUERY_PARAMS;
    }

}
