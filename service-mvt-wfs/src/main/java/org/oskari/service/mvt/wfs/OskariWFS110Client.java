package org.oskari.service.mvt.wfs;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.geotools.GML;
import org.geotools.GML.Version;
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

    /**
     * @return SimpleFeatureCollection containing the parsed Features, or null if we fail
     */
    public SimpleFeatureCollection tryGetFeatures(String endPoint, String user, String pass, String typeName, double[] bbox, String srsName, int maxFeatures) {
        // First try GeoJSON
        try {
            SimpleFeatureCollection fc = getFeaturesGeoJSON(endPoint, user, pass, typeName, bbox, srsName, maxFeatures);
            if (fc != null) {
                return fc;
            }
        } catch (IOException e) {
            LOG.warn(e, "Failed to read WFS response as GeoJSON");
        }

        try {
            SimpleFeatureCollection fc = getFeaturesGML(endPoint, user, pass, typeName, bbox, srsName, maxFeatures);
            if (fc != null) {
                return fc;
            }
        } catch (Exception e) {
            LOG.warn(e, "Failed to read WFS response as GML");
        }

        return null;
    }

    public SimpleFeatureCollection getFeaturesGeoJSON(String endPoint, String user, String pass,
            String typeName, double[] bbox, String srsName, int maxFeatures) throws IOException {
        Map<String, String> queryParams = getQueryParams(typeName, bbox, srsName, maxFeatures);
        queryParams.put("OUTPUTFORMAT", "application/json");
        String getFeatureKVP = IOHelper.constructUrl(endPoint, queryParams);
        HttpURLConnection conn = IOHelper.getConnection(getFeatureKVP, user, pass);
        if (conn.getResponseCode() != 200) {
            LOG.info("Failed to read response as GeoJSON, unexpected status code:", conn.getResponseCode());
            return null;
        }

        String contentType = conn.getContentType();
        if (contentType != null && !contentType.contains("json")) {
            // TODO: Handle WFS Error responses
            LOG.info("Failed to read response as GeoJSON, unexpected content type:", conn.getContentType());
            return null;
        }

        try (InputStream in = conn.getInputStream();
                FeatureIterator<SimpleFeature> it = FJ.streamFeatureCollection(in)) {
            DefaultFeatureCollection features = new DefaultFeatureCollection(null, null);
            while (it.hasNext()) {
                features.add(it.next());
            }
            return features;
        }
    }

    public SimpleFeatureCollection getFeaturesGML(String endPoint, String user, String pass,
            String typeName, double[] bbox, String srsName, int maxFeatures) throws Exception {
        Map<String, String> queryParams = getQueryParams(typeName, bbox, srsName, maxFeatures);
        String getFeatureKVP = IOHelper.constructUrl(endPoint, queryParams);
        HttpURLConnection conn = IOHelper.getConnection(getFeatureKVP, user, pass);
        if (conn.getResponseCode() != 200) {
            LOG.info("Failed to read response as GML, unexpected status code:", conn.getResponseCode());
            return null;
        }

        String contentEncoding = conn.getContentEncoding();
        // TODO: Handle WFS Error responses

        try (InputStream in = conn.getInputStream()) {
            GML gml = new GML(Version.WFS1_1);
            return gml.decodeFeatureCollection(in, true);
        }
    }

    protected Map<String, String> getQueryParams(String typeName, double[] bbox, String srsName, int maxFeatures) {
        Map<String, String> parameters = new LinkedHashMap<>();
        parameters.put("SERVICE", "WFS");
        parameters.put("VERSION", "1.1.0");
        parameters.put("REQUEST", "GetFeature");
        parameters.put("TYPENAME", typeName);
        parameters.put("BBOX", getBBOX(bbox, srsName));
        parameters.put("SRSNAME", srsName);
        parameters.put("MAXFEATURES", Integer.toString(maxFeatures));
        return parameters;
    }

    private String getBBOX(double[] bbox, String srsName) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            sb.append(bbox[i]);
            sb.append(',');
        }
        sb.append(srsName);
        return sb.toString();
    }

}
