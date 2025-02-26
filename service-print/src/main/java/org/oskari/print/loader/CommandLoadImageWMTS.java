package org.oskari.print.loader;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.function.Supplier;

import fi.nls.oskari.domain.map.OskariLayer;
import org.oskari.capabilities.CapabilitiesService;
import org.oskari.capabilities.ogc.LayerCapabilitiesWMTS;
import org.oskari.capabilities.ogc.wmts.*;
import org.oskari.print.request.PrintLayer;
import org.oskari.print.util.Units;
import org.oskari.print.wmts.GetTileRequestBuilder;
import org.oskari.print.wmts.GetTileRequestBuilderKVP;
import org.oskari.print.wmts.GetTileRequestBuilderREST;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.geometry.ProjectionHelper;

import org.geotools.referencing.CRS;
import org.json.JSONObject;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;

public class CommandLoadImageWMTS {

    private static final Logger LOG = LogFactory.getLogger(CommandLoadImageWMTS.class);
    private static final double EPSILON = 0.015625;

    private static final String[] FORMAT_TO_USE = new String[]{
        "image/png",
        "image/png8",
        "image/jpeg"
    };

    public static BufferedImage loadImage(PrintLayer layer,
            int width,
            int height,
            double[] bbox,
            String srs,
            double resolution,
            PrintLoader loader) {
        LayerCapabilitiesWMTS caps = getLayerCapabilities(layer);
        TileMatrixSet tms = getTileMatrixSet(caps.getTileMatrixLinks(), layer, srs);
        TileMatrix tm = getTileMatrix(tms, resolution);

        int tileWidth = tm.getTileWidth();
        int tileHeight = tm.getTileHeight();

        double[] topLeft = tm.getTopLeftCorner();
        double minX = topLeft[0];
        double maxY = topLeft[1];
        if (isAxisOrderNE(tms.getCrs())) {
            // From the WMTS spec, topLeftCorner:
            // Position in CRS coordinates of the top-left corner of this tile matrix
            // Ordered sequence of double values
            // CRS shall be inherited from the supportedCRS parameter of the parent TileMatrixSet
            // The order of these axes, shall be as specified by the supportedCRS
            minX = topLeft[1];
            maxY = topLeft[0];
        }

        // Round to the nearest px
        long minXPx = Math.round((bbox[0] - minX) / resolution);
        long maxYPx = Math.round((maxY - bbox[3]) / resolution);

        int minTileCol = (int) (minXPx / tileWidth);
        int minTileRow = (int) (maxYPx / tileHeight);

        double minTileX = minX + minTileCol * tileWidth * resolution;
        double maxTileY = maxY - minTileRow * tileHeight * resolution;

        double offsetX = bbox[0] - minTileX;
        double offsetY = maxTileY - bbox[3];

        int offsetXPixels = (int) Math.round((offsetX / resolution));
        int offsetYPixels = (int) Math.round((offsetY / resolution));

        int countTileCols = 1 + (width + offsetXPixels) / tileWidth;
        int countTileRows = 1 + (height + offsetYPixels) / tileHeight;

        // If the tile happens to fit perfectly don't fetch unnecessary tiles
        if (offsetXPixels == 0 && width % tileWidth == 0) {
            countTileCols--;
        }
        if (offsetYPixels == 0 && height % tileHeight == 0) {
            countTileRows--;
        }

        List<Future<BufferedImage>> futureTiles
                = new ArrayList<>(countTileRows * countTileCols);
        ResourceUrl tileResourceUrl = caps.getResourceUrl("tile");
        GetTileRequestBuilder requestBuilder;
        if (tileResourceUrl != null) {
            requestBuilder = getTileRequestBuilderREST(tms.getId(), tm.getId(), tileResourceUrl, layer);
        } else {
            requestBuilder = getTileRequestBuilderKVP(tms.getId(), tm.getId(), caps.getFormats(), layer);
        }

        for (int row = 0; row < countTileRows; row++) {
            int r = minTileRow + row;
            if (r < 0 || r >= tm.getMatrixHeight()) {
                // Don't request tiles outside of TileMatrix limits
                // Add nulls instead so that the calculations are easier
                for (int col = 0; col < countTileCols; col++) {
                    futureTiles.add(null);
                }
                continue;
            }
            requestBuilder.tileRow(r);
            for (int col = 0; col < countTileCols; col++) {
                int c = minTileCol + col;
                if (c < 0 || c >= tm.getMatrixWidth()) {
                    // Don't request tiles outside of TileMatrix limits
                    // Add nulls so that the calculations are easier
                    futureTiles.add(null);
                    continue;
                }
                requestBuilder.tileCol(c);
                String uri = requestBuilder.build();
                Supplier<BufferedImage> supplier = () -> PrintLoader.loadImageFromURL(uri, layer.getUsername(), layer.getPassword());
                String commandKey = Integer.toString(layer.getId());
                futureTiles.add(loader.runImageSupplier(commandKey, supplier));
            }
        }

        BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = bi.createGraphics();

        int tileIndex = 0;
        for (int row = 0; row < countTileRows; row++) {
            int y = tileHeight * row - offsetYPixels;
            for (int col = 0; col < countTileCols; col++) {
                int x = tileWidth * col - offsetXPixels;
                Future<BufferedImage> futureTile = futureTiles.get(tileIndex++);
                if (futureTile == null) {
                    // futureTile is null if the the tile is outside TileMatrix limits
                    continue;
                }
                BufferedImage tile = null;
                try {
                    tile = futureTile.get();
                } catch (Exception ignored) {}
                if (tile != null) {
                    // tile is null if something goes wrong
                    // but we don't want to cancel the whole request
                    g2d.drawImage(tile, x, y, null);
                }
            }
        }

        g2d.dispose();
        return bi;
    }

