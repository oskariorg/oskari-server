package org.oskari.service.mvt.wfs;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.geotools.GML;
import org.geotools.GML.Version;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.geojson.feature.FeatureJSON;
import org.opengis.feature.simple.SimpleFeature;

import fi.nls.oskari.cache.ComputeOnceCache;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.IOHelper;

/**
 * WFS 1.1.0 client for SimpleFeatures
 * 
 * Caches the parsed SimpleFeatureCollections
 * 
 * Tries the following outputformats: GeoJSON, GML3 (WFS 1.1.0 version)
 */
public class CachingWFSClient {

    private static final Logger LOG = LogFactory.getLogger(CachingWFSClient.class);
    private static final FeatureJSON FJ = new FeatureJSON();
    private static final int LIMIT = 100; // Cache 100 responses
    private static final ComputeOnceCache<SimpleFeatureCollection> CACHE = new ComputeOnceCache<>(LIMIT);

    public static SimpleFeatureCollection getFeatures(String endPoint, String user, String pass, String typeName, double[] bbox, String srsName, int maxFeatures) {
        final Map<String, String> getFeatureKVP = getFeatureKVP(typeName, bbox, srsName, maxFeatures);
        final String request = IOHelper.constructUrl(endPoint, getFeatureKVP);

        // Use the GET request as the cache key
        final SimpleFeatureCollection sfc = CACHE.get(request, (String k) -> {
            // First try GeoJSON (faster and easier to write and read)
            getFeatureKVP.put("OUTPUTFORMAT", "application/json");
            String geojsonRequest = IOHelper.constructUrl(endPoint, getFeatureKVP);
            SimpleFeatureCollection fc = getFeatureGeoJSON(geojsonRequest, user, pass);
            if (fc == null) {
                return fc;
            }
            return getFeatureGML(request, user, pass);
        });

        return sfc;
    }

    private static Map<String, String> getFeatureKVP(String typeName, double[] bbox, String srsName, int maxFeatures) {
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

    private static String getBBOX(double[] bbox, String srsName) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            sb.append(bbox[i]);
            sb.append(',');
        }
        sb.append(srsName);
        return sb.toString();
    }

    private static SimpleFeatureCollection getFeatureGeoJSON(String getFeatureKVP, String user, String pass) {
        try {
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

            boolean gzip = "gzip".equals(contentType);
            try (InputStream in = gzip ? new GZIPInputStream(conn.getInputStream()) : conn.getInputStream()) {
                DefaultFeatureCollection features = new DefaultFeatureCollection(null, null);
                FeatureIterator<SimpleFeature> it = FJ.streamFeatureCollection(in);
                while (it.hasNext()) {
                    features.add(it.next());
                }
                return features;
            }
        } catch (IOException e) {
            LOG.info(e, "Failed to read as GeoJSON");
            return null;
        }
    }

    private static SimpleFeatureCollection getFeatureGML(String getFeatureKVP, String user, String pass) {
        try {
            HttpURLConnection conn = IOHelper.getConnection(getFeatureKVP, user, pass);
            if (conn.getResponseCode() != 200) {
                LOG.info("Failed to read response as GML, unexpected status code:", conn.getResponseCode());
                return null;
            }

            String contentType = conn.getContentType();
            // TODO: Handle WFS Error responses

            boolean gzip = "gzip".equals(contentType);
            try (InputStream in = gzip ? new GZIPInputStream(conn.getInputStream()) : conn.getInputStream()) {
                GML gml = new GML(Version.WFS1_1);
                return gml.decodeFeatureCollection(in, true);
            }
        } catch (Exception e) {
            LOG.warn(e, "Failed to read as GML");
            return null;
        }
    }

}
