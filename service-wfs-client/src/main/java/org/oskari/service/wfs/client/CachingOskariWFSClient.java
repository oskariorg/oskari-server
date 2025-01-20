package org.oskari.service.wfs.client;

import java.util.concurrent.TimeUnit;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.api.filter.Filter;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;

import org.locationtech.jts.geom.Envelope;

import fi.nls.oskari.cache.CacheManager;
import fi.nls.oskari.cache.ComputeOnceCache;
import fi.nls.oskari.domain.map.OskariLayer;

public class CachingOskariWFSClient extends OskariWFSClient {

    private static final String CACHE_NAME = CachingOskariWFSClient.class.getName();
    private static final int CACHE_SIZE_LIMIT = 10000;
    private static final long CACHE_EXPIRATION = TimeUnit.MINUTES.toMillis(5L);

    // Consider using Redis for caching (how much does serialization/deserialization to GeoJSON add?)
    private final ComputeOnceCache<SimpleFeatureCollection> cache;

    public CachingOskariWFSClient() {
        cache = CacheManager.getCache(CACHE_NAME, () -> new ComputeOnceCache<>(CACHE_SIZE_LIMIT, CACHE_EXPIRATION));
    }

    @Override
    public SimpleFeatureCollection getFeatures(OskariLayer layer,
            ReferencedEnvelope bbox, CoordinateReferenceSystem crs, Filter filter) {
        if (filter != null) {
            // Don't cache requests with a Filter
            return super.getFeatures(layer, bbox, crs, filter);
        }
        String key = getCacheKey(layer, bbox, crs);
        return cache.get(key, __ -> super.getFeatures(layer, bbox, crs, filter));
    }

    private String getCacheKey(OskariLayer layer, Envelope bbox, CoordinateReferenceSystem crs) {
        String endPoint = layer.getUrl();
        String typeName = layer.getName();
        String bboxStr = bbox != null ? bbox.toString() : "null";
        String crsStr = crs.getIdentifiers().iterator().next().toString();
        return String.join(",", endPoint, typeName, bboxStr, crsStr);
    }

}
