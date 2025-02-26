package org.oskari.print.loader;

import java.awt.image.BufferedImage;

import org.oskari.print.request.PrintLayer;
import org.oskari.print.util.ArcGISMapExportBuilder;

public class CommandLoadImageArcGISREST {
    public static BufferedImage loadImage(PrintLayer layer,
            int width,
            int height,
            double[] bbox,
            String srsName) {
        final String request = new ArcGISMapExportBuilder()
                .endPoint(layer.getUrl())
                .layer(layer.getName())
                .bbox(bbox)
                .crs(srsName)
                .width(width)
                .height(height)
                .transparent(true)
                .build();
        return PrintLoader.loadImageFromURL(request, layer.getUsername(), layer.getPassword());
    }
}
