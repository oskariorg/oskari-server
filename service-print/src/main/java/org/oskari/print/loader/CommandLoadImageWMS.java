package org.oskari.print.loader;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

import org.oskari.print.request.PrintLayer;
import org.oskari.print.util.GetMapBuilder;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;

/**
 * HystrixCommand that loads BufferedImage from WMS
 */
public class CommandLoadImageWMS extends CommandLoadImageBase {

    private static final Logger LOG = LogFactory.getLogger(CommandLoadImageFromURL.class);
    private static final int RETRY_COUNT = 3;
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
        super(layer.getId());
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
        
        LOG.info(request);
        URL url = new URL(request);
        for (int i = 0; i < RETRY_COUNT - 1; i++) {
            try {
                return ImageIO.read(url);
            } catch (IOException e) {
                LOG.warn(e);
            }
        }
        return ImageIO.read(url);
    }

    @Override
    public BufferedImage getFallback() {
        // Return fully transparent image as fallback
        return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    }

}