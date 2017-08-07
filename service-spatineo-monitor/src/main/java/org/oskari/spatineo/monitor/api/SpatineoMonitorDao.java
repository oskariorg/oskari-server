package org.oskari.spatineo.monitor.api;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.oskari.spatineo.monitor.api.model.Response;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;

public class SpatineoMonitorDao {

    private static final Logger LOG = LogFactory.getLogger(SpatineoMonitorDao.class);
    private static final String CONTENT_TYPE_JSON = "application/json; charset=utf-8";

    private final URL url;
    private final ObjectMapper om;

    public SpatineoMonitorDao(String endPoint, String key) {
        this(endPoint, key, false, false);
    }
    
    public SpatineoMonitorDao(String endPoint, String key, boolean allMeters, boolean lean) {
        this(endPoint, key, allMeters, lean, null);
    }

    public SpatineoMonitorDao(String endPoint, String key, boolean allMeters, boolean lean, ObjectMapper om) {
        try {
            this.url = new URL(getRequestURL(endPoint, key, allMeters, lean));
            this.om = om == null ? createObjectMapper() : om;
        } catch (MalformedURLException e) {
            LOG.error(e);
            throw new IllegalStateException("Initialization failed!");
        }
    }

    private static String getRequestURL(String endPoint, String key, boolean allMeters, boolean lean) {
        if (endPoint == null || endPoint.length() == 0) {
            throw new IllegalArgumentException("endPoint null or empty!");
        }
        if (key == null || key.length() == 0) {
            throw new IllegalArgumentException("private access key null or empty!");
        }
        StringBuilder sb = new StringBuilder(endPoint);
        sb.append("?privateAccessKey").append(key);
        if (allMeters) {
            sb.append("&allMeters=true");
        }
        if (lean) {
            sb.append("&lean=true");
        }
        return sb.toString();
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
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            int sc = conn.getResponseCode();
            String cType = conn.getContentType();
            if (sc != HttpURLConnection.HTTP_OK) {
                LOG.warn("Request: ", url.toString() 
                        + " failed! Unexpected Status Code: ", sc);
                return null;
            }
            if (!CONTENT_TYPE_JSON.equals(cType)) {
                LOG.warn("Request: ", url.toString() 
                        + " failed! Unexpected Content-Type: ", cType);
                return null;
            }
            try (InputStream in = conn.getInputStream()) {
                return om.readValue(in, Response.class);
            }
        } catch (IOException e) {
            LOG.warn(e, "Request: ", url.toString());
        }
        return null;
    }

}
