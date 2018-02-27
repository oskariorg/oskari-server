package org.oskari.print.loader;

import java.awt.image.BufferedImage;

import org.oskari.print.request.PrintLayer;
import org.oskari.print.util.GetMapBuilder;

/**
 * HystrixCommand that loads BufferedImage from WMS
 */
public class CommandLoadImageWMS extends CommandLoadImageBase {

    private static final String FORMAT = "image/png";

    private final PrintLayer layer;
    private final int width;
    private final int height;
    private final double[] bbox;
    private final String srsName;

    public CommandLoadImageWMS(PrintLayer layer,
                               int width,
                               int height,
                               double[] bbox,
                               String srsName) {
        super(Integer.toString(layer.getId()));
        this.layer = layer;
        this.width = width;
        this.height = height;
        this.bbox = bbox;
        this.srsName = srsName;
    }

    @Override
    public BufferedImage run() throws Exception {
        final String request = new GetMapBuilder().endPoint(layer.getUrl())
                .version(layer.getVersion())
                .layer(layer.getName())
                .bbox(bbox)
                .crs(srsName)
                .width(width)
                .height(height)
                .format(FORMAT)
                .transparent(true)
                .toKVP();
        
        return CommandLoadImageFromURL.load(request);
    }

    @Override
    public BufferedImage getFallback() {
        return null;
    }

}
