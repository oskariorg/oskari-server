package org.oskari.service.mvt.wfs;

import org.geotools.data.simple.SimpleFeatureCollection;

import fi.nls.oskari.cache.ComputeOnceCache;

/**
 * Caching version of {@link org.oskari.service.mvt.wfs.OskariWFS110Client} WFSClient
 * Caches the parsed SimpleFeatureCollections
 */
public class CachingWFSClient extends OskariWFS110Client {

    private static final int DEFAULT_LIMIT = 100;

    private final ComputeOnceCache<SimpleFeatureCollection> cache;

    public CachingWFSClient() {
        this(DEFAULT_LIMIT);
    }

    public CachingWFSClient(int cacheSize) {
        cache = new ComputeOnceCache<>(cacheSize);
    }

    @Override
    public SimpleFeatureCollection tryGetFeatures(String endPoint, String user, String pass,
            String typeName, double[] bbox, String srsName, Integer maxFeatures) {
        String key = getCacheKey(endPoint, typeName, bbox, srsName, maxFeatures);
        return cache.get(key, __ -> super.tryGetFeatures(endPoint, user, pass, typeName, bbox, srsName, maxFeatures));
    }

    private String getCacheKey(String endPoint, String typeName, double[] bbox, String srsName, Integer maxFeatures) {
        String bboxStr = bbox != null ? getBBOX(bbox, srsName) : "null";
        String maxFeaturesStr = maxFeatures != null ? maxFeatures.toString() : "null";
        return String.join(",", endPoint, typeName, bboxStr, maxFeaturesStr);
    }

}
