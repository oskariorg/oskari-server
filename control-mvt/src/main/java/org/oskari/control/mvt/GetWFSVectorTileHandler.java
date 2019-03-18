package org.oskari.control.mvt;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.Filter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.oskari.service.mvt.SimpleFeaturesMVTEncoder;
import org.oskari.service.mvt.TileCoord;
import org.oskari.service.mvt.WFSTileGrid;
import org.oskari.service.util.ServiceFactory;
import org.oskari.service.wfs.client.CachingWFSClient;

import com.vividsolutions.jts.geom.Envelope;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.cache.ComputeOnceCache;
import fi.nls.oskari.control.ActionConstants;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.control.feature.MyPlacesWFSHelper;
import fi.nls.oskari.control.layer.PermissionHelper;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.service.ServiceRuntimeException;
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

    private final ComputeOnceCache<byte[]> tileCache = new ComputeOnceCache<>(256, TimeUnit.MINUTES.toMillis(5));

    private PermissionHelper permissionHelper;
    private MyPlacesWFSHelper myPlacesHelper;
    private CachingWFSClient wfsClient;

    @Override
    public void init() {
        this.permissionHelper = new PermissionHelper(
                ServiceFactory.getMapLayerService(),
                ServiceFactory.getPermissionsService());
        this.myPlacesHelper = new MyPlacesWFSHelper();
        this.wfsClient = new CachingWFSClient();
    }

    @Override
    public void handleAction(ActionParameters params) throws ActionException {
        final String id = params.getRequiredParam(ActionConstants.PARAM_ID);
        final String srs = params.getRequiredParam(ActionConstants.PARAM_SRS);
        final int z = params.getRequiredParamInt(PARAM_Z);
        final int x = params.getRequiredParamInt(PARAM_X);
        final int y = params.getRequiredParamInt(PARAM_Y);

        final OskariLayer layer = findLayer(id, params.getUser());
        final String uuid = params.getUser().getUuid();
        final WFSTileGrid grid = KNOWN_TILE_GRIDS.get(srs);
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
            resp = tileCache.get(cacheKey, __ -> createTile(id, uuid, layer, crs, grid, z, x, y));
        } catch (ServiceRuntimeException e) {
            throw new ActionException(e.getMessage());
        }
        params.getResponse().addHeader("Access-Control-Allow-Origin", "*");
        params.getResponse().addHeader("Content-Encoding", "gzip");
        ResponseHelper.writeResponse(params, 200, MVT_CONTENT_TYPE, resp);
    }

    private OskariLayer findLayer(String id, User user) throws ActionException {
        int layerId = getLayerId(id);
        OskariLayer layer = permissionHelper.getLayer(layerId, user);
        if (!OskariLayer.TYPE_WFS.equals(layer.getType())) {
            throw new ActionParamsException("Specified layer is not a WFS layer");
        }
        return layer;
    }

    private int getLayerId(String id) throws ActionParamsException {
        if (myPlacesHelper.isMyPlacesLayer(id)) {
            return myPlacesHelper.getMyPlacesLayerId();
        }
        try {
            return Integer.parseInt(id);
        } catch (NumberFormatException e) {
            throw new ActionParamsException("Invalid id");
        }
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
            WFSTileGrid grid, int z, int x, int y) throws ServiceRuntimeException {
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
            SimpleFeatureCollection tileFeatures = getFeatures(id, uuid, layer, crs, grid, tile);
            if (tileFeatures == null) {
                throw new ServiceRuntimeException("Failed to get features from service");
            }
            sfc = union(sfc, tileFeatures);
        }

        double[] bbox = grid.getTileExtent(new TileCoord(z, x, y));
        byte[] encoded = SimpleFeaturesMVTEncoder.encodeToByteArray(sfc, layer.getName(), bbox, 4096, 256);
        try {
            return IOHelper.gzip(encoded).toByteArray();
        } catch (IOException e) {
            throw new ServiceRuntimeException("Unexpected IOException occured");
        }
    }

    private SimpleFeatureCollection getFeatures(String id, String uuid, OskariLayer layer, CoordinateReferenceSystem crs,
            WFSTileGrid grid, TileCoord tile) {
        String endPoint = layer.getUrl();
        String version = layer.getVersion();
        String typeName = layer.getName();
        String username = layer.getUsername();
        String password = layer.getPassword();
        double[] box = grid.getTileExtent(tile);
        Envelope envelope = new Envelope(box[0], box[2], box[1], box[3]);
        ReferencedEnvelope bbox = new ReferencedEnvelope(envelope, crs);
        int maxFeatures = 10000;

        Filter filter = null;
        if (myPlacesHelper.isMyPlacesLayer(layer)) {
            int categoryId = myPlacesHelper.getCategoryId(id);
            filter = myPlacesHelper.getFilter(categoryId, uuid, bbox);
        }

        return wfsClient.tryGetFeatures(endPoint, version, username, password, typeName, bbox, crs, maxFeatures, filter);
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
