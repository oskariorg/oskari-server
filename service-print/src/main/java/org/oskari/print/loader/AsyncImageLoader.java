package org.oskari.print.loader;

import fi.nls.oskari.service.ServiceException;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import org.oskari.print.request.PrintLayer;
import org.oskari.print.request.PrintRequest;
import org.oskari.print.wmts.WMTSCapabilitiesCache;
import fi.nls.oskari.domain.map.OskariLayer;

public class AsyncImageLoader {

    public static final String GROUP_KEY = "LoadImageFromURL";

    public static List<Future<BufferedImage>> initLayers(PrintRequest request, WMTSCapabilitiesCache wmtsCapsCache)
            throws ServiceException {
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
                images.add(new CommandLoadImageWMS(layer, 
                        width, height, bbox, srsName).queue());
                break;
            case OskariLayer.TYPE_WMTS:
                images.add(new CommandLoadImageWMTS(layer, width, height, bbox, srsName,
                        wmtsCapsCache.get(layer), request.getResolution()).queue());
                break;
            case OskariLayer.TYPE_WFS:
                images.add(new CommandLoadImageWFS(layer, width, height, bbox).queue());
                break;
            default:
                throw new IllegalArgumentException("Invalid layer type!");
            }
        }

        return images;
    }

    public static double[] getBoundingBox(double e, double n, double resolution, int width, int height) {
        double halfResolution = resolution / 2;

        double widthHalf = width * halfResolution;
        double heightHalf = height * halfResolution;

        return new double[] {
                e - widthHalf,
                n - heightHalf,
                e + widthHalf,
                n + heightHalf
        };
    }

}
