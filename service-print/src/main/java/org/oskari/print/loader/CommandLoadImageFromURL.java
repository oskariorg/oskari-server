package org.oskari.print.loader;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

import javax.imageio.ImageIO;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.IOHelper;

/**
 * HystrixCommand that loads BufferedImage from URL
 * Retries up to 3 times
 */
public class CommandLoadImageFromURL extends CommandLoadImageBase {

    private static final Logger LOG = LogFactory.getLogger(CommandLoadImageFromURL.class);
    private static final int RETRY_COUNT = 3;
    private static final int SLEEP_BETWEEN_RETRIES_MS = 50;

    private final String uri;
    private final String user;
    private final String pass;

    protected CommandLoadImageFromURL(String commandName, String uri, String user, String pass) {
        super(commandName);
        this.uri = uri;
        this.user = user;
        this.pass = pass;
    }

    @Override
    public BufferedImage run() throws Exception {
        return load(uri, user, pass);
    }

    public static BufferedImage load(String uri, String user, String pass) throws InterruptedException, IOException {
        LOG.debug("Loading image from:", uri);
        for (int i = 0; i < RETRY_COUNT; i++) {
            try {
                HttpURLConnection conn = IOHelper.getConnection(uri, user, pass);
                if (conn.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND) {
                    // short-circuit 404 as we get these a lot in the log
                    return null;
                }
                if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    // Try again after sleep if not ok
                    Thread.sleep(SLEEP_BETWEEN_RETRIES_MS);
                    continue;
                }
                try (InputStream in = new BufferedInputStream(conn.getInputStream())) {
                    return ImageIO.read(in);
                }
            } catch (IOException e) {
                LOG.warn(e, "Failed to load image from:", uri);
                // Sleep for a moment between retries
                Thread.sleep(SLEEP_BETWEEN_RETRIES_MS);
            }
        }
        return null;
    }

    @Override
    public BufferedImage getFallback() {
        return null;
    }

}
