package org.oskari.capabilities.ogc.api;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.*;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import fi.nls.oskari.util.IOHelper;
import org.oskari.capabilities.CapabilitiesService;
import org.oskari.ogcapi.OGCAPIConformanceClass;
import org.oskari.ogcapi.OGCAPIException;
import org.oskari.ogcapi.OGCAPIReqClasses;
import org.oskari.ogcapi.features.*;

public class OGCAPIFeaturesService {
    private static final ObjectMapper OM;
    static {
        OM = new ObjectMapper();
        OM.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @JsonProperty(value="reqClasses")
    private final OGCAPIReqClasses reqClasses;
    @JsonProperty(value="content")
    private final FeaturesContent content;

    @JsonCreator
    public OGCAPIFeaturesService(@JsonProperty(value="reqClasses") OGCAPIReqClasses reqClasses,
                                 @JsonProperty(value="content") FeaturesContent content) {
        Objects.requireNonNull(reqClasses);
        Objects.requireNonNull(content);

        this.reqClasses = reqClasses;
        this.content = content;

        if (!conformsTo(OGCAPIConformanceClass.Core)) {
            throw new IllegalArgumentException("Service doesn't conform to WFS 3 Core conformance class");
        }
        if (!conformsTo(OGCAPIConformanceClass.GeoJSON)) {
            throw new IllegalArgumentException("Service doesn't conform to WFS 3 GeoJSON conformance class");
        }
    }

    public static OGCAPIFeaturesService fromURL(String url) throws OGCAPIException, IOException {
        return fromURL(url, null, null);
    }

    public static OGCAPIFeaturesService fromURL(String url, String user, String pass)
            throws OGCAPIException, IOException {
        while (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }

        String conformanceUrl = url + "/conformance";
        OGCAPIReqClasses reqClasses = load(conformanceUrl, user, pass, OGCAPIReqClasses.class);

        String collectionsUrl = constructUrl(url);
        FeaturesContent collections = load(collectionsUrl, user, pass, FeaturesContent.class);

        return new OGCAPIFeaturesService(reqClasses, collections);
    }

    public static String constructUrl(String baseUrl) {
        StringBuilder path = new StringBuilder(baseUrl);
        // Remove (all) trailing / characters
        while (path.charAt(path.length() - 1) == '/') {
            path.setLength(path.length() - 1);
        }
        path.append("/collections");
        return path.toString();
    }

    private static String getCollectionsPath(String endPoint, String collectionId) {
        StringBuilder path = new StringBuilder(constructUrl(endPoint));
        // Remove (all) trailing / characters
        while (path.charAt(path.length() - 1) == '/') {
            path.setLength(path.length() - 1);
        }
        path.append("/");
        path.append(collectionId);
        return path.toString();
    }

    public static String getItemsPath(String endPoint, String collectionId) {
        return getCollectionsPath(endPoint, collectionId) + "/items";
    }

    public String toJSON() throws JsonProcessingException {
        return OM.writeValueAsString(this);
    }
    public static byte[] toJSON(OGCAPIFeaturesService service) throws JsonProcessingException {
        return OM.writeValueAsBytes(service);
    }

    public static OGCAPIFeaturesService fromJSON(String json) throws IOException {
        return OM.readValue(json, OGCAPIFeaturesService.class);
    }
    public static OGCAPIFeaturesService fromJSON(byte[] b) throws IOException {
        return OM.readValue(b, OGCAPIFeaturesService.class);
    }

    private static <T> T load(String url, String user, String pass, Class<T> clazz)
            throws OGCAPIException, IOException {
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json");
        HttpURLConnection conn = IOHelper.getConnection(url, user, pass, null, headers);
        conn = IOHelper.followRedirect(conn, user, pass, null, headers, 3);
        int sc = conn.getResponseCode();
        if (sc == 200) {
            try (InputStream in = conn.getInputStream()) {
                return load(in, clazz);
            }
        } else {
            try (InputStream err = conn.getErrorStream()) {
                throw load(err, OGCAPIException.class);
            }
        }
    }

    static <T> T load(InputStream in, Class<T> clazz)
            throws JsonParseException, JsonMappingException, IOException {
        return OM.readValue(in, clazz);
    }

    public boolean conformsTo(OGCAPIConformanceClass req) {
        return reqClasses.getConformsTo().contains(req.url);
    }

    public List<FeaturesCollectionInfo> getCollections() {
        return new ArrayList<>(content.getCollections());
    }

    public Optional<FeaturesCollectionInfo> getCollection(String id) {
        return content.getCollections().stream()
                .filter(c -> c.getId().equals(id))
                .findAny();
    }

    public Set<String> getSupportedFormats (String collectionId) {
        return getCollection(collectionId)
                .orElseThrow(() -> new NoSuchElementException())
                .getLinks()
                .stream()
                .filter (link -> "items".equals(link.getRel()))
                .map(item -> item.getType())
                .filter (Objects::nonNull)
                .collect(Collectors.toSet());
    }

    public Set<String> getSupportedCrsURIs(String collectionId) throws NoSuchElementException {
        return getCollection(collectionId)
                .map(collection -> collection.getCrs())
                .map(list -> (Set<String>) new HashSet<>(list))
                .orElseThrow(() -> new NoSuchElementException());
    }

    public Set<String> getSupportedEpsgCodes(String collectionId) throws NoSuchElementException {
        return getSupportedCrsURIs(collectionId).stream()
                .map(OGCAPIFeaturesService::convertCrsToEpsg)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private static String convertCrsToEpsg(String crs) {
        if ("http://www.opengis.net/def/crs/OGC/1.3/CRS84".equals(crs)) {
            return "EPSG:4326"; // same projection, but axis order differs
        }
        try {
            return CapabilitiesService.shortSyntaxEpsg(crs);
        } catch (Exception e) {
            // Either failed - maybe the code is invalid
            // Only thing certain is that we can not use this
            return null;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        OGCAPIFeaturesService other = (OGCAPIFeaturesService) obj;
        if (content == null) {
            if (other.content != null)
                return false;
        } else if (!content.equals(other.content))
            return false;
        if (reqClasses == null) {
            if (other.reqClasses != null)
                return false;
        } else if (!reqClasses.equals(other.reqClasses))
            return false;
        return true;
    }

}
