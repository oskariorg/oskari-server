package org.oskari.print.loader;

import java.util.Optional;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.oskari.print.request.PrintLayer;
import org.oskari.service.user.UserLayerService;
import org.oskari.service.wfs.client.OskariFeatureClient;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixThreadPoolProperties;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.util.PropertyUtil;

public class CommandLoadFeatureWFS extends HystrixCommand<SimpleFeatureCollection> {
    
    private static final String GROUP_KEY = "print";
    
    private OskariFeatureClient featureClient;
    private PrintLayer layer;
    private String uuid;
    private ReferencedEnvelope bbox;
    private CoordinateReferenceSystem crs;
    
    public CommandLoadFeatureWFS(OskariFeatureClient featureClient, PrintLayer layer,
            String uuid, ReferencedEnvelope bbox, CoordinateReferenceSystem crs) {
        super(Setter
                .withGroupKey(HystrixCommandGroupKey.Factory.asKey(AsyncImageLoader.GROUP_KEY))
                .andCommandKey(HystrixCommandKey.Factory.asKey(Integer.toString(layer.getId())))
                .andThreadPoolPropertiesDefaults(
                        HystrixThreadPoolProperties.Setter()
                        .withCoreSize(PropertyUtil.getOptional("oskari." + GROUP_KEY + ".job.pool.size", 10))
                        .withMaxQueueSize(PropertyUtil.getOptional("oskari." + GROUP_KEY + ".job.pool.limit", 100))
                        .withQueueSizeRejectionThreshold(PropertyUtil.getOptional("oskari." + GROUP_KEY + ".job.pool.queue", 100)))
                .andCommandPropertiesDefaults(
                        HystrixCommandProperties.Setter()
                        // .withExecutionTimeoutInMilliseconds(PropertyUtil.getOptional("oskari." + GROUP_KEY + ".job.timeoutms", 15000))
                        .withExecutionTimeoutInMilliseconds(PropertyUtil.getOptional("oskari." + GROUP_KEY + ".job.timeoutms", 150_000))
                        .withCircuitBreakerRequestVolumeThreshold(PropertyUtil.getOptional("oskari." + GROUP_KEY + ".failrequests", 5))
                        .withMetricsRollingStatisticalWindowInMilliseconds(PropertyUtil.getOptional("oskari." + GROUP_KEY + ".rollingwindow", 100_000))
                        .withCircuitBreakerSleepWindowInMilliseconds(PropertyUtil.getOptional("oskari." + GROUP_KEY + ".sleepwindow", 20_000)))
        );
        this.featureClient = featureClient;
        this.layer = layer;
        this.uuid = uuid;
        this.bbox = bbox;
        this.crs = crs;
    }

    @Override
    protected SimpleFeatureCollection run() throws Exception {
        try {
            String id = layer.getLayerId();
            OskariLayer oskariLayer = layer.getOskariLayer();
            Optional<UserLayerService> processor = layer.getProcessor();
            return featureClient.getFeatures(id, uuid, oskariLayer, bbox, crs, processor);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
    
    @Override
    protected SimpleFeatureCollection getFallback() {
        return new DefaultFeatureCollection();
    }

}
