package org.oskari.service.wfs3.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.DefaultFeatureCollection;
import org.opengis.feature.simple.SimpleFeatureType;
import org.oskari.service.wfs.client.OskariWFSHttpUtil;

/**
 * WFS 3.0.0 client for SimpleFeatures
 */
public class OskariWFS3Client {

    private static final String CONTENT_TYPE_GEOJSON = "application/geo+json";

    public SimpleFeatureCollection getFeatures(String endPoint, String user, String pass,
            String collectionId, double[] bbox, Integer limit) throws Exception {
        String path = getItemsPath(endPoint, collectionId);
        Map<String, String> queryParams = getQueryParams(bbox, limit);
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", CONTENT_TYPE_GEOJSON);

        DefaultFeatureCollection features = new DefaultFeatureCollection();

        HttpURLConnection conn = OskariWFSHttpUtil.getConnection(path, user, pass, queryParams, headers);
        validateResponse(conn, CONTENT_TYPE_GEOJSON);
        String next = readFeaturesTo(conn, features);

        while (next != null) {
            conn = OskariWFSHttpUtil.getConnection(next, user, pass, null, headers);
            validateResponse(conn, CONTENT_TYPE_GEOJSON);
            next = readFeaturesTo(conn, features);
        }

        return features;
    }

    private void validateResponse(HttpURLConnection conn, String expectedContentType)
            throws Exception {
        if (conn.getResponseCode() != 200) {
            throw new Exception("Unexpected status code " + conn.getResponseCode());
        }

        if (expectedContentType != null) {
            String contentType = conn.getContentType();
            if (contentType != null && !expectedContentType.equals(contentType)) {
                throw new Exception("Unexpected content type " + contentType);
            }
        }
    }

    private String readFeaturesTo(HttpURLConnection conn, DefaultFeatureCollection features)
            throws IOException {
        SimpleFeatureType sft = features.getSchema();
        try (InputStream in = conn.getInputStream();
                Reader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
                WFS3FeatureCollectionIterator it = new WFS3FeatureCollectionIterator(reader, sft)) {
            while (it.hasNext()) {
                features.add(it.next());
            }
            return it.getHandler().getLinks().stream()
                    .filter(link -> "next".equals(link.getRel()))
                    .findAny()
                    .map(link -> link.getHref())
                    .orElse(null);
        }
    }

    private String getItemsPath(String endPoint, String collectionId) {
        StringBuilder path = new StringBuilder(endPoint);
        while (path.charAt(path.length() - 1) == '/') {
            path.setLength(path.length() - 1);
        }
        path.append("/collections/");
        path.append(collectionId);
        path.append("/items");
        return path.toString();
    }

    protected Map<String, String> getQueryParams(double[] bbox, Integer limit) {
        Map<String, String> parameters = new LinkedHashMap<>();
        if (bbox != null) {
            parameters.put("bbox", Arrays.stream(bbox)
                    .mapToObj(Double::toString)
                    .collect(Collectors.joining(",")));
        }
        if (limit != null) {
            parameters.put("limit", limit.toString());
        }
        return parameters;
    }

}
