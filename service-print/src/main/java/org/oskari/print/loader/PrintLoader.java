package org.oskari.print.loader;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.ServiceRuntimeException;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.PropertyUtil;
import io.github.resilience4j.bulkhead.ThreadPoolBulkhead;
import io.github.resilience4j.bulkhead.ThreadPoolBulkheadConfig;
import io.github.resilience4j.bulkhead.ThreadPoolBulkheadRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.decorators.Decorators;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.oskari.print.request.PrintLayer;
import org.oskari.print.request.PrintRequest;

import fi.nls.oskari.domain.map.OskariLayer;
import org.oskari.service.wfs.client.OskariFeatureClient;

import javax.imageio.ImageIO;

public class AsyncImageLoader {
    private static final Logger LOG = LogFactory.getLogger(AsyncImageLoader.class);
    private static final String GROUP_KEY = "print";
    private static final String FORMAT = "image/png";
    private static final int RETRY_COUNT = 3;
    private static final int SLEEP_BETWEEN_RETRIES_MS = 50;

    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final RetryRegistry retryRegistry;
    private final TimeLimiter timeLimiter;
    private final ThreadPoolBulkhead bulkhead;
    private final ScheduledExecutorService executor;

    public AsyncImageLoader() {
        int failRequests = PropertyUtil.getOptional("oskari." + GROUP_KEY + ".failrequests", 5);
        int rollingwindow = PropertyUtil.getOptional("oskari." + GROUP_KEY + ".rollingwindow", 100000);
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

        RetryConfig retryConfig = RetryConfig.custom()
                .maxAttempts(RETRY_COUNT)
                .waitDuration(Duration.ofMillis(SLEEP_BETWEEN_RETRIES_MS))
                .ignoreExceptions(ServiceRuntimeException.class)
                .failAfterMaxAttempts(true)
                .build();
        retryRegistry = RetryRegistry.of(retryConfig);

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

    public Map<Integer, Future<BufferedImage>> initImageLayers(PrintRequest request) {
        final Map<Integer, Future<BufferedImage>> images = new HashMap<>();

        final List<PrintLayer> requestedLayers = request.getLayers();
        if (requestedLayers == null) {
            return images;
        }

        final int width = request.getWidth();
        final int height = request.getHeight();
        final double[] bbox = request.getBoundingBox();
        final String srsName = request.getSrsName();

        for (PrintLayer layer : requestedLayers) {
            Supplier<BufferedImage> supplier = null;
            switch (layer.getType()) { 
            case OskariLayer.TYPE_WMS:
                supplier = () -> CommandLoadImageWMS.loadImage(layer, width, height, bbox, srsName,request.getTime());
                break;
            case OskariLayer.TYPE_WMTS:
                supplier = () -> CommandLoadImageWMTS.loadImage(layer, width, height, bbox, srsName, request.getResolution(), this);
                break;
            case OskariLayer.TYPE_ARCGIS93:
                supplier = () -> CommandLoadImageArcGISREST.loadImage(layer, width, height, bbox, srsName);
                break;
            }
            if (supplier != null) {
                images.put(layer.getZIndex(), runImageSupplier(layer.getLayerId(), supplier));
            }
        }

        return images;
    }

    public Map<Integer, Future<SimpleFeatureCollection>> initVectorLayers(PrintRequest request,
                                                                           OskariFeatureClient featureClient) {
        Map<Integer, Future<SimpleFeatureCollection>> featureCollections = new HashMap<>();

        List<PrintLayer> requestedLayers = request.getLayers();
        if (requestedLayers == null) {
            return featureCollections;
        }

        CoordinateReferenceSystem crs = request.getCrs();
        double[] bbox = request.getBoundingBox();
        ReferencedEnvelope bbox1 = new ReferencedEnvelope(bbox[0], bbox[2], bbox[1], bbox[3], crs);

        for (PrintLayer layer : requestedLayers) {
            if (!layer.getType().equals(OskariLayer.TYPE_WFS)) {
                continue;
            }
            String commandKey = Integer.toString(layer.getId());
            Supplier<SimpleFeatureCollection> supplier = () -> CommandLoadFeatureWFS.getFeatures(featureClient, layer, bbox1, crs);
            featureCollections.put(layer.getZIndex(), runFeatureSupplier(commandKey, supplier));
        }
        return featureCollections;
    }

    public Future<BufferedImage> runImageSupplier(String commandKey, Supplier<BufferedImage> supplier) {
        try {
            return Decorators.ofSupplier(supplier)
                    .withThreadPoolBulkhead(bulkhead)
                    .withTimeLimiter(timeLimiter, executor)
                    .withCircuitBreaker(circuitBreakerRegistry.circuitBreaker(commandKey))
                    .withRetry(retryRegistry.retry(commandKey), executor)
                    .withFallback(throwable -> null)
                    .get().toCompletableFuture();
        } catch (CompletionException e) {
            throw new RuntimeException(e.getCause());
        }
    }

    public Future<SimpleFeatureCollection> runFeatureSupplier(String commandKey, Supplier<SimpleFeatureCollection> supplier) {
        try {
            return Decorators.ofSupplier(supplier)
                    .withThreadPoolBulkhead(bulkhead)
                    .withTimeLimiter(timeLimiter, executor)
                    .withCircuitBreaker(circuitBreakerRegistry.circuitBreaker(commandKey))
                    .withRetry(retryRegistry.retry(commandKey), executor)
                    .withFallback(throwable -> new DefaultFeatureCollection())
                    .get().toCompletableFuture();
        } catch (CompletionException e) {
            throw new RuntimeException(e.getCause());
        }
    }

    public static BufferedImage loadImageFromURL(String uri, String user, String pass) {
        LOG.debug("Loading print content from:", uri);
        try {
            HttpURLConnection conn = IOHelper.getConnection(uri, user, pass);
            if (conn.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND) {
                // short-circuit 404 as we get these a lot in the log
                throw new ServiceRuntimeException("Not found");
            }
            try (InputStream in = new BufferedInputStream(conn.getInputStream())) {
                return ImageIO.read(in);
            }
        } catch (IOException e) {
            throw new ServiceRuntimeException(e.getMessage(), e);
        }
    }
}
