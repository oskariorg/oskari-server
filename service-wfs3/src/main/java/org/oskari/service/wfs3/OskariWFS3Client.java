package org.oskari.service.wfs3;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
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
import org.json.JSONObject;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.oskari.geojson.GeoJSONReader2;
import org.oskari.geojson.GeoJSONSchemaDetector;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.ServiceRuntimeException;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.JSONHelper;
import org.oskari.ogcapi.OpenAPILink;

/**
 * Client code for WFS 3 Core services
 */
public class OskariWFS3Client {

    private static final Logger LOG = LogFactory.getLogger(OskariWFS3Client.class);

    protected static final String ATTRIBUTE_PAGE_SIZE = "pageSize";
    protected static final String ATTRIBUTE_HARD_LIMIT = "hardLimit";

    private static final int DEFAULT_PAGE_SIZE = 1000;
    private static final int DEFAULT_HARD_LIMIT = 10_000;
    private static final int MIN_PAGE_SIZE = 10;
    private static final int MIN_HARD_LIMIT = 10;
    private static final int MAX_PAGE_SIZE = 10_000;
    private static final int MAX_HARD_LIMIT = 100_000;

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

    /**
     * @deprecated @see #getFeatures(OskariLayer, ReferencedEnvelope, CoordinateReferenceSystem, Filter)
     */
    @Deprecated
    public static SimpleFeatureCollection getFeatures(OskariLayer layer,
            ReferencedEnvelope bbox, CoordinateReferenceSystem crs) throws ServiceRuntimeException {
        return getFeatures(layer, bbox, crs, null);
    }

    public static SimpleFeatureCollection getFeatures(OskariLayer layer,
            ReferencedEnvelope bbox, CoordinateReferenceSystem crs, Filter filter) throws ServiceRuntimeException {
        String crsURI = getCrsURI(layer, crs);
        MathTransform transformCRS84ToTargetCRS = null;
        if (crsURI == null) {
            try {
                // Service doesn't support outputting the collection in the targetCRS
                transformCRS84ToTargetCRS = CRS.equalsIgnoreMetadata(getCRS84(), crs) ? null : CRS.findMathTransform(getCRS84(), crs);
            } catch (Exception e) {
                throw new ServiceRuntimeException("Coordinate transformation failure", e);
            }
        }

        String path = getItemsPath(layer.getUrl(), layer.getName());
        String user = layer.getUsername();
        String pass = layer.getPassword();

        int pageSize = DEFAULT_PAGE_SIZE;
        int hardLimit = DEFAULT_HARD_LIMIT;
        JSONObject attr = layer.getAttributes();
        if (attr != null) {
            pageSize = clamp(attr.optInt(ATTRIBUTE_PAGE_SIZE, pageSize), MIN_PAGE_SIZE, MAX_PAGE_SIZE);
            hardLimit = clamp(attr.optInt(ATTRIBUTE_HARD_LIMIT, hardLimit), MIN_HARD_LIMIT, MAX_HARD_LIMIT);
        }

        Map<String, String> query = getQueryParams(crsURI, pageSize);
        // attach any extra params added for layer (for example properties=[prop name we are interested in])
        query.putAll(JSONHelper.getObjectAsMap(layer.getParams()));

        Filter postFilter = Filter.INCLUDE;
        if (filter != null) {
            postFilter = new FilterToOAPIFCoreQuery(layer).toQueryParameters(filter, query);
        } else if (bbox != null) {
            addBboxToQuery(layer, bbox, query);
        }

        Map<String, String> headers = Collections.singletonMap("Accept", CONTENT_TYPE_GEOJSON);

        try {
            List<SimpleFeatureCollection> pages = new ArrayList<>();
            int numFeatures = 0;

            HttpURLConnection conn = IOHelper.getConnection(path, user, pass, query, headers);
            conn = IOHelper.followRedirect(conn, user, pass, query, headers, MAX_REDIRECTS);

            validateResponse(conn, CONTENT_TYPE_GEOJSON);
            Map<String, Object> geojson = readMap(conn);
            boolean ignoreGeometryProperties = true;
            SimpleFeatureType schema = GeoJSONSchemaDetector.getSchema(geojson, crs, ignoreGeometryProperties);
            SimpleFeatureCollection sfc = GeoJSONReader2.toFeatureCollection(geojson, schema, transformCRS84ToTargetCRS, postFilter);
            numFeatures += sfc.size();
            pages.add(sfc);
            String next = getLinkHref(geojson, "next");

            while (next != null && numFeatures < hardLimit) {
                // Blindly follow the next link, don't use the initial queryParameters
                conn = IOHelper.getConnection(next, user, pass, null, headers);
                conn = IOHelper.followRedirect(conn, user, pass, null, headers, MAX_REDIRECTS);

                validateResponse(conn, CONTENT_TYPE_GEOJSON);
                geojson = readMap(conn);
                sfc = GeoJSONReader2.toFeatureCollection(geojson, schema, transformCRS84ToTargetCRS, postFilter);
                numFeatures += sfc.size();
                pages.add(sfc);
                next = getLinkHref(geojson, "next");
            }

            if (pages.size() == 1) {
                return pages.get(0);
            }
            return new PaginatedFeatureCollection(pages, schema, "FeatureCollection", hardLimit);
        } catch (IOException e) {
            throw new ServiceRuntimeException("IOException occured", e);
        } catch (MismatchedDimensionException | TransformException e) {
            throw new ServiceRuntimeException("Projection transformation failed", e);
        }
    }

