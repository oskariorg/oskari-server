package org.oskari.print.loader;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import org.oskari.print.request.PrintLayer;
import org.oskari.print.util.Units;
import org.oskari.print.wmts.GetTileBuilderREST;

import fi.nls.oskari.wmts.domain.TileMatrix;

/**
 * HystrixCommand that loads tiles from a WMTS service
 * and combines them to a single BufferedImage
 */
public class CommandLoadImageWMTS extends CommandLoadImageBase {

    private final PrintLayer layer;
    private final int width;
    private final int height;
    private final TileMatrix matrix;
    private final double metersPerUnit;
    private final double[] bbox;

    public CommandLoadImageWMTS(PrintLayer layer,
            int width,
            int height,
            double[] bbox,
            TileMatrix matrix,
            double metersPerUnit) {
        this.layer = layer;
        this.width = width;
        this.height = height;
        this.bbox = bbox;
        this.matrix = matrix;
        this.metersPerUnit = metersPerUnit;
    }

    @Override
    public BufferedImage run() throws Exception {
        int tileWidth = matrix.getTileWidth();
        int tileHeight = matrix.getTileHeight();

        double[] topLeft = matrix.getTopLeftCorner();
        double minX = topLeft[0];
        double maxY = topLeft[1];

        double pixelSpan = getPixelSpan(matrix.getScaleDenominator(), metersPerUnit);

        // Round to the nearest px
        long minXPx = Math.round((bbox[0] - minX) / pixelSpan);
        long maxYPx = Math.round((maxY - bbox[3]) / pixelSpan);

        int minTileCol = (int) (minXPx / tileWidth);
        int minTileRow = (int) (maxYPx / tileHeight);

        double minTileX = minX + minTileCol * tileWidth * pixelSpan;
        double maxTileY = maxY - minTileRow * tileHeight * pixelSpan;

        double offsetX = bbox[0] - minTileX;
        double offsetY = maxTileY - bbox[3];

        int offsetXPixels = (int) Math.round((offsetX / pixelSpan));
        int offsetYPixels = (int) Math.round((offsetY / pixelSpan));

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

        GetTileBuilderREST requestBuilder = new GetTileBuilderREST(layer.getUrl())
        .layer(layer.getName())
        .style(layer.getStyle())
        .tileMatrixSet(layer.getTileMatrixSet())
        .tileMatrix(matrix.getId());

        for (int row = 0; row < countTileRows; row++) {
            requestBuilder.tileRow(minTileRow + row);
            for (int col = 0; col < countTileCols; col++) {
                requestBuilder.tileCol(minTileCol + col);
                String uri = requestBuilder.build();
                futureTiles.add(new CommandLoadImageFromURL(uri).queue());
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
                BufferedImage tile = futureTile.get();
                g2d.drawImage(tile, x, y, null);
            }
        }

        g2d.dispose();
        return bi;
    }

    public static double getPixelSpan(double scaleDenominator, double metersPerUnit) {
        return scaleDenominator * Units.OGC_PIXEL_SIZE_METRE / metersPerUnit;
    }

}
