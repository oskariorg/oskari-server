package org.oskari.print.loader;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import org.oskari.print.PrintLayer;
import org.oskari.print.PrintRequest;
import org.oskari.print.wmts.TileMatrixSetCache;

import com.netflix.hystrix.HystrixCommand.Setter;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.wmts.domain.TileMatrix;
import fi.nls.oskari.wmts.domain.TileMatrixSet;

public class AsyncImageLoader {

    public static List<Future<BufferedImage>> initLayers(PrintRequest request, Setter config) {
        final List<Future<BufferedImage>> images = new ArrayList<>();

        final List<PrintLayer> requestedLayers = request.getLayers();
        if (requestedLayers == null) {
            return images;
        }

        final int width = request.getWidth();
        final int height = request.getHeight();
        final double[] bbox = getBoundingBox(
                request.getEast(), request.getNorth(), 
                request.getResolution(), width, height);
        final String srsName = request.getSrsName();

        for (PrintLayer layer : requestedLayers) {
            switch (layer.getType()) { 
            case OskariLayer.TYPE_WMS:
                images.add(new CommandLoadImageWMS(config, layer,
                        width, height, bbox, srsName).queue());
                break;
            case OskariLayer.TYPE_WMTS:
                int zoom = request.getZoomLevel();
                TileMatrix tileMatrix = findTileMatrix(layer, zoom);
                images.add(new CommandLoadImageWMTS(config, layer,
                        width, height, bbox, 
                        tileMatrix, request.getMetersPerUnit()).queue());
                break;
            case OskariLayer.TYPE_WFS:
                images.add(new CommandLoadImageWFS(config, layer, 
                        width, height, bbox).queue());
                break;
            default:
                throw new IllegalArgumentException("Invalid layer type!");
            }
        }

        return images;
    }


    public static TileMatrix findTileMatrix(PrintLayer layer, int zoomLevel) {
        TileMatrixSet set = TileMatrixSetCache.get(layer);
        String id = Integer.toString(zoomLevel);
        return set.getTileMatrixMap().get(id);
    }


    public static double[] getBoundingBox(double east, double north, double resolution, int width, int height) {
        double halfResolution = resolution / 2;

        double widthHalf = width * halfResolution;
        double heightHalf = height * halfResolution;

        return new double[] {
                east - widthHalf,
                north - heightHalf,
                east + widthHalf,
                north + heightHalf
        };
    }

}
