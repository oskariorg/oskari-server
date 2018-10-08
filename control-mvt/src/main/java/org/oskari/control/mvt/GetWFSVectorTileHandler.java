package org.oskari.control.mvt;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.feature.DefaultFeatureCollection;
import org.opengis.feature.simple.SimpleFeature;
import org.oskari.service.mvt.SimpleFeaturesMVTEncoder;
import org.oskari.service.mvt.wfs.TileCoord;
import org.oskari.service.mvt.wfs.WFSMetaTiles;
import org.oskari.service.mvt.wfs.WFSTileGrid;
import org.oskari.service.util.ServiceFactory;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.cache.Cache;
import fi.nls.oskari.control.ActionConstants;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.map.layer.OskariLayerService;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.ResponseHelper;

@OskariActionRoute("GetWFSVectorTile")
public class GetWFSVectorTileHandler extends ActionHandler {

    protected static final String MVT_CONTENT_TYPE = "application/vnd.mapbox-vector-tile";
    protected static final String PARAM_Z = "z";
    protected static final String PARAM_X = "x";
    protected static final String PARAM_Y = "y";

    private static final Map<String, WFSTileGrid> KNOWN_TILE_GRIDS;
    static {
        KNOWN_TILE_GRIDS = new HashMap<>();
        KNOWN_TILE_GRIDS.put("EPSG:3067", new WFSTileGrid(new double[] { -548576, 6291456, -548576 + (8192*256), 6291456 + (8192*256) }, 15));
        KNOWN_TILE_GRIDS.put("EPSG:3857", new WFSTileGrid(new double[] { -20037508.3427892, -20037508.3427892, 20037508.3427892, 20037508.3427892 }, 18));
    }

    private final Cache<byte[]> cache = new Cache<>();
    private OskariLayerService layerService;

    public void setLayerService(OskariLayerService layerService) {
        this.layerService = layerService;
    }

    @Override
    public void init() {
        if (layerService == null) {
            layerService = ServiceFactory.getMapLayerService();
        }
    }

    @Override
    public void handleAction(ActionParameters params) throws ActionException {
        int layerId = params.getRequiredParamInt(ActionConstants.PARAM_ID);
        OskariLayer layer = findLayer(layerId);

        String srs = params.getRequiredParam(ActionConstants.PARAM_SRS);
        WFSTileGrid grid = KNOWN_TILE_GRIDS.get(srs);
        if (grid == null) {
            throw new ActionParamsException("Unknown tile grid");
        }

        int z = params.getRequiredParamInt(PARAM_Z);
        int x = params.getRequiredParamInt(PARAM_X);
        int y = params.getRequiredParamInt(PARAM_Y);
        validate(grid, z, x, y);

        Double minScale = layer.getMinScale();
        Double maxScale = layer.getMaxScale();
        if (minScale != null || maxScale != null) {
            double scaleDenominator = getScaleDenominator(grid, z);
            validateScaleDenominator(scaleDenominator, minScale, maxScale);
        }

        String cacheKey = getCacheKey(layerId, srs, z, x, y);
        byte[] cached = cache.get(cacheKey);
        if (cached != null) {
            params.getResponse().addHeader("Access-Control-Allow-Origin", "*");
            if (shouldGzip(params.getRequest())) {
                params.getResponse().addHeader("Content-Encoding", "gzip");
                ResponseHelper.writeResponse(params, 200, MVT_CONTENT_TYPE, cached);
            } else {
                ByteArrayOutputStream ungzipped;
                try {
                    ungzipped = IOHelper.ungzip(cached);
                } catch (IOException e) {
                    throw new ActionException("Failed to decompress cached response", e);
                }
                ResponseHelper.writeResponse(params, 200, MVT_CONTENT_TYPE, ungzipped);
            }
            return;
        }

        // Find nearest higher resolution
        int targetZ = grid.getZForResolution(8, -1);

        List<TileCoord> wfsTiles;
        int dz = z - targetZ;
        if (dz < 0) {
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
            wfsTiles = Collections.singletonList(new TileCoord(z, x, y));
        } else {
            int div = (int) Math.pow(2, dz);
            int targetX = x / div;
            int targetY = y / div;
            wfsTiles = Collections.singletonList(new TileCoord(targetZ, targetX, targetY));
        }

        SimpleFeatureCollection sfc = null;
        for (TileCoord tile : wfsTiles) {
            SimpleFeatureCollection tileFeatures = WFSMetaTiles.getFeatures(layer, srs, grid, tile);
            if (tileFeatures == null) {
                throw new ActionException("Failed to get features from service");
            }
            sfc = sfc == null ? tileFeatures : union(sfc, tileFeatures);
        }

        double[] bbox = grid.getTileExtent(new TileCoord(z, x, y));
        byte[] encoded = SimpleFeaturesMVTEncoder.encodeToByteArray(sfc, layer.getName(), bbox, 4096, 256);

        byte[] gzip = null;
        try {
            gzip = IOHelper.gzip(encoded).toByteArray();
        } catch (IOException e) {
            throw new ActionException("Unexpected exception occured, try again", e);
        }

        cache.put(cacheKey, gzip);

        byte[] response;
        if (shouldGzip(params.getRequest())) {
            params.getResponse().addHeader("Content-Encoding", "gzip");
            response = gzip;
        } else {
            response = encoded;
        }
        params.getResponse().addHeader("Access-Control-Allow-Origin", "*");
        ResponseHelper.writeResponse(params, 200, MVT_CONTENT_TYPE, response);
    }

