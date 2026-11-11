package org.oskari.print.loader;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import javax.imageio.ImageIO;

import org.json.JSONException;
import org.json.JSONObject;
import org.oskari.print.mvt.sprite.SpriteAtlas;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.ServiceRuntimeException;
import fi.nls.oskari.util.IOHelper;

/**
 * Loads a style's sprite sheet, preferring @2x since print resolution is above
 * screen resolution.
 */
public class CommandLoadSprite {

    private static final Logger LOG = LogFactory.getLogger(CommandLoadSprite.class);

    private static final String HIDPI = "@2x";

    private CommandLoadSprite() {}

    /**
     * Starts loading the sheet. A sheet that fails to load leaves the icons out
     * of the print rather than failing it.
     *
     * @param spriteUrl the style's sprite url, without the extension
     */
    public static CompletableFuture<SpriteAtlas> loadSprite(String spriteUrl, String commandKey,
            PrintLoader loader) {
        if (spriteUrl == null || spriteUrl.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }
        Supplier<SpriteAtlas> supplier = () -> load(spriteUrl);
        return loader.runSupplier(commandKey, supplier, () -> null);
    }

    private static SpriteAtlas load(String spriteUrl) {
        SpriteAtlas atlas = load(spriteUrl, HIDPI);
        if (atlas != null) {
            return atlas;
        }
        LOG.debug("No", HIDPI, "sprite sheet, falling back to the plain one:", spriteUrl);
        return load(spriteUrl, "");
    }

    private static SpriteAtlas load(String spriteUrl, String suffix) {
        try {
            byte[] index = read(spriteUrl + suffix + ".json");
            if (index == null) {
                return null;
            }
            byte[] image = read(spriteUrl + suffix + ".png");
            if (image == null) {
                return null;
            }
            Map<String, SpriteAtlas.Icon> icons = SpriteAtlas.parseIndex(
                    new JSONObject(new String(index, StandardCharsets.UTF_8)));
            BufferedImage sheet = ImageIO.read(new ByteArrayInputStream(image));
            if (sheet == null || icons.isEmpty()) {
                return null;
            }
            return new SpriteAtlas(sheet, icons);
        } catch (IOException | JSONException e) {
            LOG.warn("Failed to load sprite sheet:", spriteUrl + suffix, "-", e.getMessage());
            return null;
        }
    }

    /**
     * @return the file's bytes, null when the service doesn't have it
     */
    private static byte[] read(String uri) throws IOException {
        LOG.debug("Loading sprite from:", uri);
        HttpURLConnection conn = IOHelper.getConnection(uri);
        int status = conn.getResponseCode();
        if (status == HttpURLConnection.HTTP_NOT_FOUND
                || status == HttpURLConnection.HTTP_NO_CONTENT) {
            return null;
        }
        if (status != HttpURLConnection.HTTP_OK) {
            LOG.warn("Got status", status, "for sprite:", uri);
            throw new ServiceRuntimeException("Unexpected status: " + status);
        }
        byte[] bytes = IOHelper.readBytes(conn);
        return bytes.length == 0 ? null : bytes;
    }
}
