package org.oskari.service.wfs.client;

import java.util.function.Function;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.oskari.service.wfs3.client.OskariWFS3Client;

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
            String typeName, double[] bbox, String srsName, Integer maxFeatures) {
        String key = getCacheKey(endPoint, typeName, bbox, srsName, maxFeatures);
        return cache.get(key, getSupplier(endPoint, version, user, pass, typeName, bbox, srsName, maxFeatures));
    }

    private Function<String, SimpleFeatureCollection> getSupplier(String endPoint, String version,
            String user, String pass, String typeName, double[] bbox, String srsName, Integer maxFeatures) {
        switch (version) {
        case WFS_3_VERSION:
            return __ -> wfs3Client.tryGetFeatures(endPoint, user, pass, typeName, bbox, maxFeatures);
        default:
            return  __ -> wfs110Client.tryGetFeatures(endPoint, user, pass, typeName, bbox, srsName, maxFeatures);
        }
    }

    private String getCacheKey(String endPoint, String typeName, double[] bbox, String srsName, Integer maxFeatures) {
        String bboxStr = bbox != null ? wfs110Client.getBBOX(bbox, srsName) : "null";
        String maxFeaturesStr = maxFeatures != null ? maxFeatures.toString() : "null";
        return String.join(",", endPoint, typeName, bboxStr, maxFeaturesStr);
    }

}
