package org.oskari.spatineo.monitor.api;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.IOHelper;
import org.oskari.spatineo.monitor.api.model.Response;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.LinkedHashMap;
import java.util.Map;

public class SpatineoMonitorDao {

    private static final Logger LOG = LogFactory.getLogger(SpatineoMonitorDao.class);
    private static final String CONTENT_TYPE_JSON = "application/json";

    private static final String PARAM_ACCESS_KEY = "privateAccessKey";
    private static final String PARAM_ALL_METERS = "allMeters";
    private static final String PARAM_LEAN = "lean";

    private final String url;
    private final ObjectMapper om;

    public SpatineoMonitorDao(String endPoint, String key) {
        this(endPoint, key, false, false);
    }
    
    public SpatineoMonitorDao(String endPoint, String key,
            boolean allMeters, boolean lean) {
        this(endPoint, key, allMeters, lean, null);
    }

    public SpatineoMonitorDao(String endPoint, String key,
            boolean allMeters, boolean lean, ObjectMapper om) {
        if (endPoint == null || endPoint.length() == 0) {
            throw new IllegalArgumentException("endPoint null or empty!");
        }
        this.url = IOHelper.constructUrl(endPoint, getParams(key, allMeters, lean));
        this.om = om == null ? createObjectMapper() : om;
    }

    private static Map<String, String> getParams(String key, boolean allMeters, boolean lean) {
        final Map<String, String> params = new LinkedHashMap<>();
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("Access key null or empty!");
        }
        params.put(PARAM_ACCESS_KEY, key);
        if (allMeters) {
            params.put(PARAM_ALL_METERS, "true");
        }
        if (lean) {
            params.put(PARAM_LEAN, "true");
        }
        return params;
    }

    private static ObjectMapper createObjectMapper() {
        ObjectMapper om = new ObjectMapper();
        om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return om;
    }

    /**
     * Queries the service for status
     * Converts the JSON response to a Response object
     * @return the Response, null if something went wrong
     */
    public Response query() {
        try {
            final HttpURLConnection conn = IOHelper.getConnection(url);
            final int sc = conn.getResponseCode();
            if (sc != HttpURLConnection.HTTP_OK) {
                LOG.warn("Request:", url, "failed! Unexpected Status Code:", sc);
                return null;
            }
            final String contentType = conn.getContentType();
            if (contentType == null || !contentType.startsWith(CONTENT_TYPE_JSON)) {
                LOG.warn("Request:", url, "failed! Unexpected Content-Type:", contentType);
                return null;
            }
            try (InputStream in = conn.getInputStream()) {
                return parse(in);
            }
        } catch (IOException e) {
            LOG.warn(e, "Request: ", url.toString());
            return null;
        }
    }

    protected Response parse(final InputStream in) throws IOException {
        return om.readValue(in, Response.class);
    }

}