    private static int clamp(int value, int min, int max) {
        if (value > max) {
            return max;
        } else if (value < min) {
            return min;
        }
        return value;
    }

    static void addBboxToQuery(OskariLayer layer, ReferencedEnvelope bbox, Map<String, String> query) {
        String bboxCrsURI = getCrsURI(layer, bbox.getCoordinateReferenceSystem());
        if (bboxCrsURI != null) {
            query.put("bbox-crs", bboxCrsURI);
        } else {
            // Server doesn't support bbox's CRS - transform bbox to CRS84
            bbox = transformEnvelope(bbox, getCRS84());
        }
        query.put("bbox", String.format(Locale.US, "%f,%f,%f,%f",
                bbox.getMinX(), bbox.getMinY(),
                bbox.getMaxX(), bbox.getMaxY()));
    }

    private static ReferencedEnvelope transformEnvelope(ReferencedEnvelope env, CoordinateReferenceSystem to) {
        if (CRS.equalsIgnoreMetadata(env.getCoordinateReferenceSystem(), to)) {
            return env;
        }
        try {
            return env.transform(to, true);
        } catch (Exception e) {
            throw new ServiceRuntimeException("bbox coordinate transformation failure", e);
        }
    }

    /**
     * @return URI matching the requested crs if the layer supports it,
     *          null otherwise
     */
    private static String getCrsURI(OskariLayer layer, CoordinateReferenceSystem crs) {
        JSONObject capabilities = layer.getCapabilities();
        if (capabilities == null) {
            return null;
        }
        // support for new structure
        JSONObject typeSpecific = capabilities.optJSONObject("typeSpecific");
        List<String> crsURIs;
        if (typeSpecific != null) {
            crsURIs = JSONHelper.getArrayAsList(typeSpecific.optJSONArray("crs-uri"));;
        } else {
            crsURIs = JSONHelper.getArrayAsList(capabilities.optJSONArray("crs-uri"));
        }
        for (String crsURI : crsURIs) {
            CoordinateReferenceSystem tmp = safeCRSDecode(crsURI);
            if (tmp != null && CRS.equalsIgnoreMetadata(tmp, crs)) {
                return crsURI;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static String getLinkHref(Map<String, Object> json, String rel) {
        // Check if there's a link with rel="next"
        Object _links = json.get("links");
        if (_links != null && _links instanceof List) {
            return toLinks((List<Object>) _links).stream()
                    .filter(link -> rel.equals(link.getRel()))
                    .findAny()
                    .map(OpenAPILink::getHref)
                    .orElse(null);
        }
        return null;
    }

    private static Map<String, Object> readMap(HttpURLConnection conn) throws IOException {
        try (InputStream in = conn.getInputStream()) {
            return OM.readValue(in, TYPE_REF);
        }
    }

    public static void validateResponse(HttpURLConnection conn, String expectedContentType)
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

    public static String getItemsPath(String endPoint, String collectionId) {
        return getCollectionsPath(endPoint, collectionId) + "/items";
    }

    protected static Map<String, String> getQueryParams(String crsURI, int limit)
            throws ServiceRuntimeException {
        // Linked not needed, but looks better when logging the requests
        Map<String, String> parameters = new LinkedHashMap<>();
        if (crsURI != null) {
            parameters.put("crs", crsURI);
        }
        parameters.put("limit", Integer.toString(limit));
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
    protected static List<OpenAPILink> toLinks(List<Object> arrayOflinks) {
        return arrayOflinks.stream()
                .map(obj -> (Map<String, String>) obj)
                .map(OskariWFS3Client::toLink)
                .collect(Collectors.toList());
    }

    protected static OpenAPILink toLink(Map<String, String> map) {
        String href = map.get("href");
        String rel = map.get("rel");
        String type = map.get("type");
        String hreflang = map.get("hreflang");
        String title = map.get("title");
        return new OpenAPILink(href, rel, type, hreflang, title);
    }


}
