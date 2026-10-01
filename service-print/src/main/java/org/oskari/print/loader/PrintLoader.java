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
import java.util.concurrent.CompletableFuture;
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
import org.json.JSONObject;
import org.oskari.print.mvt.MVTLayerData;
import org.oskari.print.mvt.MVTStyleLayer;
import org.oskari.print.mvt.MVTStyleResources;
import org.oskari.print.mvt.MVTTileGrid;
import org.oskari.print.request.PrintLayer;
import org.oskari.print.request.PrintRequest;

import fi.nls.oskari.domain.map.OskariLayer;
import org.oskari.service.wfs.client.OskariFeatureClient;

import javax.imageio.ImageIO;

public class PrintLoader {
    private static final Logger LOG = LogFactory.getLogger(PrintLoader.class);
    private static final String GROUP_KEY = "print";
    private static final String FORMAT = "image/png";
    private static final int RETRY_COUNT = 3;
    private static final int SLEEP_BETWEEN_RETRIES_MS = 50;

    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final RetryRegistry retryRegistry;
    private final TimeLimiter timeLimiter;
    private final ThreadPoolBulkhead bulkhead;
    private final ScheduledExecutorService executor;

    public PrintLoader() {
        int failRequests = PropertyUtil.getOptional("oskari." + GROUP_KEY + ".failrequests", 5);
        int rollingWindowMs = PropertyUtil.getOptional("oskari." + GROUP_KEY + ".rollingwindow", 100000);
        int waitDuration = PropertyUtil.getOptional("oskari." + GROUP_KEY + ".sleepwindow", 20000);
        int slidingWindow = rollingWindowMs / 1000;

        CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
                .waitDurationInOpenState(Duration.ofMillis(waitDuration))
                .permittedNumberOfCallsInHalfOpenState(failRequests/2)
                .minimumNumberOfCalls(failRequests)
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

    public Map<Integer, Future<MVTLayerData>> initMVTLayers(PrintRequest request) {
        Map<Integer, Future<MVTLayerData>> tilesByLayer = new HashMap<>();

        List<PrintLayer> requestedLayers = request.getLayers();
        if (requestedLayers == null) {
            return tilesByLayer;
        }

        double[] bbox = request.getBoundingBox();
        double resolution = request.getResolution();
        double[] extent = getProjectionExtent(request.getSrsName());

        for (PrintLayer layer : requestedLayers) {
            if (!OskariLayer.TYPE_VECTOR_TILE.equals(layer.getType())) {
                continue;
            }
            MVTTileGrid tileGrid = MVTTileGrid.create(layer.getOskariLayer(), extent);
            if (tileGrid == null) {
                LOG.info("Skipping vector tile layer", layer.getId(),
                        "- no tile grid for srs:", request.getSrsName());
                continue;
            }
            int zoom = tileGrid.getClosestZoom(resolution);
            // The style is read at the viewed zoom, which may be past the grid's last level
            double styleZoom = tileGrid.getZoomForResolution(resolution);
            tilesByLayer.put(layer.getZIndex(), loadMVTLayer(layer, tileGrid, bbox, zoom, styleZoom));
        }
        return tilesByLayer;
    }

    /**
     * Loads the tiles and, once their labels are known, the glyphs and icons
     * the style needs for them, so that drawing doesn't wait on the network.
     */
    private CompletableFuture<MVTLayerData> loadMVTLayer(PrintLayer layer, MVTTileGrid tileGrid,
            double[] bbox, int zoom, double styleZoom) {
        JSONObject style = layer.getMapboxStyle();
        if (style == null) {
            LOG.info("No Mapbox style for vector tile layer:", layer.getId());
            return CompletableFuture.completedFuture(MVTLayerData.empty());
        }
        List<MVTStyleLayer> styleLayers = MVTStyleLayer.parseAll(style.optJSONArray("layers"), styleZoom);
        if (styleLayers.isEmpty()) {
            LOG.info("Mapbox style of layer", layer.getId(), "draws nothing at zoom", styleZoom);
            return CompletableFuture.completedFuture(MVTLayerData.empty());
        }
        String commandKey = Integer.toString(layer.getId());
        return CommandLoadMVT.loadTiles(layer, tileGrid, bbox, zoom, this)
                .thenCompose(tiles -> tiles.isEmpty()
                        ? CompletableFuture.completedFuture(MVTLayerData.empty())
                        : MVTStyleResources.load(style, styleLayers, tiles, commandKey, this)
                                .thenApply(resources -> new MVTLayerData(tiles, styleLayers, resources)));
    }

    /**
     * Extent used as the tile grid fallback when the layer doesn't define one.
     */
    private static double[] getProjectionExtent(String srs) {
        String[] extent = PropertyUtil.getCommaSeparatedList(
                "oskari.wfs.mvt." + srs.toUpperCase().replace("EPSG:", "") + ".extent");
        if (extent.length != 4) {
            return null;
        }
        try {
            double[] values = new double[4];
            for (int i = 0; i < 4; i++) {
                values[i] = Double.parseDouble(extent[i]);
            }
            return values;
        } catch (NumberFormatException e) {
            LOG.warn("Invalid tile grid extent configured for srs:", srs);
            return null;
        }
    }

    public Future<BufferedImage> runImageSupplier(String commandKey, Supplier<BufferedImage> supplier) {
        return runSupplier(commandKey, supplier, () -> null);
    }

    public Future<SimpleFeatureCollection> runFeatureSupplier(String commandKey, Supplier<SimpleFeatureCollection> supplier) {
        return runSupplier(commandKey, supplier, DefaultFeatureCollection::new);
    }

    /**
     * Runs the supplier with the shared bulkhead, timeout, circuit breaker and retry.
     * @param fallback value to use when the call fails
     */
    <T> CompletableFuture<T> runSupplier(String commandKey, Supplier<T> supplier, Supplier<T> fallback) {
        return Decorators.ofSupplier(supplier)
                .withThreadPoolBulkhead(bulkhead)
                .withTimeLimiter(timeLimiter, executor)
                .withCircuitBreaker(circuitBreakerRegistry.circuitBreaker(commandKey))
                .withRetry(retryRegistry.retry(commandKey), executor)
                .withFallback(throwable -> fallback.get())
                .get().toCompletableFuture();
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
