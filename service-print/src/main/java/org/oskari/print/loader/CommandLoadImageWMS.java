package org.oskari.print.loader;

import java.awt.image.BufferedImage;

import org.oskari.print.request.PrintLayer;
import org.oskari.print.util.GetMapBuilder;

public class CommandLoadImageWMS {

    private static final String FORMAT = "image/png";

    public static BufferedImage loadImage(PrintLayer layer,
            int width,
            int height,
            double[] bbox,
            String srsName,
            String time) {
        final String request = new GetMapBuilder().endPoint(layer.getUrl())
                .version(layer.getVersion())
                .layer(layer.getName(), layer.getStyle())
                .bbox(bbox)
                .crs(srsName)
                .width(width)
                .height(height)
                .format(FORMAT)
                .transparent(true)
                .time(time)
                .toKVP();
        return AsyncImageLoader.loadImageFromURL(request, layer.getUsername(), layer.getPassword());
    }
}
