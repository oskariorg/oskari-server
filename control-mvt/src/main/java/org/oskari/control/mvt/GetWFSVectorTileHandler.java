package org.oskari.control.mvt;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import fi.nls.oskari.control.feature.AbstractWFSFeaturesHandler;
import fi.nls.oskari.control.view.modifier.bundle.BundleHandler;
import fi.nls.oskari.control.view.modifier.bundle.MapfullHandler;
import fi.nls.oskari.view.modifier.ViewModifierManager;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.oskari.service.mvt.SimpleFeaturesMVTEncoder;
import org.oskari.service.mvt.TileCoord;
import org.oskari.service.mvt.WFSTileGrid;
import org.oskari.service.user.UserLayerService;

import com.vividsolutions.jts.geom.Envelope;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.cache.CacheManager;
import fi.nls.oskari.cache.ComputeOnceCache;
import fi.nls.oskari.control.ActionConstants;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.service.ServiceRuntimeException;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.ResponseHelper;
import org.oskari.service.mvt.WFSTileGridProperties;

@OskariActionRoute("GetWFSVectorTile")
public class GetWFSVectorTileHandler extends AbstractWFSFeaturesHandler {

    protected static final String MVT_CONTENT_TYPE = "application/vnd.mapbox-vector-tile";
    protected static final String PARAM_Z = "z";
    protected static final String PARAM_X = "x";
    protected static final String PARAM_Y = "y";

    private static final int DEFAULT_MIN_ZOOM_LEVEL = 7;
    private static final Map<String, WFSTileGrid> KNOWN_TILE_GRIDS;
    static {
        KNOWN_TILE_GRIDS = new HashMap<>();
        KNOWN_TILE_GRIDS.put("EPSG:3067", new WFSTileGrid(new double[] { -548576, 6291456, -548576 + (8192*256), 6291456 + (8192*256) }, 15));
        KNOWN_TILE_GRIDS.put("EPSG:3857", new WFSTileGrid(new double[] { -20037508.3427892, -20037508.3427892, 20037508.3427892, 20037508.3427892 }, 18));
    }

    private static final int TILE_EXTENT = 4096;
    private static final int TILE_BUFFER = 256;

    private static final int CACHE_LIMIT = 256;
    private static final long CACHE_EXPIRATION = TimeUnit.MINUTES.toMillis(5);

    private ComputeOnceCache<byte[]> tileCache;
    private WFSTileGridProperties tileGridProperties;

    @Override
    public void init() {
        super.init();
        tileCache = CacheManager.getCache(getClass().getName(),
                () -> new ComputeOnceCache<>(CACHE_LIMIT, CACHE_EXPIRATION));
        tileGridProperties = new WFSTileGridProperties();

        final Map<String, BundleHandler> handlers = ViewModifierManager.getModifiersOfType(BundleHandler.class);
        MapfullHandler mapfullHandler = (MapfullHandler)handlers.get("mapfull");
        WFSVectorLayerPluginViewModifier pluginHandler = new WFSVectorLayerPluginViewModifier();
        mapfullHandler.registerPluginHandler(WFSVectorLayerPluginViewModifier.PLUGIN_NAME, pluginHandler);

        Map<String, WFSTileGrid> propTileGrids = tileGridProperties.getTileGridMap();
        KNOWN_TILE_GRIDS.keySet().stream().forEach(srsName -> {
            pluginHandler.setMinZoomLevelForSRS(srsName, DEFAULT_MIN_ZOOM_LEVEL);
            pluginHandler.setTileGridForSRS(srsName, KNOWN_TILE_GRIDS.get(srsName));
        });
        propTileGrids.keySet().stream().forEach(srsName -> {
            pluginHandler.setMinZoomLevelForSRS(srsName, DEFAULT_MIN_ZOOM_LEVEL);
            pluginHandler.setTileGridForSRS(srsName, propTileGrids.get(srsName));
        });
    }

