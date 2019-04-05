package org.oskari.service.wfs.client;

import java.util.concurrent.TimeUnit;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.filter.Filter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;

import fi.nls.oskari.cache.CacheManager;
import fi.nls.oskari.cache.ComputeOnceCache;

public class CachingWFSClient {

    private static final String CACHE_NAME = CachingWFSClient.class.getName();
    // one user on z 7 will trigger about ~100 requests to the service since tiles are always loaded as z 8
    // so this is enough for 100 users looking at different layers/areas without cache overflowing
    // or 20 users looking at 5 wfs layers each on z 7
    // consider using Redis for caching (how much does serialization/deserialization to GeoJSON add?)
    private static final int CACHE_SIZE_LIMIT = 10000;
    private static final long CACHE_EXPIRATION = TimeUnit.MINUTES.toMillis(5L);

    private final ComputeOnceCache<SimpleFeatureCollection> cache;

    public CachingWFSClient() {
        cache = CacheManager.getCache(CACHE_NAME,
                () -> new ComputeOnceCache<>(CACHE_SIZE_LIMIT, CACHE_EXPIRATION));
    }

    public SimpleFeatureCollection tryGetFeatures(String endPoint, String version,
            String user, String pass, String typeName,
            ReferencedEnvelope bbox, CoordinateReferenceSystem crs,
            Integer maxFeatures, Filter filter) {
        if (filter != null) {
            // Don't cache requests with a Filter
            return OskariWFSClient.tryGetFeatures(
                    endPoint, version,
                    user, pass, typeName,
                    bbox, crs, maxFeatures, filter);
        }
        String key = getCacheKey(endPoint, typeName, bbox, crs, maxFeatures);
        return cache.get(key, __ -> OskariWFSClient.tryGetFeatures(
                endPoint, version,
                user, pass, typeName,
                bbox, crs, maxFeatures, filter));
    }

    private String getCacheKey(String endPoint, String typeName, Envelope bbox,
            CoordinateReferenceSystem crs, Integer maxFeatures) {
        String bboxStr = bbox != null ? bbox.toString() : "null";
        String maxFeaturesStr = maxFeatures != null ? maxFeatures.toString() : "null";
        return String.join(",", endPoint, typeName, bboxStr,
                crs.getIdentifiers().iterator().next().toString(), maxFeaturesStr);
    }

}
