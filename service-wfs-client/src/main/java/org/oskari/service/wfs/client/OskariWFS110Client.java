package org.oskari.service.wfs.client;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
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
            String typeName, ReferencedEnvelope bbox, CoordinateReferenceSystem crs,
            int maxFeatures, Filter filter) {
        // First try GeoJSON
        Map<String, String> query = getQueryParams(typeName, bbox, crs, maxFeatures, filter);
        query.put("OUTPUTFORMAT", "application/json");

        byte[] response = getResponse(endPoint, user, pass, query);
        try {
            return parseGeoJSON(new ByteArrayInputStream(response), crs);
        } catch (IOException e) {
            if (!isOutputFormatInvalid(new ByteArrayInputStream(response))) {
                // If we can not determine that the exception was due to bad
                // outputFormat parameter then don't bother trying GML
                throw new ServiceRuntimeException("Unable to parse GeoJSON", e);
            }
        }

        // Fallback to GML
        query.remove("OUTPUTFORMAT");
        response = getResponse(endPoint, user, pass, query);

        try {
            return OSKARI_GML.decodeFeatureCollection(new ByteArrayInputStream(response), user, pass);
        } catch (Exception e) {
            throw new ServiceRuntimeException("Unable to parse GML", e);
        }
    }

    public static SimpleFeatureCollection getFeaturesGeoJSON(String endPoint, String user, String pass,
            String typeName, ReferencedEnvelope bbox, CoordinateReferenceSystem crs,
            int maxFeatures, Filter filter) throws IOException {
        Map<String, String> query = getQueryParams(typeName, bbox, crs, maxFeatures, filter);
        query.put("OUTPUTFORMAT", "application/json");
        HttpURLConnection conn = getConnection(endPoint, user, pass, query);
        String contentType = conn.getContentType();
        if (contentType != null && !contentType.contains("json")) {
            throw new ServiceRuntimeException("Unexpected content type " + contentType);
        }
        try (InputStream in = new BufferedInputStream(conn.getInputStream())) {
            return parseGeoJSON(in, crs);
        }
    }

    private static SimpleFeatureCollection parseGeoJSON(InputStream in,
            CoordinateReferenceSystem crs) throws IOException {
        Map<String, Object> geojson = OM.readValue(in, TYPE_REF);
        in.close();
        SimpleFeatureType schema = GeoJSONSchemaDetector.getSchema(geojson, crs);
        return GeoJSONReader2.toFeatureCollection(geojson, schema);
    }

    public static SimpleFeatureCollection getFeaturesGML(String endPoint, String user, String pass,
            String typeName, ReferencedEnvelope bbox, CoordinateReferenceSystem crs,
            int maxFeatures, Filter filter) throws Exception {
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

    protected static byte[] getResponse(String endPoint,
            String user, String pass, Map<String, String> query) {
        try {
            HttpURLConnection conn = getConnection(endPoint, user, pass, query);
            return IOHelper.readBytes(conn);
        } catch (IOException e) {
            throw new ServiceRuntimeException("Unable to read response", e);
        }
    }

    protected static HttpURLConnection getConnection(String endPoint,
            String user, String pass, Map<String, String> query) throws IOException {
        HttpURLConnection conn = IOHelper.getConnection(endPoint, user, pass, query);
        conn = IOHelper.followRedirect(conn, user, pass, query, MAX_REDIRECTS);
        int sc = conn.getResponseCode();
        if (sc != 200) {
            throw new ServiceRuntimeException("Unexpected status code " + sc);
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
            throw new ServiceRuntimeException("Failed to encode filter!", e);
        }
    }

    protected static boolean isOutputFormatInvalid(InputStream in) {
        try {
            OWSException ex = OWSExceptionReportParser.parse(in);
            return isExceptionDueToInvalidOutputFormat(ex);
        } catch (Exception e) {
            LOG.debug(e);
            return false;
        }
    }

    protected static boolean isExceptionDueToInvalidOutputFormat(OWSException ex) {
        if (ex.getExceptionCode().equalsIgnoreCase("InvalidParameterValue")) {
            if ("outputFormat".equalsIgnoreCase(ex.getLocator())) {
                return true;
            }
        }
        return false;
    }

}
