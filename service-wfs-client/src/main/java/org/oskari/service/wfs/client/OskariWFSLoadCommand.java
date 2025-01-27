package org.oskari.service.wfs.client;

import fi.nls.oskari.domain.map.OskariLayer;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.api.filter.Filter;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.oskari.service.wfs3.OskariWFS3Client;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixThreadPoolProperties;

import fi.nls.oskari.util.PropertyUtil;

public class OskariWFSLoadCommand extends HystrixCommand<SimpleFeatureCollection> {

    private static final String WFS_3_VERSION = "3.0.0";
    private static final String WFS_2_VERSION = "2.0.0";
    private static final String GROUP_KEY = "wfs";

    private final OskariLayer layer;
    private final ReferencedEnvelope bbox;
    private final CoordinateReferenceSystem crs;
    private final Filter filter;

    public OskariWFSLoadCommand(OskariLayer layer, ReferencedEnvelope bbox,
            CoordinateReferenceSystem crs, Filter filter) {
        this(layer, bbox, crs, filter, Setter
                .withGroupKey(HystrixCommandGroupKey.Factory.asKey(GROUP_KEY))
                .andCommandKey(HystrixCommandKey.Factory.asKey(layer.getUrl()))
                .andThreadPoolPropertiesDefaults(
                        HystrixThreadPoolProperties.Setter()
                        .withCoreSize(PropertyUtil.getOptional("oskari." + GROUP_KEY + ".job.pool.size", 10))
                        .withMaxQueueSize(PropertyUtil.getOptional("oskari." + GROUP_KEY + ".job.pool.limit", 100))
                        .withQueueSizeRejectionThreshold(PropertyUtil.getOptional("oskari." + GROUP_KEY + ".job.pool.queue", 100)))
                .andCommandPropertiesDefaults(
                        HystrixCommandProperties.Setter()
                        .withExecutionTimeoutInMilliseconds(PropertyUtil.getOptional("oskari." + GROUP_KEY + ".job.timeoutms", 15000))
                        .withCircuitBreakerRequestVolumeThreshold(PropertyUtil.getOptional("oskari." + GROUP_KEY + ".failrequests", 5))
                        .withMetricsRollingStatisticalWindowInMilliseconds(PropertyUtil.getOptional("oskari." + GROUP_KEY + ".rollingwindow", 100000))
                        .withCircuitBreakerSleepWindowInMilliseconds(PropertyUtil.getOptional("oskari." + GROUP_KEY + ".sleepwindow", 20000))));
    }

    public OskariWFSLoadCommand(OskariLayer layer, ReferencedEnvelope bbox,
            CoordinateReferenceSystem crs, Filter filter, Setter setter) {
        super(setter);
        this.layer = layer;
        this.bbox = bbox;
        this.crs = crs;
        this.filter = filter;
    }

    @Override
    protected SimpleFeatureCollection run() throws Exception {
        switch (layer.getVersion()) {
        case WFS_3_VERSION:
            return OskariWFS3Client.getFeatures(layer, bbox, crs, filter);
        case WFS_2_VERSION:
            return OskariWFS2Client.getFeatures(layer, bbox, crs, filter);
        default:
            return OskariWFS110Client.getFeatures(layer, bbox, crs, filter);
        }
    }

}
