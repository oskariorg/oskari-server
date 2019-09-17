package org.oskari.service.wfs3;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.oskari.geojson.GeoJSONReader2;
import org.oskari.geojson.GeoJSONSchemaDetector;
import org.oskari.service.wfs3.model.WFS3Link;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.ServiceRuntimeException;
import fi.nls.oskari.util.IOHelper;

/**
 * Client code for WFS 3 Core services
 */
public class OskariWFS3Client {

    private static final Logger LOG = LogFactory.getLogger(OskariWFS3Client.class);

    // Don't have one yet
    private static final String CONFORMANCE_CRS = "TBD";

    protected static final int PAGE_SIZE = 1000;

    private static final String CONTENT_TYPE_JSON = "application/json";
    private static final String CONTENT_TYPE_GEOJSON = "application/geo+json";
    private static final int MAX_REDIRECTS = 5;
    private static final ObjectMapper OM = new ObjectMapper();
    private static final TypeReference<HashMap<String, Object>> TYPE_REF = new TypeReference<HashMap<String, Object>>() {};


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

    public static SimpleFeatureCollection getFeatures(String endPoint,
            String user, String pass, String collectionId,
            ReferencedEnvelope bbox, CoordinateReferenceSystem crs, int hardLimit) throws ServiceRuntimeException {
        /* Servers can not yet implement this since no requirement URI is standardized yet
        boolean crsExtension;
        try {
            crsExtension = checkCRSExtension(endPoint, user, pass);
        } catch (IOException e) {
            throw new ServiceRuntimeException("IOException occured", e);
        }
         */

        String crsURI = getCrsURI(endPoint, user, pass, collectionId, crs);

        MathTransform transform = null;
        if (crsURI == null) {
            try {
                transform = CRS.equalsIgnoreMetadata(getCRS84(), crs) ? null : CRS.findMathTransform(getCRS84(), crs);
            } catch (Exception e) {
                throw new ServiceRuntimeException("Coordinate transformation failure", e);
            }
        }

        String path = getItemsPath(endPoint, collectionId);
        CoordinateReferenceSystem bboxCRS = crsURI != null ? safeCRSDecode(crsURI) : getCRS84();
        Map<String, String> query = getQueryParams(bbox, crsURI, bboxCRS, hardLimit);
        Map<String, String> headers = Collections.singletonMap("Accept", CONTENT_TYPE_GEOJSON);

        List<SimpleFeatureCollection> pages = new ArrayList<>();
        int numFeatures = 0;

        SimpleFeatureType schema;
        try {
            HttpURLConnection conn = IOHelper.getConnection(path, user, pass, query, headers);
            conn = IOHelper.followRedirect(conn, user, pass, query, headers, MAX_REDIRECTS);

            validateResponse(conn, CONTENT_TYPE_GEOJSON);
            Map<String, Object> geojson = readMap(conn);
            schema = GeoJSONSchemaDetector.getSchema(geojson, crs);
            SimpleFeatureCollection sfc = GeoJSONReader2.toFeatureCollection(geojson, schema, transform);
            numFeatures += sfc.size();
            pages.add(sfc);
            String next = getLinkHref(geojson, "next");

            while (next != null && numFeatures < hardLimit) {
                // Blindly follow the next link, don't use the initial queryParameters
                conn = IOHelper.getConnection(next, user, pass, null, headers);
                conn = IOHelper.followRedirect(conn, user, pass, null, headers, MAX_REDIRECTS);

                validateResponse(conn, CONTENT_TYPE_GEOJSON);
                geojson = readMap(conn);
                sfc = GeoJSONReader2.toFeatureCollection(geojson, schema, transform);
                numFeatures += sfc.size();
                pages.add(sfc);
                next = getLinkHref(geojson, "next");
            }
        } catch (IOException e) {
            throw new ServiceRuntimeException("IOException occured", e);
        } catch (MismatchedDimensionException | TransformException e) {
            throw new ServiceRuntimeException("Projection transformation failed", e);
        }

        if (pages.size() == 1) {
            return pages.get(0);
        }
        return new PaginatedFeatureCollection(pages, schema, "FeatureCollection", hardLimit);
    }

    /**
     * @return URI matching the requested crs if the collection supports it,
     *          null otherwise
     */
    private static String getCrsURI(String endPoint, String user, String pass,
            String collectionId, CoordinateReferenceSystem crs) {
        Map<String, Object> collectionJSON;
        try {
            String collectionPath = getCollectionsPath(endPoint, collectionId);
            collectionJSON = getJSON(collectionPath, user, pass, null);
        } catch (IOException e) {
            throw new ServiceRuntimeException("Failed to get collection metadata");
        }
        if (!collectionJSON.containsKey("crs")) {
            return null;
        }
        Collection<String> supportedCRS = (Collection<String>) collectionJSON.get("crs");
        for (String crsURI : supportedCRS) {
            CoordinateReferenceSystem tmp = safeCRSDecode(crsURI);
            if (tmp == null) {
                continue;
            }
            if (CRS.equalsIgnoreMetadata(tmp, crs)) {
                return crsURI;
            }
        }
        return null;
    }

