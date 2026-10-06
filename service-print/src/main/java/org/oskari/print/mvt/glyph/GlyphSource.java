package org.oskari.print.mvt.glyph;

import java.awt.geom.GeneralPath;

/**
 * Where the renderer gets the shape of a character.
 *
 * Outlines are in a 1000 unit em square with the origin on the baseline at the
 * pen position, so a caller scales them by fontSize / 1000.
 */
public interface GlyphSource {

    /**
     * @return outline of the character, or null when the source has no glyph for
     *         it or the glyph has no shape of its own such as a space
     */
    GeneralPath getOutline(int codePoint);

    /**
     * @return outline of the character grown outwards by the halo width, or null
     *         when the source cannot draw a halo for it
     * @param haloWidthPx halo width in pixels at a {@link SDFGlyph#UNITS_PER_EM}
     *                    pixel em, not at the label's own size
     */
    GeneralPath getHaloOutline(int codePoint, float haloWidthPx);

    /**
     * @return how far the pen moves after the character, in em units
     */
    float getAdvance(int codePoint);

    /**
     * @return true when the source can draw the character at all. A source that
     *         cannot is asked for nothing else.
     */
    boolean hasGlyph(int codePoint);

    /**
     * @return width of the text in em units, characters the source cannot draw
     *         are skipped
     */
    default float getWidth(String text) {
        float width = 0;
        for (int i = 0; i < text.length(); i++) {
            int codePoint = text.codePointAt(i);
            if (Character.isSupplementaryCodePoint(codePoint)) {
                i++;
            }
            if (hasGlyph(codePoint)) {
                width += getAdvance(codePoint);
            }
        }
        return width;
    }
}
