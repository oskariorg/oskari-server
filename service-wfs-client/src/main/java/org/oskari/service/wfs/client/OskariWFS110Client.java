package org.oskari.service.wfs.client;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.IOHelper;

/**
 * Client code for WFS 1.1.0 services
 */
public class OskariWFS110Client {

    private static final Logger LOG = LogFactory.getLogger(OskariWFS110Client.class);
    private static final OskariGML OSKARI_GML = new OskariGML();
    private static final int MAX_REDIRECTS = 5;

    private OskariWFS110Client() {}

    /**
     * @return SimpleFeatureCollection containing the parsed Features, or null if all fails
     */
    public static SimpleFeatureCollection tryGetFeatures(String endPoint, String user, String pass,
            String typeName, ReferencedEnvelope bbox, CoordinateReferenceSystem crs, Integer maxFeatures) {
        // First try GeoJSON
        try {
            return getFeaturesGeoJSON(endPoint, user, pass, typeName, bbox, crs, maxFeatures);
        } catch (Exception e) {
            LOG.warn(e, "Failed to read WFS response as GeoJSON");
        }

        // Fallback to GML
        try {
            return getFeaturesGML(endPoint, user, pass, typeName, bbox, crs, maxFeatures);
        } catch (Exception e) {
            LOG.warn(e, "Failed to read WFS response as GML");
        }

        return null;
    }

    public static SimpleFeatureCollection getFeaturesGeoJSON(String endPoint, String user, String pass,
            String typeName, ReferencedEnvelope bbox, CoordinateReferenceSystem crs, Integer maxFeatures) throws Exception {
        Map<String, String> query = getQueryParams(typeName, bbox, crs, maxFeatures);
        query.put("OUTPUTFORMAT", "application/json");
        HttpURLConnection conn = IOHelper.getConnection(endPoint, user, pass, query);
        conn = IOHelper.followRedirect(conn, user, pass, query, MAX_REDIRECTS);
        if (conn.getResponseCode() != 200) {
            throw new Exception("Unexpected status code " + conn.getResponseCode());
        }

        String contentType = conn.getContentType();
        if (contentType != null && !contentType.contains("json")) {
            // TODO: Handle WFS Error responses
            throw new Exception("Unexpected content type " + contentType);
        }

        try (InputStream in = conn.getInputStream();
                Reader utf8Reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
                FeatureIterator<SimpleFeature> it = new FeatureCollectionIterator(utf8Reader)) {
            DefaultFeatureCollection features = new DefaultFeatureCollection(null, null);
            while (it.hasNext()) {
                features.add(it.next());
            }
            return features;
        }
    }

    public static SimpleFeatureCollection getFeaturesGML(String endPoint, String user, String pass, String typeName,
            ReferencedEnvelope bbox, CoordinateReferenceSystem crs, Integer maxFeatures) throws Exception {
        Map<String, String> query = getQueryParams(typeName, bbox, crs, maxFeatures);
        HttpURLConnection conn = IOHelper.getConnection(endPoint, user, pass, query);
        conn = IOHelper.followRedirect(conn, user, pass, query, MAX_REDIRECTS);
        if (conn.getResponseCode() != 200) {
            throw new Exception("Unexpected status code " + conn.getResponseCode());
        }

        try (InputStream in = new BufferedInputStream(conn.getInputStream())) {
            return OSKARI_GML.decodeFeatureCollection(in, user, pass);
        }
    }

    protected static Map<String, String> getQueryParams(String typeName, ReferencedEnvelope bbox,
            CoordinateReferenceSystem crs, Integer maxFeatures) {
        Map<String, String> parameters = new LinkedHashMap<>();
        parameters.put("SERVICE", "WFS");
        parameters.put("VERSION", "1.1.0");
        parameters.put("REQUEST", "GetFeature");
        parameters.put("TYPENAME", typeName);
        parameters.put("SRSNAME", crs.getIdentifiers().iterator().next().toString());
        parameters.put("BBOX", getBBOX(bbox));
        if (maxFeatures != null) {
            parameters.put("MAXFEATURES", maxFeatures.toString());
        }
        return parameters;
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

}
