package org.oskari.service.wfs.client;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.filter.v1_0.OGCConfiguration;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.xml.Encoder;
import org.opengis.filter.Filter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import fi.nls.oskari.domain.map.OskariLayer;
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
    private static final String PROPERTY_FORCE_GML = "forceGML";

    private OskariWFS110Client() {}

    /**
     * @return SimpleFeatureCollection containing the parsed Features, or null if all fails
     */
    public static SimpleFeatureCollection getFeatures(OskariLayer layer,
            ReferencedEnvelope bbox, CoordinateReferenceSystem crs, Filter filter) {
        String endPoint = layer.getUrl();
        String typeName = layer.getName();
        String user = layer.getUsername();
        String pass = layer.getPassword();

        // TODO: FIXME!
        int maxFeatures = 10000;
        boolean forceGML = layer.getAttributes().optBoolean(PROPERTY_FORCE_GML, false);

        Map<String, String> query = getQueryParams(typeName, bbox, crs, maxFeatures, filter);
        byte[] response;
        if (!forceGML) {
            // First try GeoJSON
            query.put("OUTPUTFORMAT", "application/json");
            response = OskariWFSClient.getResponse(endPoint, user, pass, query);
            try {
                return OskariWFSClient.parseGeoJSON(new ByteArrayInputStream(response), crs);
            } catch (IOException e) {
                if (!OskariWFSClient.isOutputFormatInvalid(new ByteArrayInputStream(response))) {
                    // If we can not determine that the exception was due to bad
                    // outputFormat parameter then don't bother trying GML
                    final String url = IOHelper.constructUrl(endPoint, query);
                    LOG.debug("Response from", url, "was:\n", new String(response, StandardCharsets.UTF_8));
                    throw new ServiceRuntimeException("Unable to parse GeoJSON from " + url, e);
                }
            }
        }

        // Fallback to GML
        query.remove("OUTPUTFORMAT");
        response = OskariWFSClient.getResponse(endPoint, user, pass, query);

        try {
            return OSKARI_GML.decodeFeatureCollection(new ByteArrayInputStream(response), user, pass);
        } catch (Exception e) {
            final String url = IOHelper.constructUrl(endPoint, query);
            LOG.debug("Response from", url, "was:\n", new String(response, StandardCharsets.UTF_8));
            throw new ServiceRuntimeException("Unable to parse GML from " + url, e);
        }
    }

    public static SimpleFeatureCollection getFeaturesGeoJSON(String endPoint, String user, String pass,
            String typeName, ReferencedEnvelope bbox, CoordinateReferenceSystem crs,
            int maxFeatures, Filter filter) throws IOException {
        Map<String, String> query = getQueryParams(typeName, bbox, crs, maxFeatures, filter);
        query.put("OUTPUTFORMAT", "application/json");
        HttpURLConnection conn = OskariWFSClient.getConnection(endPoint, user, pass, query);
        String contentType = conn.getContentType();
        if (contentType != null && !contentType.contains("json")) {
            throw new ServiceRuntimeException("Unexpected content type " + contentType);
        }
        try (InputStream in = new BufferedInputStream(conn.getInputStream())) {
            return OskariWFSClient.parseGeoJSON(in, crs);
        }
    }

    public static SimpleFeatureCollection getFeaturesGML(String endPoint, String user, String pass,
            String typeName, ReferencedEnvelope bbox, CoordinateReferenceSystem crs,
            int maxFeatures, Filter filter) throws Exception {
        Map<String, String> query = getQueryParams(typeName, bbox, crs, maxFeatures, filter);
        HttpURLConnection conn = OskariWFSClient.getConnection(endPoint, user, pass, query);
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
            parameters.put("BBOX", OskariWFSClient.getBBOX(bbox));
        } else {
            parameters.put("FILTER", getFilter(filter));
        }
        parameters.put("MAXFEATURES", Integer.toString(maxFeatures));
        return parameters;
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
}
