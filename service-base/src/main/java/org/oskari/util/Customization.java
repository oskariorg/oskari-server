package org.oskari.util;

import fi.nls.oskari.cache.Cache;
import fi.nls.oskari.cache.CacheManager;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import org.json.JSONArray;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Customization {
    private static final String SVG_MARKERS_JSON = "svg-markers.json";
    private static Cache<String> CUSTOMIZATION = CacheManager.getCache("Customization");
    private static final String CACHE_KEY_MARKERS = "markers";
    private static final String LOGO_PATH_DEFAULT = "logo.png";
    private static final String LOGO_PATH = PropertyUtil.get("print.logo.path", LOGO_PATH_DEFAULT);

    private static Logger getLogger() {
        return LogFactory.getLogger(Customization.class);
    }

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
            JSONArray list = null;
            try {
                list = JSONHelper.createJSONArray(json);
                CUSTOMIZATION.put(CACHE_KEY_MARKERS, json);
            } catch (IllegalArgumentException e) {
                getLogger().warn("Markers not found");
            }
            return list;
        }
    }


    public static InputStream getLogo() throws IOException {
        // Try file
        Path pathToLogo = Paths.get(LOGO_PATH);
        try (InputStream in = Files.newInputStream(pathToLogo)) {
            return new BufferedInputStream(in);
        } catch (NoSuchFileException e) {
            // print out absolute path so it's easier to debug proper value in config
            getLogger().debug("Logo file " + pathToLogo.toAbsolutePath() + " does not exist. Trying from classpath.");
        } catch (IOException e) {
            getLogger().warn(e, "Failed to read logo from file");
        }
        // File didn't work, try resources file
        try (InputStream in = Customization.class.getResourceAsStream(LOGO_PATH)) {
            if (in == null) {
                getLogger().debug("Resource file " + LOGO_PATH + " does not exist");
            }
            return new BufferedInputStream(in);
        } catch (IOException e) {
            getLogger().warn("Failed to read logo from resource " + LOGO_PATH);
            throw e;
        }
    }
}
