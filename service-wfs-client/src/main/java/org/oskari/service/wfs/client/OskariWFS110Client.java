package org.oskari.service.wfs.client;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.filter.v1_0.OGCConfiguration;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.xml.Encoder;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.oskari.geojson.GeoJSONReader2;
import org.oskari.geojson.GeoJSONSchemaDetector;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.ServiceRuntimeException;
import fi.nls.oskari.util.IOHelper;

/**
 * Client code for WFS 1.1.0 services
 */
public class OskariWFS110Client {

    private static final Logger LOG = LogFactory.getLogger(OskariWFS110Client.class);
    private static final OskariGML OSKARI_GML = new OskariGML();
    private static final int MAX_REDIRECTS = 5;
    private static final ObjectMapper OM = new ObjectMapper();
    private static final TypeReference<HashMap<String, Object>> TYPE_REF = new TypeReference<HashMap<String, Object>>() {};

    private OskariWFS110Client() {}

    /**
     * @return SimpleFeatureCollection containing the parsed Features, or null if all fails
     */
    public static SimpleFeatureCollection getFeatures(String endPoint, String user, String pass,
            String typeName, ReferencedEnvelope bbox, CoordinateReferenceSystem crs, int maxFeatures, Filter filter) {
        // First try GeoJSON
        Map<String, String> query = getQueryParams(typeName, bbox, crs, maxFeatures, filter);

        query.put("OUTPUTFORMAT", "application/json");
        HttpURLConnection conn;
        try {
            conn = getConnection(endPoint, user, pass, query);
        } catch (Exception e) {
            LOG.debug(e);
            throw new ServiceRuntimeException("Unable to get features");
        }

        Map<String, Object> geojson = null;
        try (InputStream in = new BufferedInputStream(conn.getInputStream())) {
            try {
                in.mark(8192);
                geojson = OM.readValue(in, TYPE_REF);
            } catch (Exception e) {
                in.reset();
                if (!isOutputFormatInvalid(in)) {
                    throw new ServiceRuntimeException("Unable to get features");
                }
            }
        } catch (IOException e) {
            LOG.debug(e);
        }

        if (geojson != null) {
            try {
                SimpleFeatureType schema = GeoJSONSchemaDetector.getSchema(geojson, crs);
                return GeoJSONReader2.toFeatureCollection(geojson, schema);
            } catch (Exception e) {
                LOG.debug(e);
            }
        }

        query.remove("OUTPUTFORMAT");
        try {
            conn = getConnection(endPoint, user, pass, query);
        } catch (Exception e) {
            LOG.debug(e);
            throw new ServiceRuntimeException("Unable to get features");
        }

        try (InputStream in = new BufferedInputStream(conn.getInputStream())) {
            return OSKARI_GML.decodeFeatureCollection(in, user, pass);
        } catch (Exception e) {
            throw new ServiceRuntimeException("Unable to get features");
        }
    }

    public static SimpleFeatureCollection getFeaturesGeoJSON(String endPoint, String user, String pass,
            String typeName, ReferencedEnvelope bbox, CoordinateReferenceSystem crs, int maxFeatures, Filter filter) throws Exception {
        Map<String, String> query = getQueryParams(typeName, bbox, crs, maxFeatures, filter);
        query.put("OUTPUTFORMAT", "application/json");
        HttpURLConnection conn = getConnection(endPoint, user, pass, query);
        String contentType = conn.getContentType();
        if (contentType != null && !contentType.contains("json")) {
            throw new Exception("Unexpected content type " + contentType);
        }
        Map<String, Object> geojson;
        try (InputStream in = new BufferedInputStream(conn.getInputStream())) {
            geojson = OM.readValue(in, TYPE_REF);
        }
        SimpleFeatureType schema = GeoJSONSchemaDetector.getSchema(geojson, crs);
        return GeoJSONReader2.toFeatureCollection(geojson, schema);
    }

    public static SimpleFeatureCollection getFeaturesGML(String endPoint, String user, String pass, String typeName,
            ReferencedEnvelope bbox, CoordinateReferenceSystem crs, int maxFeatures, Filter filter) throws Exception {
        Map<String, String> query = getQueryParams(typeName, bbox, crs, maxFeatures, filter);
        HttpURLConnection conn = getConnection(endPoint, user, pass, query);
        try (InputStream in = new BufferedInputStream(conn.getInputStream())) {
            return OSKARI_GML.decodeFeatureCollection(in, user, pass);
        }
    }

    protected static Map<String, String> getQueryParams(String typeName, ReferencedEnvelope bbox,
            CoordinateReferenceSystem crs, int maxFeatures, Filter filter) {
        Map<String, String> parameters = new LinkedHashMap<>();
        parameters.put("SERVICE", "WFS");
        parameters.put("VERSION", "1.1.0");
        parameters.put("REQUEST", "GetFeature");
        parameters.put("TYPENAME", typeName);
        parameters.put("SRSNAME", crs.getIdentifiers().iterator().next().toString());
        if (filter == null) {
            parameters.put("BBOX", getBBOX(bbox));
        } else {
            parameters.put("FILTER", getFilter(filter));
        }
        parameters.put("MAXFEATURES", Integer.toString(maxFeatures));
        return parameters;
    }

    protected static HttpURLConnection getConnection(String endPoint,
            String user, String pass, Map<String, String> query) throws Exception {
        HttpURLConnection conn = IOHelper.getConnection(endPoint, user, pass, query);
        conn = IOHelper.followRedirect(conn, user, pass, query, MAX_REDIRECTS);
        if (conn.getResponseCode() != 200) {
            throw new Exception("Unexpected status code " + conn.getResponseCode());
        }
        return conn;
    }

    protected static String getBBOX(ReferencedEnvelope bbox) {
        if (bbox == null) {
            return null;
        }
        String srsName = bbox.getCoordinateReferenceSystem()
                .getIdentifiers()
                .iterator()
                .next()
                .toString();
        return String.format(Locale.US, "%f,%f,%f,%f,%s",
                bbox.getMinX(), bbox.getMinY(),
                bbox.getMaxX(), bbox.getMaxY(),
                srsName);
    }

    protected static String getFilter(Filter filter) {
        if (filter == null) {
            return null;
        }
        try {
            Encoder encoder = new Encoder(new OGCConfiguration());
            encoder.setOmitXMLDeclaration(true);
            return encoder.encodeAsString(filter, org.geotools.filter.v1_0.OGC.Filter);
        } catch (IOException e) {
            LOG.warn("Failed to encode filter!");
        }
        return null;
    }

    protected static boolean isOutputFormatInvalid(InputStream in) {
        try {
            OWSException ex = OWSExceptionReportParser.parse(in);
            return isExceptionDueToInvalidOutputFormat(ex);
        } catch (Exception e) {
            return false;
        }
    }

    protected static boolean isExceptionDueToInvalidOutputFormat(OWSException ex) {
        if (ex.getExceptionCode().equalsIgnoreCase("InvalidParameterValue")) {
            if (ex.getLocator() != null && ex.getLocator().equalsIgnoreCase("outputFormat")) {
                return true;
            }
        }
        return false;
    }

}