    private OskariLayer findLayer(int layerId) throws ActionParamsException {
        OskariLayer layer = layerService.find(layerId);
        if (layer == null) {
            throw new ActionParamsException("Unknown layerId");
        }
        if (!OskariLayer.TYPE_WFS.equals(layer.getType())) {
            throw new ActionParamsException("Specified layer is not a WFS layer");
        }
        return layer;
    }

    private void validate(WFSTileGrid grid, int z, int x, int y) throws ActionParamsException {
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
        int matrixWidthHeight = WFSTileGrid.getMatrixSize(z);
        if (x >= matrixWidthHeight) {
            throw new ActionParamsException("x must be < " + matrixWidthHeight + " (z = " + z + ")");
        }
        if (y >= matrixWidthHeight) {
            throw new ActionParamsException("y must be < " + matrixWidthHeight + " (z = " + z + ")");
        }
    }

    private double getScaleDenominator(WFSTileGrid grid, int z) {
        double resolution = grid.getResolutionForZ(z);
        return getScaleDenominator(resolution);
    }

    protected static double getScaleDenominator(double resolution) {
        return resolution * 1000 / 0.28; // OGC WMTS 0.28 mm/px
    }

    private void validateScaleDenominator(double scaleDenominator, Double minScale, Double maxScale)
            throws ActionParamsException {
        if (minScale != null) {
            if (scaleDenominator > minScale) {
                // Bigger denominator <=> Smaller scale
                throw new ActionParamsException("z too low for layer");
            }
        }
        if (maxScale != null) {
            if (scaleDenominator < maxScale) {
                // Smaller denominator <=> Bigger scale
                throw new ActionParamsException("z too high for layer");
            }
        }
    }

    private String getCacheKey(int layerId, String srs, int z, int x, int y) {
        return "WFS_" + layerId + "_" + srs + "_" + z + "_" + x + "_" + y;
    }

    private boolean shouldGzip(HttpServletRequest request) {
        String acceptEncoding = request.getHeader("Accept-Encoding");
        if (acceptEncoding == null) {
            return false;
        }
        return acceptEncoding.contains("gzip");
    }

    public static SimpleFeatureCollection union(SimpleFeatureCollection a, SimpleFeatureCollection b) {
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