    private static boolean checkCRSExtension(String endPoint, String user, String pass) throws IOException {
        String conformancePath = getConformancePath(endPoint, user, pass);
        Map<String, Object> conformanceJSON = getJSON(conformancePath, user, pass, null);
        Collection<String> conformsTo = (Collection<String>) conformanceJSON.get("conformsTo");
        return conformsTo.contains(CONFORMANCE_CRS);
    }

    private static String getConformancePath(String endPoint, String user, String pass) throws ServiceRuntimeException, IOException {
        String landingPagePath = endPoint;
        Map<String, Object> json = getJSON(landingPagePath, user, pass, null);
        return getLinkHref(json, "conformance");
    }

    private static Map<String, Object> getJSON(String path, String user, String pass, Map<String, String> queryParams) throws IOException {
        Map<String, String> headers = Collections.singletonMap("Accept", CONTENT_TYPE_JSON);
        HttpURLConnection conn = IOHelper.getConnection(path, user, pass, null, headers);
        conn = IOHelper.followRedirect(conn, user, pass, null, headers, MAX_REDIRECTS);
        validateResponse(conn, CONTENT_TYPE_JSON);
        return readMap(conn);
    }

    @SuppressWarnings("unchecked")
    private static String getLinkHref(Map<String, Object> json, String rel) {
        // Check if there's a link with rel="next"
        Object _links = json.get("links");
        if (_links != null && _links instanceof List) {
            return toLinks((List<Object>) _links).stream()
                    .filter(link -> rel.equals(link.getRel()))
                    .findAny()
                    .map(WFS3Link::getHref)
                    .orElse(null);
        }
        return null;
    }

    private static Map<String, Object> readMap(HttpURLConnection conn) throws IOException {
        try (InputStream in = conn.getInputStream()) {
            return OM.readValue(in, TYPE_REF);
        }
    }

    private static void validateResponse(HttpURLConnection conn, String expectedContentType)
            throws ServiceRuntimeException, IOException {
        if (conn.getResponseCode() != 200) {
            throw new ServiceRuntimeException("Unexpected status code " + conn.getResponseCode());
        }

        if (expectedContentType != null) {
            String contentType = conn.getContentType();
            if (contentType != null && !expectedContentType.equals(contentType)) {
                throw new ServiceRuntimeException("Unexpected content type " + contentType);
            }
        }
    }

    private static String getCollectionsPath(String endPoint, String collectionId) {
        StringBuilder path = new StringBuilder(endPoint);
        // Remove (all) trailing / characters
        while (path.charAt(path.length() - 1) == '/') {
            path.setLength(path.length() - 1);
        }
        path.append("/collections/");
        path.append(collectionId);
        return path.toString();
    }

    private static String getItemsPath(String endPoint, String collectionId) {
        return getCollectionsPath(endPoint, collectionId) + "/items";
    }

    protected static Map<String, String> getQueryParams(ReferencedEnvelope bbox, String crsURI, CoordinateReferenceSystem crs, int hardLimit)
            throws ServiceRuntimeException {
        // Linked not needed, but look better when logging the requests
        Map<String, String> parameters = new LinkedHashMap<>();
        if (bbox != null) {
            if (!CRS.equalsIgnoreMetadata(bbox.getCoordinateReferenceSystem(), crs)) {
                try {
                    bbox = bbox.transform(crs, true, 5);
                } catch (Exception e) {
                    throw new ServiceRuntimeException("Failed to transform bbox to CRS84");
                }
            }
            String bboxStr = String.format(Locale.US, "%f,%f,%f,%f",
                    bbox.getMinX(), bbox.getMinY(),
                    bbox.getMaxX(), bbox.getMaxY());
            parameters.put("bbox", bboxStr);
            if (crsURI != null) {
                parameters.put("crs", crsURI);
                parameters.put("bbox-crs", crsURI);
            }
        }
        parameters.put("limit", Integer.toString(Math.min(PAGE_SIZE, hardLimit)));
        return parameters;
    }

    private static CoordinateReferenceSystem safeCRSDecode(String code) {
        try {
            return CRS.decode(code);
        } catch (Exception ignore) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    protected static List<WFS3Link> toLinks(List<Object> arrayOflinks) {
        return arrayOflinks.stream()
                .map(obj -> (Map<String, String>) obj)
                .map(OskariWFS3Client::toLink)
                .collect(Collectors.toList());
    }

    protected static WFS3Link toLink(Map<String, String> map) {
        String href = map.get("href");
        String rel = map.get("rel");
        String type = map.get("type");
        String hreflang = map.get("hreflang");
        String title = map.get("title");
        return new WFS3Link(href, rel, type, hreflang, title);
    }


}