    @Override
    public void handleAction(ActionParameters params) throws ActionException {
        final String id = params.getRequiredParam(ActionConstants.PARAM_ID);
        final String srs = params.getRequiredParam(ActionConstants.PARAM_SRS);
        final int z = params.getRequiredParamInt(PARAM_Z);
        final int x = params.getRequiredParamInt(PARAM_X);
        final int y = params.getRequiredParamInt(PARAM_Y);

        final Optional<UserLayerService> contentProcessor = getUserContentProsessor(id);
        final OskariLayer layer = findLayer(id, params.getUser(), contentProcessor);
        requireWFSLayer(layer);
        final String uuid = params.getUser().getUuid();

        final WFSTileGrid gridFromProps = tileGridProperties.getTileGrid(srs.toUpperCase());
        final WFSTileGrid grid = gridFromProps != null ? gridFromProps : KNOWN_TILE_GRIDS.get(srs.toUpperCase());
        validateTile(grid, z, x, y);
        validateScaleDenominator(layer, grid, z);

        final CoordinateReferenceSystem crs;
        try {
            crs = CRS.decode(srs, true);
        } catch (Exception e) {
            throw new ActionParamsException("Invalid srs!");
        }

        final String cacheKey = getCacheKey(id, srs, z, x, y);
        final byte[] resp;
        try {
            resp = tileCache.get(cacheKey, __ -> createTile(id, uuid, layer, crs, grid, z, x, y, contentProcessor));
        } catch (ServiceRuntimeException e) {
            throw new ActionException(e.getMessage());
        }
        params.getResponse().addHeader("Access-Control-Allow-Origin", "*");
        params.getResponse().addHeader("Content-Encoding", "gzip");
        ResponseHelper.writeResponse(params, 200, MVT_CONTENT_TYPE, resp);
    }


    private void validateTile(WFSTileGrid grid, int z, int x, int y)
            throws ActionParamsException {
        if (grid == null) {
            throw new ActionParamsException("Unknown srs");
        }
        if (z < 0) {
            throw new ActionParamsException("z must be non-negative");
        }
        if (x < 0) {
            throw new ActionParamsException("x must be non-negative");
        }
        if (y < 0) {
            throw new ActionParamsException("y must be non-negative");
        }
        if (z > grid.getMaxZoom()) {
            throw new ActionParamsException("z must be <= " + grid.getMaxZoom());
        }
        if (z < 7) {
            // we always request features with z 8 and anything below 7 will trigger too many requests to services
            throw new ActionParamsException("z must be > 6");
        }
        int matrixWidthHeight = WFSTileGrid.getMatrixSize(z);
        if (x >= matrixWidthHeight) {
            throw new ActionParamsException("x must be < " + matrixWidthHeight + " (z = " + z + ")");
        }
        if (y >= matrixWidthHeight) {
            throw new ActionParamsException("y must be < " + matrixWidthHeight + " (z = " + z + ")");
        }
    }

    private void validateScaleDenominator(OskariLayer layer, WFSTileGrid grid, int z)
            throws ActionParamsException {
        double scaleDenominator = getScaleDenominator(grid, z);
        if (layer.getMinScale() != null) {
            if (scaleDenominator > layer.getMinScale()) {
                // Bigger denominator <=> Smaller scale
                throw new ActionParamsException("z too low for layer");
            }
        }
        if (layer.getMaxScale() != null) {
            if (scaleDenominator < layer.getMaxScale()) {
                // Smaller denominator <=> Bigger scale
                throw new ActionParamsException("z too high for layer");
            }
        }
    }

    private double getScaleDenominator(WFSTileGrid grid, int z) {
        double resolution = grid.getResolutionForZ(z);
        return getScaleDenominator(resolution);
    }

    private double getScaleDenominator(double resolution) {
        return resolution * 1000 / 0.28; // OGC WMTS 0.28 mm/px
    }

    private String getCacheKey(String id, String srs, int z, int x, int y) {
        return "WFS_" + id + "_" + srs + "_" + z + "_" + x + "_" + y;
    }

