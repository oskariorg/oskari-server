package org.oskari.service.wfs.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.JSONHelper;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.json.JSONObject;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import fi.nls.oskari.service.ServiceRuntimeException;
import org.oskari.geojson.GeoJSONReader2;
import org.oskari.geojson.GeoJSONSchemaDetector;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import static fi.nls.oskari.service.capabilities.CapabilitiesConstants.KEY_FEATURE_OUTPUT_FORMATS;
import static fi.nls.oskari.service.capabilities.CapabilitiesConstants.KEY_MAX_FEATURES;

public class OskariWFSClient {

    private static final Logger LOG = LogFactory.getLogger(OskariWFS110Client.class);
    private static final String EXC_HANDLING_OUTPUTFORMAT = "outputformat";
    private static final TypeReference<HashMap<String, Object>> TYPE_REF = new TypeReference<HashMap<String, Object>>() {};
    private static final ObjectMapper OM = new ObjectMapper();
    private static final int MAX_REDIRECTS = 5;
    private static final String PROPERTY_FORCE_GML = "forceGML";
    private static final String JSON_OUTPUT_FORMAT = "application/json";
    private static final int DEFAULT_MAX_FEATURES = 10000;

    public SimpleFeatureCollection getFeatures(OskariLayer layer,
            ReferencedEnvelope bbox, CoordinateReferenceSystem crs, Filter filter) {
        return new OskariWFSLoadCommand(layer, bbox, crs, filter).execute();
    }

    // Common methods for WFS 1.1.0 and 2.0.0 clients
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

    protected static SimpleFeatureCollection getFeatures(String endPoint,
            String user, String pass, Map<String, String> query,
            CoordinateReferenceSystem crs, boolean tryGeoJSON, OskariGMLDecoder gmlDecoder) {
        String url; // for debugging

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] response;
        Map<String, String> responseHeaders;
        SimpleFeatureCollection fc;

        if (tryGeoJSON) {
            // First try GeoJSON
            query.put("OUTPUTFORMAT", "application/json");
            url = IOHelper.constructUrl(endPoint, query);
            responseHeaders = OskariWFSClient.readResponseTo(endPoint, user, pass, query, baos);
            response = baos.toByteArray();
            // TODO: Select parsing algorithm based on response headers (Content-Type)
            fc = parseGeoJSON(response, crs, url);
            if (fc != null) {
                return fc;
            }
            // Try to parse the same response as GML
            fc = parseGML(response, crs, url, user, pass, gmlDecoder);
            if (fc != null) {
                LOG.info("Requested JSON but got GML. Possibly misconfigured service for", url);
                return fc;
            }
            // Okay I guess it wasn't a GML FeatureCollection either - move on
            LOG.warn("Requested JSON but didn't get a parseable result. Making a new request for GML. Possibly misconfigured service for", url);
        }

        // Fallback to to requesting GML
        query.remove("OUTPUTFORMAT");
        url = IOHelper.constructUrl(endPoint, query);
        baos.reset();
        responseHeaders = OskariWFSClient.readResponseTo(endPoint, user, pass, query, baos);
        response = baos.toByteArray();
        fc = parseGML(response, crs, url, user, pass, gmlDecoder);
        if (fc != null) {
            return fc;
        }

        throw new ServiceRuntimeException("Failed to get features");
    }

    private static Map<String, String> readResponseTo(String endPoint,
            String user, String pass, Map<String, String> query, OutputStream out) {
        try {
            HttpURLConnection conn = getConnection(endPoint, user, pass, query);
            Map<String, String> responseHeaders = new HashMap<>();
            conn.getHeaderFields().forEach((k, v) -> responseHeaders.put(k, v.get(0)));
            IOHelper.readBytesTo(conn, out);
            return responseHeaders;
        } catch (IOException e) {
            throw new ServiceRuntimeException("Unable to read response", e);
        }
    }

    private static HttpURLConnection getConnection(String endPoint,
                                                     String user, String pass, Map<String, String> query) throws IOException {
        HttpURLConnection conn = IOHelper.getConnection(endPoint, user, pass, query);
        conn = IOHelper.followRedirect(conn, user, pass, query, MAX_REDIRECTS);
        int sc = conn.getResponseCode();
        if (sc != 200) {
            throw new ServiceRuntimeException("Unexpected status code " + sc, Integer.toString(sc));
        }
        return conn;
    }

    private static SimpleFeatureCollection parseGeoJSON(byte[] response, CoordinateReferenceSystem crs, String url) {
        try {
            InputStream in = new ByteArrayInputStream(response);
            Map<String, Object> geojson = OM.readValue(in, TYPE_REF);
            boolean ignoreGeometryProperties = true;
            SimpleFeatureType schema = GeoJSONSchemaDetector.getSchema(geojson, crs, ignoreGeometryProperties);
            return GeoJSONReader2.toFeatureCollection(geojson, schema);
        } catch (Exception e) {
            LOG.info(e, "Unable to parse GeoJSON from", url);
            LOG.debug("Response from", url, "was:\n", new String(response, StandardCharsets.UTF_8));
            return null;
        }
    }

    private static SimpleFeatureCollection parseGML(byte[] response, CoordinateReferenceSystem crs, String url, String user, String pass, OskariGMLDecoder gmlDecoder) {
        try {
            return gmlDecoder.decodeFeatureCollection(new ByteArrayInputStream(response), user, pass);
        } catch (Exception e) {
            LOG.info(e, "Unable to parse GML from", url);
            LOG.debug("Response from", url, "was:\n", new String(response, StandardCharsets.UTF_8));
            return null;
        }
    }

    protected static boolean tryGeoJSON (OskariLayer layer) {
        if(layer.getAttributes().optBoolean(PROPERTY_FORCE_GML, false)) return false;

        JSONObject capa = layer.getCapabilities();
        if (capa.has(KEY_FEATURE_OUTPUT_FORMATS)) {
            List<String> formats = JSONHelper.getArrayAsList(JSONHelper.getJSONArray(capa, KEY_FEATURE_OUTPUT_FORMATS));
            return formats.contains(JSON_OUTPUT_FORMAT);
        }
        return true;
    }
    protected static int getMaxFeatures(OskariLayer layer) {
        return layer.getCapabilities().optInt(KEY_MAX_FEATURES, DEFAULT_MAX_FEATURES);
    }

}
