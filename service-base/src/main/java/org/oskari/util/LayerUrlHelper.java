package org.oskari.util;

import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.IOHelper;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class LayerUrlHelper {

    private static final Set<String> RESTRICTED_PARAMS = ConversionHelper.asSet("request", "service", "version");

    public static String getSanitizedUrl(String pUrl) {
        String baseUrl = IOHelper.removeQueryString(pUrl);
        Map<String, List<String>> params = IOHelper.parseQuerystring(pUrl);
        String url = baseUrl;

        for (String key : params.keySet()) {
            if (!RESTRICTED_PARAMS.contains(key.toLowerCase())) {
                // not the most efficient way, but just regenerating the url without restricted params
                url = IOHelper.addUrlParam(url, key, params.get(key).stream().toArray(String[]::new));
            }
        }
        return url;
    }
}