    /**
     * Creates the actual MVT tile
     * @return an MVT tile as a GZipped byte array
     * @throws ActionException
     */
    private byte[] createTile(String id, String uuid, OskariLayer layer, CoordinateReferenceSystem crs,
            WFSTileGrid grid, int z, int x, int y,
            Optional<UserLayerService> contentProcessor) throws ServiceRuntimeException {
        // Find nearest higher resolution
        // always fetch at z 8 so we don't cache same features on multiple zoom levels
        int targetZ = grid.getZForResolution(8, -1);

        List<TileCoord> wfsTiles;
        int dz = z - targetZ;

        if (dz < 0) {
            // get adjacent tiles so we can unify z=8 tiles to create z7 etc tiles
            int d = (int) Math.pow(2, -dz);
            int targetX1 = x * d;
            int targetY1 = y * d;
            int targetX2 = (x+1) * d;
            int targetY2 = (y+1) * d;
            wfsTiles = new ArrayList<>();
            for (int targetX = targetX1; targetX < targetX2; targetX++) {
                for (int targetY = targetY1; targetY < targetY2; targetY++) {
                    wfsTiles.add(new TileCoord(targetZ, targetX, targetY));
                }
            }
        } else if (dz == 0) {
            // this is the sweet spot - just get the features that was requested
            wfsTiles = Collections.singletonList(new TileCoord(z, x, y));
        } else {
            // recalculate x/y to match a z=8 tile
            int div = (int) Math.pow(2, dz);
            int targetX = x / div;
            int targetY = y / div;
            wfsTiles = Collections.singletonList(new TileCoord(targetZ, targetX, targetY));
        }

        SimpleFeatureCollection sfc = null;
        for (TileCoord tile : wfsTiles) {
            SimpleFeatureCollection tileFeatures = getFeatures(id, uuid, layer, crs, grid, tile, contentProcessor);
            if (tileFeatures == null) {
                throw new ServiceRuntimeException("Failed to get features from service");
            }
            // merge z=8 tiles to create featureCollection for z=7 etc
            sfc = union(sfc, tileFeatures);
        }

        // sfc always has features for z<=8 so we need to clip to smaller tiles based on requested x,y,z
        double[] bbox = grid.getTileExtent(new TileCoord(z, x, y));
        byte[] encoded = SimpleFeaturesMVTEncoder.encodeToByteArray(sfc, layer.getName(), bbox, TILE_EXTENT, TILE_BUFFER);
        try {
            return IOHelper.gzip(encoded).toByteArray();
        } catch (IOException e) {
            throw new ServiceRuntimeException("Unexpected IOException occured");
        }
    }

    private SimpleFeatureCollection getFeatures(String id, String uuid, OskariLayer layer,
            CoordinateReferenceSystem crs, WFSTileGrid grid, TileCoord tile,
            Optional<UserLayerService> processor) throws ServiceRuntimeException {
        double[] box = grid.getTileExtent(tile);
        Envelope envelope = new Envelope(box[0], box[2], box[1], box[3]);
        Envelope bufferedEnvelope = new Envelope(envelope);
        double bufferSizePercent = (double) TILE_BUFFER / (double) TILE_EXTENT;
        double deltaX = bufferSizePercent * envelope.getWidth();
        double deltaY = bufferSizePercent * envelope.getHeight();
        bufferedEnvelope.expandBy(deltaX, deltaY);
        ReferencedEnvelope bbox = new ReferencedEnvelope(bufferedEnvelope, crs);
        return featureClient.getFeatures(id, uuid, layer, bbox, crs, processor);
    }

    public static SimpleFeatureCollection union(SimpleFeatureCollection a, SimpleFeatureCollection b) {
        if (a == null) {
            return b;
        }
        if (b == null) {
            return a;
        }
        try (SimpleFeatureIterator iterA = a.features();
                SimpleFeatureIterator iterB = b.features()) {
            if (!iterA.hasNext()) {
                return b;
            }
            if (!iterB.hasNext()) {
                return a;
            }

            Set<String> ids = new HashSet<>();
            DefaultFeatureCollection union = new DefaultFeatureCollection();

            while (iterA.hasNext()) {
                SimpleFeature f = iterA.next();
                String id = f.getID();
                if (id != null && ids.add(id)) {
                    union.add(f);
                }
            }

            while (iterB.hasNext()) {
                SimpleFeature f = iterB.next();
                String id = f.getID();
                if (id != null && ids.add(id)) {
                    union.add(f);
                }
            }

            return union;
        }
    }

}
