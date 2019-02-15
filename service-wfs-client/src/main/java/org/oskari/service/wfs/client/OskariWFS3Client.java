package org.oskari.service.wfs.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.oskari.service.wfs.client.geojson.WFS3FeatureCollectionIterator;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.util.IOHelper;

/**
 * Client code for WFS 3 Core services
 */
public class OskariWFS3Client {

    private static final Logger LOG = LogFactory.getLogger(OskariWFS3Client.class);
    private static final String CONTENT_TYPE_GEOJSON = "application/geo+json";
    private static final int MAX_REDIRECTS = 5;

    private static CoordinateReferenceSystem CRS84;
    protected static CoordinateReferenceSystem getCRS84() {
        if (CRS84 == null) {
            try {
                // Default CRS for WFS 3 is CRS84 (= WGS84 with lon/lat order)
                final boolean longitudeFirst = true;
                CRS84 = CRS.decode("EPSG:4326", longitudeFirst);
            } catch (Exception e) {
                LOG.error(e, "Failed to decode CRS84");
            }
        }
        return CRS84;
    }

    private OskariWFS3Client() {}

    public static SimpleFeatureCollection tryGetFeatures(String endPoint, String user, String pass,
            String collectionId, ReferencedEnvelope bbox, CoordinateReferenceSystem crs, Integer limit) {
        try {
            return getFeatures(endPoint, user, pass, collectionId, bbox, crs, limit);
        } catch (Exception e) {
            LOG.warn(e, "Failed to get features");
            return null;
        }
    }

    public static SimpleFeatureCollection getFeatures(String endPoint,
            String user, String pass,
            String collectionId, ReferencedEnvelope bbox,
            CoordinateReferenceSystem crs, Integer limit) throws ServiceException, IOException {
        String path = getItemsPath(endPoint, collectionId);
        Map<String, String> query = getQueryParams(bbox, limit);
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", CONTENT_TYPE_GEOJSON);

        DefaultFeatureCollection features = new DefaultFeatureCollection();

        HttpURLConnection conn = IOHelper.getConnection(path, user, pass, query, headers);
        conn = IOHelper.followRedirect(conn, user, pass, query, headers, MAX_REDIRECTS);
        validateResponse(conn, CONTENT_TYPE_GEOJSON);
        String next = readFeaturesTo(conn, features);

        // Check if there's a link with rel="next"
        // => While the next link exists there's a next page to be read
        while (next != null) {
            // Blindly follow the next link, don't use the initial queryParameters
            conn = IOHelper.getConnection(next, user, pass, null, headers);
            conn = IOHelper.followRedirect(conn, user, pass, null, headers, MAX_REDIRECTS);
            validateResponse(conn, CONTENT_TYPE_GEOJSON);
            next = readFeaturesTo(conn, features);
        }

        // Features are in CRS84
        // Check if we need to transform them to the requested CRS
        if (CRS.equalsIgnoreMetadata(getCRS84(), crs)) {
            return features;
        }

        try {
            CoordinateTransformer coordTransformer = new CoordinateTransformer(getCRS84(), crs);
            return coordTransformer.transform(features);
        } catch (Exception e) {
            throw new ServiceException("Failed to transform features");
        }
    }

    private static void validateResponse(HttpURLConnection conn, String expectedContentType)
            throws ServiceException, IOException {
        if (conn.getResponseCode() != 200) {
            throw new ServiceException("Unexpected status code " + conn.getResponseCode());
        }

        if (expectedContentType != null) {
            String contentType = conn.getContentType();
            if (contentType != null && !expectedContentType.equals(contentType)) {
                throw new ServiceException("Unexpected content type " + contentType);
            }
        }
    }

    private static String readFeaturesTo(HttpURLConnection conn, DefaultFeatureCollection fc)
            throws IOException {
        try (InputStream in = conn.getInputStream();
                Reader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
                WFS3FeatureCollectionIterator it = new WFS3FeatureCollectionIterator(reader)) {
            while (it.hasNext()) {
                fc.add(it.next());
            }
            return it.getHandler().getLinks().stream()
                    .filter(link -> "next".equals(link.getRel()))
                    .findAny()
                    .map(link -> link.getHref())
                    .orElse(null);
        }
    }

    private static String getItemsPath(String endPoint, String collectionId) {
        StringBuilder path = new StringBuilder(endPoint);
        // Remove (all) trailing / characters
        while (path.charAt(path.length() - 1) == '/') {
            path.setLength(path.length() - 1);
        }
        path.append("/collections/");
        path.append(collectionId);
        path.append("/items");
        return path.toString();
    }

    protected static Map<String, String> getQueryParams(ReferencedEnvelope bbox, Integer limit)
            throws ServiceException {
        // Linked not needed, but look better when logging the requests
        Map<String, String> parameters = new LinkedHashMap<>();
        if (bbox != null) {
            // All WFS3 services must support CRS84 for 'bbox'
            // But not all of them will support the CRS extension
            // => Best we request in CRS84 and do the transformation ourselves
            if (!CRS.equalsIgnoreMetadata(bbox.getCoordinateReferenceSystem(), getCRS84())) {
                try {
                    bbox = bbox.transform(getCRS84(), true, 5);
                } catch (Exception e) {
                    throw new ServiceException("Failed to transform bbox to CRS84");
                }
            }
            String bboxStr = String.format(Locale.US, "%f,%f,%f,%f",
                    bbox.getMinX(), bbox.getMinY(),
                    bbox.getMaxX(), bbox.getMaxY());
            parameters.put("bbox", bboxStr);
        }
        if (limit != null) {
            parameters.put("limit", limit.toString());
        }
        return parameters;
    }

}
