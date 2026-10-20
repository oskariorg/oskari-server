package org.oskari.print.mvt;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.json.JSONObject;
import org.oskari.print.loader.CommandLoadGlyphs;
import org.oskari.print.loader.CommandLoadSprite;
import org.oskari.print.loader.PrintLoader;
import org.oskari.print.mvt.glyph.GlyphSource;
import org.oskari.print.mvt.glyph.SDFGlyph;
import org.oskari.print.mvt.glyph.SDFGlyphSource;
import org.oskari.print.mvt.sprite.SpriteAtlas;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import no.ecc.vectortile.VectorTileDecoder;

/**
 * The glyphs and icons a style needs to draw its symbols.
 *
 * Both hang off the style document rather than the map layer: the glyph
 * endpoint is its "glyphs" url and the icons its "sprite" url. They are fetched
 * once per print, after the tiles are in, because which glyphs are needed
 * depends on the labels the tiles actually carry.
 */
public class MVTStyleResources {

    private static final Logger LOG = LogFactory.getLogger(MVTStyleResources.class);

    /** Mapbox's default when a style names no font */
    private static final String DEFAULT_FONT = "Open Sans Regular";

    /** One source per fontstack, keyed by the fontstack joined with commas */
    private final Map<String, GlyphSource> glyphsByFontstack;
    private final SpriteAtlas sprites;

    MVTStyleResources(Map<String, GlyphSource> glyphsByFontstack, SpriteAtlas sprites) {
        this.glyphsByFontstack = glyphsByFontstack;
        this.sprites = sprites;
    }

    /**
     * @return glyphs of the layer's text-font fontstack, null when they could not be loaded
     */
    public GlyphSource getGlyphs(MVTStyleLayer styleLayer) {
        return glyphsByFontstack.get(key(styleLayer.getTextFont()));
    }

    private static String key(List<String> fontstack) {
        return String.join(",", fontstack == null || fontstack.isEmpty()
                ? Collections.singletonList(DEFAULT_FONT)
                : fontstack);
    }

    /**
     * @return the style's icons, null when it publishes no sprite sheet
     */
    public SpriteAtlas getSprites() {
        return sprites;
    }

    /**
     * Starts loading what the style's symbol layers need for the tiles that
     * were fetched. A glyph range or sprite sheet that fails is left out rather
     * than failing the layer, so the future always completes normally.
     */
    public static CompletableFuture<MVTStyleResources> load(JSONObject style,
            List<MVTStyleLayer> styleLayers, List<MVTTile> tiles, String commandKey,
            PrintLoader loader) {
        boolean needsText = styleLayers.stream().anyMatch(
                layer -> layer.getType() == MVTStyleLayer.Type.SYMBOL && layer.hasText());
        boolean needsIcons = styleLayers.stream().anyMatch(
                layer -> layer.getType() == MVTStyleLayer.Type.SYMBOL && layer.hasIcon());
        if (!needsText && !needsIcons) {
            return CompletableFuture.completedFuture(empty());
        }

        Map<String, CompletableFuture<Map<Integer, SDFGlyph>>> futuresByFontstack =
                new LinkedHashMap<>();
        if (needsText) {
            String glyphsUrl = style.optString("glyphs", null);
            if (glyphsUrl == null) {
                LOG.info("Style has symbol layers but no glyphs url, labels are not drawn");
            } else {
                for (Map.Entry<String, List<String>> entry
                        : getFontstacks(styleLayers).entrySet()) {
                    Set<Integer> codePoints = getCodePoints(styleLayers, tiles, entry.getKey());
                    if (codePoints.isEmpty()) {
                        continue;
                    }
                    futuresByFontstack.put(entry.getKey(), CommandLoadGlyphs.loadGlyphs(
                            glyphsUrl, entry.getValue(), codePoints, commandKey, loader));
                }
            }
        }

        CompletableFuture<SpriteAtlas> futureSprites = null;
        if (needsIcons) {
            String spriteUrl = style.optString("sprite", null);
            if (spriteUrl == null) {
                LOG.info("Style has icon layers but no sprite url, icons are not drawn");
            } else {
                futureSprites = CommandLoadSprite.loadSprite(spriteUrl, commandKey, loader);
            }
        }

        List<CompletableFuture<?>> all = new ArrayList<>(futuresByFontstack.values());
        if (futureSprites != null) {
            all.add(futureSprites);
        }
        CompletableFuture<SpriteAtlas> sprites = futureSprites;
        // Every future is done by the time this runs, a failed one is read as missing
        return CompletableFuture.allOf(all.toArray(new CompletableFuture[0]))
                .handle((ignored, e) -> {
                    Map<String, GlyphSource> glyphs = new LinkedHashMap<>();
                    for (Map.Entry<String, CompletableFuture<Map<Integer, SDFGlyph>>> entry
                            : futuresByFontstack.entrySet()) {
                        Map<Integer, SDFGlyph> loaded = get(entry.getValue(), "glyphs");
                        if (loaded != null && !loaded.isEmpty()) {
                            glyphs.put(entry.getKey(), new SDFGlyphSource(loaded));
                        }
                    }
                    return new MVTStyleResources(glyphs,
                            sprites == null ? null : get(sprites, "sprite sheet"));
                });
    }

    private static <T> T get(CompletableFuture<T> future, String what) {
        try {
            return future.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOG.warn("Interrupted while loading", what);
            return null;
        } catch (ExecutionException e) {
            LOG.warn("Failed to load", what, "-", e.getMessage());
            return null;
        }
    }

    /**
     * Distinct fontstacks of the text symbol layers, keyed by the comma-joined stack
     */
    static Map<String, List<String>> getFontstacks(List<MVTStyleLayer> styleLayers) {
        Map<String, List<String>> fontstacks = new LinkedHashMap<>();
        for (MVTStyleLayer layer : styleLayers) {
            if (layer.getType() != MVTStyleLayer.Type.SYMBOL || !layer.hasText()) {
                continue;
            }
            String key = key(layer.getTextFont());
            fontstacks.putIfAbsent(key, List.of(key.split(",")));
        }
        return fontstacks;
    }

    /**
     * Reads the labels the tiles carry to find which characters have to be
     * fetched. A print is one bbox at one zoom, so this is usually one or two
     * ranges of 256 rather than a whole font.
     */
    private static Set<Integer> getCodePoints(List<MVTStyleLayer> styleLayers,
            List<MVTTile> tiles, String fontstack) {
        Set<Integer> codePoints = new HashSet<>();
        for (MVTStyleLayer styleLayer : styleLayers) {
            if (styleLayer.getType() != MVTStyleLayer.Type.SYMBOL || !styleLayer.hasText()) {
                continue;
            }
            if (!fontstack.equals(key(styleLayer.getTextFont()))) {
                continue;
            }
            String sourceLayer = styleLayer.getSourceLayer();
            for (MVTTile tile : tiles) {
                for (VectorTileDecoder.Feature feature : tile.getFeatures()) {
                    if (sourceLayer != null && !sourceLayer.equals(feature.getLayerName())) {
                        continue;
                    }
                    Map<String, Object> attributes = feature.getAttributes();
                    if (!styleLayer.matches(attributes, feature.getGeometry())) {
                        continue;
                    }
                    String label = MVTTextField.resolve(styleLayer.getTextField(), attributes);
                    label = MVTTextField.transform(label, styleLayer.getTextTransform());
                    if (label != null) {
                        label.codePoints().forEach(codePoints::add);
                    }
                }
            }
        }
        return codePoints;
    }

    public static MVTStyleResources empty() {
        return new MVTStyleResources(Collections.emptyMap(), null);
    }
}
