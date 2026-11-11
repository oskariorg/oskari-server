package org.oskari.print.mvt.glyph;

import java.awt.geom.GeneralPath;
import java.util.Map;

/**
 * Glyphs from a Mapbox style's glyph endpoint, vectorised into outlines.
 */
public class SDFGlyphSource implements GlyphSource {

    private final Map<Integer, SDFGlyph> glyphs;

    public SDFGlyphSource(Map<Integer, SDFGlyph> glyphs) {
        this.glyphs = glyphs;
    }

    @Override
    public GeneralPath getOutline(int codePoint) {
        return contour(codePoint, SDFGlyph.EDGE);
    }

    @Override
    public GeneralPath getHaloOutline(int codePoint, float haloWidthPx) {
        return contour(codePoint, SDFGlyph.getHaloLevel(haloWidthPx));
    }

    @Override
    public float getAdvance(int codePoint) {
        SDFGlyph glyph = glyphs.get(codePoint);
        // Metrics are in the field's own em, the outlines are in a 1000 unit one
        return glyph == null ? 0 : glyph.getAdvance() / SDFGlyph.UNITS_PER_EM * GlyphForms.UNITS_PER_EM;
    }

    @Override
    public boolean hasGlyph(int codePoint) {
        return glyphs.containsKey(codePoint);
    }

    private GeneralPath contour(int codePoint, float level) {
        SDFGlyph glyph = glyphs.get(codePoint);
        return glyph == null ? null : SDFVectorizer.getOutline(glyph, level);
    }
}
