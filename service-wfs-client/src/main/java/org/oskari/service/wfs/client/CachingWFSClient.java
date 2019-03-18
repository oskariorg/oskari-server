package org.oskari.service.wfs.client;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.filter.Filter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;

import fi.nls.oskari.cache.ComputeOnceCache;

public class CachingWFSClient {

    private static final int DEFAULT_LIMIT = 100;

    private final ComputeOnceCache<SimpleFeatureCollection> cache;

    public CachingWFSClient() {
        this(DEFAULT_LIMIT);
    }

    public CachingWFSClient(int cacheSize) {
        cache = new ComputeOnceCache<>(cacheSize);
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
