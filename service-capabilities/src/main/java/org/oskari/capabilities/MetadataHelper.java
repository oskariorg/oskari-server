package org.oskari.capabilities;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.PropertyUtil;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class MetadataHelper {
    private static final Logger LOG = LogFactory.getLogger(MetadataHelper.class);
    /**
     * Helper for parsing metadata uuid from url.
     * @param url
     * @return
     */
    public static String getIdFromMetadataUrl(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }
        if (!url.toLowerCase().startsWith("http")) {
            // not a url -> return as is
            return url;
        }
        // check if allowedDomains arraylist contains the metadata url
        if (!isDomainAllowed(url, getAllowedDomainsList())) {
            return null;
        }

        try {
            Map<String, List<String>> params = IOHelper.parseQuerystring(url);
            String idParam = params.keySet().stream()
                    .filter(key -> "uuid".equalsIgnoreCase(key) || "id".equalsIgnoreCase(key))
                    .findFirst()
                    .orElse(null);
            if (idParam == null) {
                // param not in url
                return null;
            }
            List<String> values = params.getOrDefault(idParam, Collections.emptyList());
            if (values.isEmpty()) {
                // param was present but has no value
                return null;
            }
            return values.get(0);
        } catch (Exception ignored) {
            // propably just not valid URL
            LOG.ignore("Unexpected error parsing metadataid", ignored);
        }
        LOG.debug("Couldn't parse uuid from metadata url:", url);
        return null;
    }

    public static ArrayList<String> getAllowedDomainsList()  {
        ArrayList<String> allowedDomains = new ArrayList<String>(Arrays.asList(PropertyUtil.getCommaSeparatedList("service.metadata.domains")));
        //add property url if allowedDomains list is empty
        if (!allowedDomains.isEmpty()) {
            allowedDomains.add(PropertyUtil.get("service.metadata.url"));
        }
        return allowedDomains;  
    }

    public static boolean isDomainAllowed(String url, List<String> allowedDomains) {	
        if (allowedDomains.isEmpty()) {
            return true;
        }
        try {	
            URI uri = new URI(url);
            for (String a : allowedDomains) {
                if (uri.getHost().endsWith(a)) {
                    return true;
                }
            } 
        } catch (Exception e) {
            LOG.debug("Unexpected error converting url-string to uri:", e);
        }
        return false;
    }
}
