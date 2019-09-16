package org.oskari.print.loader;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import org.oskari.print.request.PrintLayer;
import org.oskari.print.request.PrintRequest;
import org.oskari.print.wmts.WMTSCapabilitiesCache;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.service.ServiceException;

public class AsyncImageLoader {

    public static final String GROUP_KEY = "LoadImageFromURL";

    public static Map<Integer, Future<BufferedImage>> initLayers(PrintRequest request, WMTSCapabilitiesCache wmtsCapsCache)
            throws ServiceException {
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
            switch (layer.getType()) { 
            case OskariLayer.TYPE_WMS:
                images.put(layer.getZIndex(), new CommandLoadImageWMS(layer, 
                        width, height, bbox, srsName,request.getTime()).queue());
                break;
            case OskariLayer.TYPE_WMTS:
                images.put(layer.getZIndex(), new CommandLoadImageWMTS(layer, width, height, bbox, srsName,
                        wmtsCapsCache.get(layer), request.getResolution()).queue());
                break;
            case OskariLayer.TYPE_ARCGIS93:
                images.put(layer.getZIndex(), new CommandLoadImageArcGISREST(layer,
                        width, height, bbox, srsName).queue());
                break;
            }

        }

        return images;
    }
}
