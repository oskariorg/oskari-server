package org.oskari.service.wfs.client;

import java.io.*;
import java.net.HttpURLConnection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.geojson.feature.FeatureJSON;
import org.opengis.feature.simple.SimpleFeature;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.IOHelper;

/**
 * WFS 1.1.0 client for SimpleFeatures
 */
public class OskariWFS110Client {

    private static final Logger LOG = LogFactory.getLogger(OskariWFS110Client.class);
    private static final FeatureJSON FJ = new FeatureJSON();
    private static final OskariGML OSKARI_GML = new OskariGML();
    private static final int MAX_REDIRECTS = 5;

    /**
     * @return SimpleFeatureCollection containing the parsed Features, or null if all fails
     */
    public SimpleFeatureCollection tryGetFeatures(String endPoint, String user, String pass,
            String typeName, double[] bbox, String srsName, Integer maxFeatures) {
        // First try GeoJSON
        try {
            return getFeaturesGeoJSON(endPoint, user, pass, typeName, bbox, srsName, maxFeatures);
        } catch (Exception e) {
            LOG.warn(e, "Failed to read WFS response as GeoJSON");
        }

        try {
            return getFeaturesGML(endPoint, user, pass, typeName, bbox, srsName, maxFeatures);
        } catch (Exception e) {
            LOG.warn(e, "Failed to read WFS response as GML");
        }

        return null;
    }

    public SimpleFeatureCollection getFeaturesGeoJSON(String endPoint, String user, String pass,
            String typeName, double[] bbox, String srsName, Integer maxFeatures) throws Exception {
        Map<String, String> queryParams = getQueryParams(typeName, bbox, srsName, maxFeatures);
        queryParams.put("OUTPUTFORMAT", "application/json");
        HttpURLConnection conn = getConnection(endPoint, user, pass, queryParams);
        if (conn.getResponseCode() != 200) {
            throw new Exception("Unexpected status code " + conn.getResponseCode());
        }

        String contentType = conn.getContentType();
        if (contentType != null && !contentType.contains("json")) {
            // TODO: Handle WFS Error responses
            throw new Exception("Unexpected content type " + contentType);
        }

        try (InputStream in = conn.getInputStream();
             Reader utf8Reader = new InputStreamReader(in, IOHelper.CHARSET_UTF8);
             FeatureIterator<SimpleFeature> it = FJ.streamFeatureCollection(utf8Reader)) {

            DefaultFeatureCollection features = new DefaultFeatureCollection(null, null);
            while (it.hasNext()) {
                features.add(it.next());
            }
            return features;
        }
    }

    public SimpleFeatureCollection getFeaturesGML(String endPoint, String user, String pass, String typeName,
            double[] bbox, String srsName, Integer maxFeatures) throws Exception {
        Map<String, String> queryParams = getQueryParams(typeName, bbox, srsName, maxFeatures);
        HttpURLConnection conn = getConnection(endPoint, user, pass, queryParams);
        if (conn.getResponseCode() != 200) {
            throw new Exception("Unexpected status code " + conn.getResponseCode());
        }

        try (InputStream in = new BufferedInputStream(conn.getInputStream())) {
            return OSKARI_GML.decodeFeatureCollection(in, user, pass);
        }
    }

    protected Map<String, String> getQueryParams(String typeName, double[] bbox,
            String srsName, Integer maxFeatures) {
        Map<String, String> parameters = new LinkedHashMap<>();
        parameters.put("SERVICE", "WFS");
        parameters.put("VERSION", "1.1.0");
        parameters.put("REQUEST", "GetFeature");
        parameters.put("TYPENAME", typeName);
        parameters.put("SRSNAME", srsName);
        if (bbox != null) {
            parameters.put("BBOX", getBBOX(bbox, srsName));
        }
        if (maxFeatures != null) {
            parameters.put("MAXFEATURES", maxFeatures.toString());
        }
        return parameters;
    }

    protected String getBBOX(double[] bbox, String srsName) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            sb.append(bbox[i]);
            sb.append(',');
        }
        sb.append(srsName);
        return sb.toString();
    }

    public static HttpURLConnection getConnection(String endPoint,
            String user, String pass, Map<String, String> queryParams) throws IOException {
        String request = IOHelper.constructUrl(endPoint, queryParams);
        HttpURLConnection conn = IOHelper.getConnection(request, user, pass);
        return followRedirect(conn, user, pass, queryParams, MAX_REDIRECTS);
    }

    private static HttpURLConnection followRedirect(HttpURLConnection conn,
            String user, String pass, Map<String, String> queryParams, int redirectLatch) throws IOException {
        final int sc = conn.getResponseCode();
        if (sc == HttpURLConnection.HTTP_MOVED_PERM
                || sc == HttpURLConnection.HTTP_MOVED_TEMP
                || sc == HttpURLConnection.HTTP_SEE_OTHER) {
            if (--redirectLatch == 0) {
                throw new IOException("Too many redirects!");
            }
            String location = conn.getHeaderField("Location");
            int i = location.indexOf('?');
            i = i < 0 ? location.length() : i;
            String newEndPoint = location.substring(0, i);
            LOG.info("Following redirect to", newEndPoint);
            String newRequest = IOHelper.constructUrl(newEndPoint, queryParams);
            HttpURLConnection newConnection = IOHelper.getConnection(newRequest, user, pass);
            return followRedirect(newConnection, user, pass, queryParams, redirectLatch);
        } else {
            return conn;
        }
    }

}
