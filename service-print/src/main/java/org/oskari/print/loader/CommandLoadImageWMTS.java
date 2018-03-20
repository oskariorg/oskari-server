package org.oskari.print.loader;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;

import org.oskari.print.request.PrintLayer;
import org.oskari.print.util.Units;
import org.oskari.print.wmts.GetTileRequestBuilder;
import org.oskari.print.wmts.GetTileRequestBuilderKVP;
import org.oskari.print.wmts.GetTileRequestBuilderREST;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.geometry.ProjectionHelper;
import fi.nls.oskari.wmts.domain.ResourceUrl;
import fi.nls.oskari.wmts.domain.TileMatrix;
import fi.nls.oskari.wmts.domain.TileMatrixSet;
import fi.nls.oskari.wmts.domain.WMTSCapabilities;
import fi.nls.oskari.wmts.domain.WMTSCapabilitiesLayer;

/**
 * HystrixCommand that loads tiles from a WMTS service
 * and combines them to a single BufferedImage
 */
public class CommandLoadImageWMTS extends CommandLoadImageBase {

    private static final Logger LOG = LogFactory.getLogger(CommandLoadImageWMTS.class);
    private static final double EPSILON = 0.015625;

    private static final String[] FORMAT_TO_USE = new String[] {
            "image/png",
            "image/png8",
            "image/jpeg"
    };

    private final PrintLayer layer;
    private final int width;
    private final int height;
    private final double[] bbox;
    private final double resolution;
    private final String srs;
    private final WMTSCapabilities capabilities;

    public CommandLoadImageWMTS(PrintLayer layer,
            int width,
            int height,
            double[] bbox,
            String srs,
            WMTSCapabilities capabilities,
            double resolution) {
        // Use Layers id as commandName
        super(Integer.toString(layer.getId()));
        this.layer = layer;
        this.width = width;
        this.height = height;
        this.bbox = bbox;
        this.srs = srs;
        this.capabilities = capabilities;
        this.resolution = resolution;
    }

    @Override
    public BufferedImage run() throws Exception {
        WMTSCapabilitiesLayer layerCapabilities = getLayerCapabilities();
        TileMatrixSet tms = getTileMatrixSet();
        TileMatrix tm = getTileMatrix(tms);

        int tileWidth = tm.getTileWidth();
        int tileHeight = tm.getTileHeight();

        double[] topLeft = tm.getTopLeftCorner();
        double minX = topLeft[0];
        double maxY = topLeft[1];

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

        List<Future<BufferedImage>> futureTiles =
                new ArrayList<Future<BufferedImage>>(countTileRows * countTileCols);

        ResourceUrl tileResourceUrl = layerCapabilities.getResourceUrlByType("tile");
        GetTileRequestBuilder requestBuilder;
        if (tileResourceUrl != null) {
            requestBuilder = getTileRequestBuilderREST(tms.getId(), tm.getId(), tileResourceUrl);
        } else {
            requestBuilder = getTileRequestBuilderKVP(tms.getId(), tm.getId(), layerCapabilities);
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
                futureTiles.add(new CommandLoadImageFromURL(
                        Integer.toString(layer.getId()), uri).queue());
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
                BufferedImage tile = futureTile.get();
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

    private WMTSCapabilitiesLayer getLayerCapabilities() throws IllegalArgumentException {
        WMTSCapabilitiesLayer layerCapabilities = capabilities.getLayer(layer.getName());
        if (layerCapabilities != null) {
            return layerCapabilities;
        }
        throw new IllegalArgumentException("Could not find layer from Capabilities");
    }

    private TileMatrix getTileMatrix(TileMatrixSet tms) throws IllegalArgumentException {
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

    private TileMatrixSet getTileMatrixSet() throws IllegalArgumentException {
        for (TileMatrixSet tms : capabilities.getTileMatrixSets()) {
            if (srs.equals(ProjectionHelper.shortSyntaxEpsg(tms.getCrs()))) {
                return tms;
            }
        }
        throw new IllegalArgumentException("Could not find TileMatrixSet for the requested crs");
    }

    private GetTileRequestBuilder getTileRequestBuilderREST(String tileMatrixSetId, String tileMatrixId, ResourceUrl tileResourceUrl) {
        String template = tileResourceUrl.getTemplate();
        return new GetTileRequestBuilderREST(template)
                .layer(layer.getName())
                .style(layer.getStyle())
                .tileMatrixSet(tileMatrixSetId)
                .tileMatrix(tileMatrixId);
    }

    private GetTileRequestBuilder getTileRequestBuilderKVP(String tileMatrixSetId, String tileMatrixId, WMTSCapabilitiesLayer layerCapabilities) {
        String format = getFormat(layerCapabilities.getFormats());
        return new GetTileRequestBuilderKVP().endPoint(layer.getUrl())
                .layer(layer.getName())
                .style(layer.getStyle())
                .tileMatrixSet(tileMatrixSetId)
                .tileMatrix(tileMatrixId)
                .format(format);
    }

    private String getFormat(Set<String> layersFormats) {
        for (String format : FORMAT_TO_USE) {
            if (layersFormats.contains(format)) {
                return format;
            }
        }
        throw new RuntimeException("Layer doesn't support any of the supported formats");
    }

}
