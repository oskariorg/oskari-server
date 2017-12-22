package org.oskari.print.loader;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixThreadPoolProperties;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.PropertyUtil;

/**
 * HystrixCommand that loads BufferedImage from URL
 * Retries up to 3 times
 */
public class CommandLoadImageFromURL extends HystrixCommand<BufferedImage> {

    private static final String GROUP_KEY = "LoadImageFromURL";

    private static final Logger LOG = LogFactory.getLogger(CommandLoadImageFromURL.class);
    private static final int RETRY_COUNT = 3;

    private static final Setter SETTER = Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey(AsyncImageLoader.GROUP_KEY))
            .andThreadPoolPropertiesDefaults(
                    HystrixThreadPoolProperties.Setter()
                            .withCoreSize(PropertyUtil.getOptional("oskari." + GROUP_KEY + ".job.pool.size", 10))
                            .withMaxQueueSize(PropertyUtil.getOptional("oskari." + GROUP_KEY + ".job.pool.limit", 200))
                            .withQueueSizeRejectionThreshold(PropertyUtil.getOptional("oskari." + GROUP_KEY + ".job.pool.queue", 200)))
            .andCommandPropertiesDefaults(
                    HystrixCommandProperties.Setter()
                            .withExecutionTimeoutInMilliseconds(PropertyUtil.getOptional("oskari." + GROUP_KEY + ".job.timeoutms", 15000)));

    private final String uri;

    protected CommandLoadImageFromURL(String uri) {
        super(SETTER);
        this.uri = uri;
    }

    @Override
    public BufferedImage run() throws Exception {
        return loadImageFromURL(new URL(uri));
    }

    protected static BufferedImage loadImageFromURL(URL url) {
        LOG.debug(url.toString());
        for (int i = 0; i < RETRY_COUNT; i++) {
            try {
                return ImageIO.read(url);
            } catch (IOException e) {
                LOG.warn(e);
            }
        }
        throw new RuntimeException("Failed to read image from: " + url.toString());
    }

}