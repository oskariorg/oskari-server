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

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
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
            ReferencedEnvelope bbox, CoordinateReferenceSystem crs, Filter filter) throws ServiceRuntimeException {
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
            throw new ServiceRuntimeException("Unexpected status code " + sc, Integer.toString(sc));
        }
        return conn;
    }

    protected static SimpleFeatureCollection parseGeoJSON(InputStream in,
                                                        CoordinateReferenceSystem crs) throws IOException {
        Map<String, Object> geojson = OM.readValue(in, TYPE_REF);
        boolean ignoreGeometryProperties = true;
        SimpleFeatureType schema = GeoJSONSchemaDetector.getSchema(geojson, crs, ignoreGeometryProperties);
        return GeoJSONReader2.toFeatureCollection(geojson, schema);
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

    protected static boolean isOutputFormatInvalid(InputStream in) {
        try {
            OWSException ex = OWSExceptionReportParser.parse(in);
            return isExceptionDueToInvalidOutputFormat(ex);
        } catch (Exception e) {
            LOG.debug(e);
            return false;
        }
    }

    /**
     * We might get:
     * <ows:ExceptionReport xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:ows="http://www.opengis.net/ows" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.opengis.net/ows http://schemas.opengis.net/ows/1.1.0/owsExceptionReport.xsd" version="1.1.0">
     * <ows:Exception exceptionCode="InvalidParameterValue" locator="Unknown">
     * <ows:ExceptionText>
     * <![CDATA[ OutputFormat 'application/json' not supported. ]]>
     * </ows:ExceptionText>
     * </ows:Exception>
     * </ows:ExceptionReport>
     * @param ex
     * @return
     */
    protected static boolean isExceptionDueToInvalidOutputFormat(OWSException ex) {
        if (ex.getExceptionCode().equalsIgnoreCase("InvalidParameterValue")) {
            if (EXC_HANDLING_OUTPUTFORMAT.equalsIgnoreCase(ex.getLocator())) {
                return true;
            }
            if (ex.getExceptionText() != null &&
                    ex.getExceptionText().toLowerCase().contains(EXC_HANDLING_OUTPUTFORMAT)) {
                return true;
            }
        }
        return false;
    }
}
