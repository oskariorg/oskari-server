package org.oskari.print.loader;

import com.netflix.hystrix.HystrixCommand;
import org.oskari.print.PrintLayer;
import org.oskari.util.GetMapBuilder;

import java.awt.image.BufferedImage;

/**
 * HystrixCommand that loads BufferedImage from WMS
 */
public class CommandLoadImageWMS extends HystrixCommand<BufferedImage> {

    private static final String FORMAT = "image/png";

    private final PrintLayer layer;
    private final int width;
    private final int height;
    private final double[] bbox;
    private final String srsName;

    public CommandLoadImageWMS(Setter config, 
            final PrintLayer layer, 
            final int width, 
            final int height,
            final double[] bbox,
            final String srsName) {
        super(config);
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
        return new CommandLoadImageFromURL(commandGroup, request).execute();
    }

}