    private static boolean isAxisOrderNE(String srs) {
        try {
            CoordinateReferenceSystem crs = CRS.decode(srs);
            return ProjectionHelper.isFirstAxisNorth(crs);
            // return CRS.getAxisOrder(crs) == CRS.AxisOrder.NORTH_EAST;
        } catch (Exception e) {
            LOG.info("Failed to decode crs from: " + srs);
            return false;
        }
    }

    private static LayerCapabilitiesWMTS getLayerCapabilities(PrintLayer layer) throws IllegalArgumentException {
        OskariLayer oskariLayer = layer.getOskariLayer();
        if (oskariLayer != null) {
            JSONObject capabilies = layer.getOskariLayer().getCapabilities();
            if (capabilies != null) {
                return CapabilitiesService.fromJSON(layer.getOskariLayer().getCapabilities().toString(), OskariLayer.TYPE_WMTS);
            }
        }
        throw new IllegalArgumentException("Could not find layer from Capabilities");
    }

    private static TileMatrix getTileMatrix(TileMatrixSet tms, double resolution) throws IllegalArgumentException {
        double wantedScale = resolution / Units.OGC_PIXEL_SIZE_METRE;
        for (TileMatrix matrix : tms.getTileMatrixMap().values()) {
            double scaleDenominator = matrix.getScaleDenominator();
            LOG.debug("Comparing scaleDenominators, wanted:", wantedScale,
                    "current:", scaleDenominator);
            if (Math.abs(wantedScale - matrix.getScaleDenominator()) < EPSILON) {
                return matrix;
            }
        }
        throw new IllegalArgumentException(String.format(
                "Could not find TileMatrix with scaleDenominator: %f", wantedScale));
    }

    private static TileMatrixSet getTileMatrixSet(List<TileMatrixLink> tileMatrixLinks, PrintLayer layer, String srs) throws IllegalArgumentException {
        List<TileMatrixSet> possibleTileMatrixSets = new ArrayList<>();

        for (TileMatrixLink link : tileMatrixLinks) {
            TileMatrixSet tms = link.getTileMatrixSet();
            if (srs.equals(ProjectionHelper.shortSyntaxEpsg(tms.getCrs()))) {
                possibleTileMatrixSets.add(tms);
            }
        }

        if (possibleTileMatrixSets.isEmpty()) {
            throw new IllegalArgumentException("Could not find TileMatrixSet for the requested crs");
        }

        if (possibleTileMatrixSets.size() == 1) {
            return possibleTileMatrixSets.get(0);
        }
        return determineTileMatrixSetToUse(possibleTileMatrixSets, layer, srs);
    }

    private static TileMatrixSet determineTileMatrixSetToUse(List<TileMatrixSet> possibleTileMatrixSets, PrintLayer layer, String srs) throws IllegalArgumentException {
        JSONObject useThisTileMatrixSetInstead = layer.getOskariLayer().getAttributes().optJSONObject("preferredTileMatrix");
        if (useThisTileMatrixSetInstead == null) {
            return possibleTileMatrixSets.get(0);
        }
        
        String id = useThisTileMatrixSetInstead.optString(srs);
        if (id == null) {
            return possibleTileMatrixSets.get(0);
        }
        for (TileMatrixSet tms : possibleTileMatrixSets) {
            if (tms.getId().equals(id)) {
                return tms;
            }
        }
        throw new IllegalArgumentException("Could not find TileMatrixSet with id " + id + " for layer " + layer.getId());
    }

    private static GetTileRequestBuilder getTileRequestBuilderREST(String tileMatrixSetId, String tileMatrixId, ResourceUrl tileResourceUrl, PrintLayer layer) {
        String template = tileResourceUrl.getTemplate();
        return new GetTileRequestBuilderREST(template)
                .layer(layer.getName())
                .style(layer.getStyle())
                .tileMatrixSet(tileMatrixSetId)
                .tileMatrix(tileMatrixId);
    }

    private static GetTileRequestBuilder getTileRequestBuilderKVP(String tileMatrixSetId, String tileMatrixId, Set<String> formats, PrintLayer layer) {
        String format = getFormat(formats);
        return new GetTileRequestBuilderKVP().endPoint(layer.getUrl())
                .layer(layer.getName())
                .style(layer.getStyle())
                .tileMatrixSet(tileMatrixSetId)
                .tileMatrix(tileMatrixId)
                .format(format);
    }

    private static String getFormat(Set<String> layersFormats) {
        for (String format : FORMAT_TO_USE) {
            if (layersFormats.contains(format)) {
                return format;
            }
        }
        throw new RuntimeException("Layer doesn't support any of the supported formats");
    }
    protected BufferedImage getFallback() {
        return null;
    }
}
