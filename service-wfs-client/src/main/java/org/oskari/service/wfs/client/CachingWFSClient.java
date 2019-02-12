package org.oskari.service.wfs.client;

import java.util.function.Function;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.oskari.service.wfs3.client.OskariWFS3Client;

import com.vividsolutions.jts.geom.Envelope;

import fi.nls.oskari.cache.ComputeOnceCache;

public class CachingWFSClient {

    private static final int DEFAULT_LIMIT = 100;
    private static final String WFS_3_VERSION = "3.0.0";

    private final ComputeOnceCache<SimpleFeatureCollection> cache;
    private final OskariWFS110Client wfs110Client;
    private final OskariWFS3Client wfs3Client;

    public CachingWFSClient() {
        this(DEFAULT_LIMIT);
    }

    public CachingWFSClient(int cacheSize) {
        cache = new ComputeOnceCache<>(cacheSize);
        wfs110Client = new OskariWFS110Client();
        wfs3Client = new OskariWFS3Client();
    }

    public SimpleFeatureCollection tryGetFeatures(String endPoint, String version,
            String user, String pass,
            String typeName, ReferencedEnvelope bbox,
            CoordinateReferenceSystem crs, Integer maxFeatures) {
        String key = getCacheKey(endPoint, typeName, bbox, crs, maxFeatures);
        return cache.get(key, getSupplier(endPoint, version, user, pass, typeName, bbox, crs, maxFeatures));
    }

    private Function<String, SimpleFeatureCollection> getSupplier(String endPoint, String version,
            String user, String pass,
            String typeName, ReferencedEnvelope bbox,
            CoordinateReferenceSystem crs, Integer maxFeatures) {
        switch (version) {
        case WFS_3_VERSION:
            return __ -> wfs3Client.tryGetFeatures(endPoint, user, pass, typeName, bbox, crs, maxFeatures);
        default:
            return  __ -> wfs110Client.tryGetFeatures(endPoint, user, pass, typeName, bbox, crs, maxFeatures);
        }
    }

    private String getCacheKey(String endPoint, String typeName, Envelope bbox,
            CoordinateReferenceSystem crs, Integer maxFeatures) {
        String bboxStr = bbox != null ? bbox.toString() : "null";
        String maxFeaturesStr = maxFeatures != null ? maxFeatures.toString() : "null";
        return String.join(",", endPoint, typeName, bboxStr,
                crs.getIdentifiers().iterator().next().toString(), maxFeaturesStr);
    }

}
