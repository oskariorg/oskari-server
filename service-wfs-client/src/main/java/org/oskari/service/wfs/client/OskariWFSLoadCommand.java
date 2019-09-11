package org.oskari.service.wfs.client;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.ServiceRuntimeException;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.filter.Filter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.oskari.service.wfs3.OskariWFS3Client;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixThreadPoolProperties;

import fi.nls.oskari.util.PropertyUtil;

public class OskariWFSLoadCommand extends HystrixCommand<SimpleFeatureCollection> {

    private static final Logger LOG = LogFactory.getLogger(OskariWFSLoadCommand.class);
    private static final String WFS_3_VERSION = "3.0.0";
    private static final String WFS_2_VERSION = "2.0.0";
    private static final String GROUP_KEY = "wfs";

    private final String endPoint;
    private final String version;
    private final String user;
    private final String pass;
    private final String typeName;
    private final ReferencedEnvelope bbox;
    private final CoordinateReferenceSystem crs;
    private final int maxFeatures;
    private final Filter filter;

    public OskariWFSLoadCommand(String endPoint, String version, String user, String pass,
            String typeName, ReferencedEnvelope bbox, CoordinateReferenceSystem crs,
            int maxFeatures, Filter filter) {
        this(endPoint, version, user, pass, typeName, bbox, crs, maxFeatures, filter, Setter
                .withGroupKey(HystrixCommandGroupKey.Factory.asKey(GROUP_KEY))
                .andCommandKey(HystrixCommandKey.Factory.asKey(endPoint))
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

    public OskariWFSLoadCommand(String endPoint, String version, String user, String pass,
            String typeName, ReferencedEnvelope bbox, CoordinateReferenceSystem crs,
            int maxFeatures, Filter filter, Setter setter) {
        super(setter);
        this.endPoint = endPoint;
        this.version = version;
        this.user = user;
        this.pass = pass;
        this.typeName = typeName;
        this.bbox = bbox;
        this.crs = crs;
        this.maxFeatures = maxFeatures;
        this.filter = filter;
    }

    @Override
    protected SimpleFeatureCollection run() throws Exception {
        switch (version) {
        case WFS_3_VERSION:
            return OskariWFS3Client.getFeatures(endPoint, user, pass, typeName, bbox, crs, maxFeatures);
        case WFS_2_VERSION:
            return OskariWFS2Client.getFeatures(endPoint, user, pass, typeName, bbox, crs, maxFeatures, filter);
        default:
            return OskariWFS110Client.getFeatures(endPoint, user, pass, typeName, bbox, crs, maxFeatures, filter);
        }
    }

    public SimpleFeatureCollection getFallback() {
        LOG.debug("Error getting features from", endPoint, "Events completed:\n", getExecutionEvents());
        // handler expects an Exception or SimpleFeatureCollection. If we don't throw an exception a null value is returned.
        throw new ServiceRuntimeException("Error getting features from " + endPoint, getExceptionFromThrowable(getExecutionException()));
    }
}
