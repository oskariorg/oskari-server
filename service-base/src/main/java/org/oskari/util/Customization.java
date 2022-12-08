package org.oskari.util;

import fi.nls.oskari.cache.Cache;
import fi.nls.oskari.cache.CacheManager;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.JSONHelper;
import org.json.JSONArray;

import java.io.IOException;
import java.io.InputStream;

public class Customization {
    private static final String SVG_MARKERS_JSON = "svg-markers.json";
    private static Cache<String> CUSTOMIZATION = CacheManager.getCache("Customization");
    private static final String CACHE_KEY_MARKERS = "markers";

    public static JSONArray getMarkers() throws IOException {
        // caches are mutable so store as string in case someone modifies the json
        String json = CUSTOMIZATION.get(CACHE_KEY_MARKERS);
        if (json != null) {
            return JSONHelper.createJSONArray(json);
        }
        try (InputStream is = Customization.class.getResourceAsStream(SVG_MARKERS_JSON)) {
            if (is == null) {
                throw new IOException("Resource file " + SVG_MARKERS_JSON + " does not exist");
            }
            json = IOHelper.readString(is);
            CUSTOMIZATION.put(CACHE_KEY_MARKERS, json);
            return JSONHelper.createJSONArray(json);
        }
    }
}
