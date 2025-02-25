package org.oskari.service.wfs.client;

import fi.nls.oskari.domain.map.OskariLayer;

import fi.nls.oskari.service.ServiceRuntimeException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.api.filter.Filter;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.oskari.service.wfs3.OskariWFS3Client;

import io.github.resilience4j.decorators.Decorators;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.bulkhead.ThreadPoolBulkhead;
import io.github.resilience4j.bulkhead.ThreadPoolBulkheadConfig;
import io.github.resilience4j.bulkhead.ThreadPoolBulkheadRegistry;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

import fi.nls.oskari.util.PropertyUtil;

public class OskariWFSLoader {
    private static final String WFS_3_VERSION = "3.0.0";
    private static final String WFS_2_VERSION = "2.0.0";
    private static final String GROUP_KEY = "wfs";
    protected static final String ERR_SHORT_CIRCUIT = "Backing service disabled temporarily";
    protected static final String ERR_TIMEOUT = "Request to backing service timed out";
    protected static final String ERR_FAILED_TO_RETRIEVE_FEATURES = "Failed to retrieve features";

    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final TimeLimiter timeLimiter;
    private final ThreadPoolBulkhead bulkhead;
    private final ScheduledExecutorService executor;

    public OskariWFSLoader() {
        int failRequests = PropertyUtil.getOptional("oskari." + GROUP_KEY + ".failrequests", 5);
        int slidingWindowMs = PropertyUtil.getOptional("oskari." + GROUP_KEY + ".rollingwindow", 100000);
        int waitDuration = PropertyUtil.getOptional("oskari." + GROUP_KEY + ".sleepwindow", 20000);
        int slidingWindow = failRequests * 2; // failing rate 50%

        CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
                .waitDurationInOpenState(Duration.ofMillis(waitDuration))
                .permittedNumberOfCallsInHalfOpenState(failRequests)
                .minimumNumberOfCalls(slidingWindow)
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.TIME_BASED)
                .slidingWindowSize(slidingWindow)
                .build();
        circuitBreakerRegistry = CircuitBreakerRegistry.of(circuitBreakerConfig);

        int poolSize = PropertyUtil.getOptional("oskari." + GROUP_KEY + ".job.pool.size", 10);
        int poolLimit = PropertyUtil.getOptional("oskari." + GROUP_KEY + ".job.pool.limit", 100);
        int queueSize = PropertyUtil.getOptional("oskari." + GROUP_KEY + ".job.pool.queue", 100);
        ThreadPoolBulkheadConfig bulkheadConfig = ThreadPoolBulkheadConfig.custom()
                .maxThreadPoolSize(poolSize)
                .coreThreadPoolSize(poolSize/2)
                .queueCapacity(queueSize)
                .build();
        ThreadPoolBulkheadRegistry registry = ThreadPoolBulkheadRegistry.of(bulkheadConfig);
        bulkhead = registry.bulkhead(GROUP_KEY);

        executor = Executors.newScheduledThreadPool(3);

        int timeout = PropertyUtil.getOptional("oskari." + GROUP_KEY + ".job.timeoutms", 15000);
        TimeLimiterConfig timeLimiterConfig = TimeLimiterConfig.custom().timeoutDuration(Duration.ofMillis(timeout)).build();
        timeLimiter = TimeLimiterRegistry.of(timeLimiterConfig).timeLimiter(GROUP_KEY);
    }

    public SimpleFeatureCollection getFeatures(OskariLayer layer, ReferencedEnvelope bbox, CoordinateReferenceSystem crs, Filter filter) {
        try {
            return Decorators.ofSupplier(getSupplier(layer, bbox, crs, filter))
                .withThreadPoolBulkhead(bulkhead)
                .withTimeLimiter(timeLimiter, executor)
                .withCircuitBreaker(circuitBreakerRegistry.circuitBreaker(layer.getUrl()))
                .get().toCompletableFuture().join();
        } catch (Exception e) { // CompletionException
            Throwable cause = e.getCause();
            if (cause instanceof TimeoutException) {
                throw new ServiceRuntimeException(ERR_TIMEOUT);
            }
            if (cause instanceof CallNotPermittedException) {
                throw new ServiceRuntimeException(ERR_SHORT_CIRCUIT);
            }
            throw new ServiceRuntimeException(ERR_FAILED_TO_RETRIEVE_FEATURES, e);
        }
    }

    protected Supplier<SimpleFeatureCollection> getSupplier(OskariLayer layer, ReferencedEnvelope bbox, CoordinateReferenceSystem crs, Filter filter) {
        switch (layer.getVersion()) {
        case WFS_3_VERSION:
            return () -> OskariWFS3Client.getFeatures(layer, bbox, crs, filter);
        case WFS_2_VERSION:
            return () -> OskariWFS2Client.getFeatures(layer, bbox, crs, filter);
        default:
            return () -> OskariWFS110Client.getFeatures(layer, bbox, crs, filter);
        }
    }
}
