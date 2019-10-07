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
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.oskari.geojson.GeoJSONReader2;
import org.oskari.geojson.GeoJSONSchemaDetector;
import org.oskari.service.wfs3.model.WFS3Link;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.ServiceRuntimeException;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.JSONHelper;

/**
 * Client code for WFS 3 Core services
 */
public class OskariWFS3Client {

    private static final Logger LOG = LogFactory.getLogger(OskariWFS3Client.class);

    protected static final int PAGE_SIZE = 1000;

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
    
    public static SimpleFeatureCollection getFeatures(OskariLayer layer,
            ReferencedEnvelope bbox, CoordinateReferenceSystem crs) throws ServiceRuntimeException {
        /* Servers can not yet implement this since no requirement URI is standardized yet
        boolean crsExtension;
        try {
            crsExtension = checkCRSExtension(endPoint, user, pass);
        } catch (IOException e) {
            throw new ServiceRuntimeException("IOException occured", e);
        }
         */
        
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

        String bboxCrsURI = null;
        if (bbox != null) {
            bboxCrsURI = getCrsURI(layer, bbox.getCoordinateReferenceSystem());
            if (bboxCrsURI == null) {
                try {
                    // Server doesn't support bboxes CRS - transform bbox to CRS84
                    bbox = transformEnvelope(bbox, getCRS84());
                } catch (Exception e) {
                    throw new ServiceRuntimeException("bbox coordinate transformation failure", e);
                }
            }
        }

        String endPoint = layer.getUrl();
        String collectionId = layer.getName();
        String user = layer.getUsername();
        String pass = layer.getPassword();
        // TODO: FIXME!
        int hardLimit = 10000;

        String path = getItemsPath(endPoint, collectionId);
        Map<String, String> query = getQueryParams(crsURI, bbox, bboxCrsURI, hardLimit);
        Map<String, String> headers = Collections.singletonMap("Accept", CONTENT_TYPE_GEOJSON);

        List<SimpleFeatureCollection> pages = new ArrayList<>();
        int numFeatures = 0;

        SimpleFeatureType schema;
        try {
            HttpURLConnection conn = IOHelper.getConnection(path, user, pass, query, headers);
            conn = IOHelper.followRedirect(conn, user, pass, query, headers, MAX_REDIRECTS);

            validateResponse(conn, CONTENT_TYPE_GEOJSON);
            Map<String, Object> geojson = readMap(conn);
            boolean ignoreGeometryProperties = true;
            schema = GeoJSONSchemaDetector.getSchema(geojson, crs, ignoreGeometryProperties);
            SimpleFeatureCollection sfc = GeoJSONReader2.toFeatureCollection(geojson, schema, transformCRS84ToTargetCRS);
            numFeatures += sfc.size();
            pages.add(sfc);
            String next = getLinkHref(geojson, "next");

            while (next != null && numFeatures < hardLimit) {
                // Blindly follow the next link, don't use the initial queryParameters
                conn = IOHelper.getConnection(next, user, pass, null, headers);
                conn = IOHelper.followRedirect(conn, user, pass, null, headers, MAX_REDIRECTS);

                validateResponse(conn, CONTENT_TYPE_GEOJSON);
                geojson = readMap(conn);
                sfc = GeoJSONReader2.toFeatureCollection(geojson, schema, transformCRS84ToTargetCRS);
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

    private static ReferencedEnvelope transformEnvelope(ReferencedEnvelope env, CoordinateReferenceSystem to)
            throws TransformException, FactoryException {
        if (CRS.equalsIgnoreMetadata(env.getCoordinateReferenceSystem(), to)) {
            return env;
        }
        return env.transform(to, true);
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
        List<String> crsURIs = JSONHelper.getArrayAsList(capabilities.optJSONArray("crs-uri"));
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

    protected static Map<String, String> getQueryParams(String crsURI, ReferencedEnvelope bbox, String bboxCrsURI, int hardLimit)
            throws ServiceRuntimeException {
        // Linked not needed, but looks better when logging the requests
        Map<String, String> parameters = new LinkedHashMap<>();
        if (crsURI != null) {
            parameters.put("crs", crsURI);
        }
        if (bbox != null) {
            String bboxStr = String.format(Locale.US, "%f,%f,%f,%f",
                    bbox.getMinX(), bbox.getMinY(),
                    bbox.getMaxX(), bbox.getMaxY());
            parameters.put("bbox", bboxStr);
            if (bboxCrsURI != null) {
                parameters.put("bbox-crs", bboxCrsURI);
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
