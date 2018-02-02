package org.oskari.print.loader;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;

/**
 * HystrixCommand that loads BufferedImage from URL
 * Retries up to 3 times
 */
public class CommandLoadImageFromURL extends CommandLoadImageBase {

    private static final Logger LOG = LogFactory.getLogger(CommandLoadImageFromURL.class);
    private static final int RETRY_COUNT = 3;
    
    private final String uri;

    protected CommandLoadImageFromURL(String commandName, String uri) {
        super(commandName);
        this.uri = uri;
    }

    @Override
    public BufferedImage run() throws Exception {
        LOG.info(uri);
        URL url = new URL(uri);
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
        return null;
    }

}