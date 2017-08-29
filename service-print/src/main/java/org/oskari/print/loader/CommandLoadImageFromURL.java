package org.oskari.print.loader;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

/**
 * HystrixCommand that loads BufferedImage from URL
 * Retries up to 3 times
 */
public class CommandLoadImageFromURL extends HystrixCommand<BufferedImage> {

    private static final Logger LOG = LogFactory.getLogger(CommandLoadImageWMS.class);
    private static final int RETRY_COUNT = 3;

    private final String uri;

    protected CommandLoadImageFromURL(HystrixCommandGroupKey group, String uri) {
        super(group);
        this.uri = uri;
    }

    @Override
    public BufferedImage run() throws Exception {
        LOG.debug(uri);
        final URL url = new URL(uri);
        for (int i = 0; i < RETRY_COUNT; i++) {
            try {
                return ImageIO.read(url);
            } catch (IOException e) {
                LOG.warn(e);
            }
        }
        throw new RuntimeException("Failed to load image from " + url.toString());
    }

}
