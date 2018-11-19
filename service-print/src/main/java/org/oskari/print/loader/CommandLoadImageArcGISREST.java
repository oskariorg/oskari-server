package org.oskari.print.loader;

import java.awt.image.BufferedImage;

import org.oskari.print.request.PrintLayer;
import org.oskari.print.util.ArcGISMapExportBuilder;

/**
 * HystrixCommand that loads BufferedImage from ArcGIS REST API
 */
public class CommandLoadImageArcGISREST extends CommandLoadImageBase {

    private final PrintLayer layer;
    private final int width;
    private final int height;
    private final double[] bbox;
    private final String srsName;

    public CommandLoadImageArcGISREST(PrintLayer layer,
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
        final String request = new ArcGISMapExportBuilder()
                .endPoint(layer.getUrl())
                .layer(layer.getName())
                .bbox(bbox)
                .crs(srsName)
                .width(width)
                .height(height)
                .transparent(true)
                .build();

        return CommandLoadImageFromURL.load(request, layer.getUsername(), layer.getPassword());
    }

    @Override
    public BufferedImage getFallback() {
        return null;
    }

}
