package org.oskari.print.loader;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.oskari.print.mvt.glyph.GlyphPBF;
import org.oskari.print.mvt.glyph.SDFGlyph;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.ServiceRuntimeException;
import fi.nls.oskari.util.IOHelper;

/**
 * Loads the glyph ranges a print's labels use from the style's glyph endpoint.
 */
public class CommandLoadGlyphs {

    private static final Logger LOG = LogFactory.getLogger(CommandLoadGlyphs.class);

    /** Code points per range, as the glyph endpoint defines them */
    private static final int RANGE_SIZE = 256;
    /** Highest range the endpoint serves, code points above it have no glyphs */
    private static final int MAX_RANGE = 255;

    private CommandLoadGlyphs() {}

    /**
     * Ranges that fail are left out rather than failing the print.
     */
    public static CompletableFuture<Map<Integer, SDFGlyph>> loadGlyphs(String glyphsUrl,
            List<String> fontstack, Set<Integer> codePoints, String commandKey,
            PrintLoader loader) {
        if (glyphsUrl == null || codePoints.isEmpty()) {
            return CompletableFuture.completedFuture(Collections.emptyMap());
        }

        List<Integer> ranges = getRanges(codePoints);
        if (ranges.isEmpty()) {
            return CompletableFuture.completedFuture(Collections.emptyMap());
        }

        List<CompletableFuture<Map<Integer, SDFGlyph>>> futures = new ArrayList<>(ranges.size());
        for (int range : ranges) {
            String uri = getGlyphsURL(glyphsUrl, fontstack, range);
            Supplier<Map<Integer, SDFGlyph>> supplier = () -> loadRange(uri);
            futures.add(loader.runSupplier(commandKey, supplier, () -> null));
        }

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(ignored -> futures.stream()
                        .map(CompletableFuture::join)
                        .filter(Objects::nonNull)
                        .collect(HashMap::new, Map::putAll, Map::putAll));
    }

    /**
     * @return the ranges covering the code points, in order and without duplicates
     */
    private static List<Integer> getRanges(Set<Integer> codePoints) {
        return codePoints.stream()
                .map(codePoint -> codePoint / RANGE_SIZE)
                .filter(range -> range >= 0 && range <= MAX_RANGE)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Font fallback within the stack is resolved by the endpoint.
     */
    static String getGlyphsURL(String glyphsUrl, List<String> fontstack, int range) {
        String stack = String.join(",", fontstack);
        String from = Integer.toString(range * RANGE_SIZE);
        String to = Integer.toString(range * RANGE_SIZE + RANGE_SIZE - 1);
        return glyphsUrl
                .replace("{fontstack}", encodePathSegment(stack))
                .replace("{range}", from + "-" + to);
    }

    /**
     * URLEncoder encodes a space as '+', which is a literal plus in a path.
     */
    private static String encodePathSegment(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }

    private static Map<Integer, SDFGlyph> loadRange(String uri) {
        LOG.debug("Loading glyphs from:", uri);
        try {
            HttpURLConnection conn = IOHelper.getConnection(uri);
            int status = conn.getResponseCode();
            if (status == HttpURLConnection.HTTP_NO_CONTENT
                    || status == HttpURLConnection.HTTP_NOT_FOUND) {
                // A range the fontstack has no characters in
                return null;
            }
            if (status != HttpURLConnection.HTTP_OK) {
                LOG.warn("Got status", status, "for glyphs:", uri);
                throw new ServiceRuntimeException("Unexpected status: " + status);
            }
            byte[] encoded = IOHelper.readBytes(conn);
            if (encoded.length == 0) {
                return null;
            }
            return GlyphPBF.parse(encoded);
        } catch (IOException e) {
            throw new ServiceRuntimeException(e.getMessage(), e);
        }
    }
}
