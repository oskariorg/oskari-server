package org.oskari.service.wfs3;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.*;

import org.geotools.referencing.CRS;
import org.oskari.service.wfs3.model.WFS3CollectionInfo;
import org.oskari.service.wfs3.model.WFS3ConformanceClass;
import org.oskari.service.wfs3.model.WFS3Content;
import org.oskari.service.wfs3.model.WFS3Exception;
import org.oskari.service.wfs3.model.WFS3ReqClasses;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import fi.nls.oskari.util.IOHelper;

public class WFS3Service {
    private static final ObjectMapper OM;
    static {
        OM = new ObjectMapper();
        OM.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @JsonProperty(value="reqClasses")
    private final WFS3ReqClasses reqClasses;
    @JsonProperty(value="content")
    private final WFS3Content content;

    @JsonCreator
    public WFS3Service(@JsonProperty(value="reqClasses") WFS3ReqClasses reqClasses,
            @JsonProperty(value="content") WFS3Content content) {
        Objects.requireNonNull(reqClasses);
        Objects.requireNonNull(content);

        this.reqClasses = reqClasses;
        this.content = content;

        if (!conformsTo(WFS3ConformanceClass.Core)) {
            throw new IllegalArgumentException("Service doesn't conform to WFS 3 Core conformance class");
        }
        if (!conformsTo(WFS3ConformanceClass.GeoJSON)) {
            throw new IllegalArgumentException("Service doesn't conform to WFS 3 GeoJSON conformance class");
        }
    }

    public static WFS3Service fromURL(String url) throws WFS3Exception, IOException {
        return fromURL(url, null, null);
    }

    public static WFS3Service fromURL(String url, String user, String pass)
            throws WFS3Exception, IOException {
        while (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }

        String conformanceUrl = url + "/conformance";
        WFS3ReqClasses reqClasses = load(conformanceUrl, user, pass, WFS3ReqClasses.class);

        String collectionsUrl = url + "/collections";
        WFS3Content collections = load(collectionsUrl, user, pass, WFS3Content.class);

        return new WFS3Service(reqClasses, collections);
    }

    public static byte[] toJSON(WFS3Service service) throws JsonProcessingException {
        return OM.writeValueAsBytes(service);
    }

    public static WFS3Service fromJSON(byte[] b) throws IOException {
        return OM.readValue(b, WFS3Service.class);
    }

    private static <T> T load(String url, String user, String pass, Class<T> clazz)
            throws WFS3Exception, IOException {
        HttpURLConnection conn = IOHelper.getConnection(url, user, pass);
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json");
        conn = IOHelper.followRedirect(conn, user, pass, headers, 3);

        int sc = conn.getResponseCode();
        if (sc == 200) {
            try (InputStream in = conn.getInputStream()) {
                return load(in, clazz);
            }
        } else {
            try (InputStream err = conn.getErrorStream()) {
                throw load(err, WFS3Exception.class);
            }
        }
    }

    static <T> T load(InputStream in, Class<T> clazz)
            throws JsonParseException, JsonMappingException, IOException {
        return OM.readValue(in, clazz);
    }

    public boolean conformsTo(WFS3ConformanceClass req) {
        return reqClasses.getConformsTo().contains(req.url);
    }

    public List<WFS3CollectionInfo> getCollections() {
        return new ArrayList<>(content.getCollections());
    }

    public Optional<WFS3CollectionInfo> getCollection(String id) {
        return content.getCollections().stream()
                .filter(c -> c.getId().equals(id))
                .findAny();
    }
    public Set<String> getSupportedEpsgCodes (String collectionId) throws NoSuchElementException {
        Set<String> epsgs = new HashSet<>();
        WFS3CollectionInfo collection = getCollection(collectionId).get();
        for (String crs : collection.getCrs()) {
            String epsg = convertCrsToEpsg(crs);
            if (epsg != null) {
                epsgs.add(epsg);
            }
        }
        return epsgs;
    }

    public static String convertCrsToEpsg (String crs) {
        if (crs.toUpperCase().contains("CRS84")){
            return "EPSG:4326"; // same projection, but axis order differs
        }
        try {
            return CRS.lookupIdentifier(CRS.decode(crs), false);
        } catch (Exception e) {
            // not valid EPSG projection
        }
        return null;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        WFS3Service other = (WFS3Service) obj;
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
