package org.oskari.print.mvt.glyph;

import java.awt.geom.GeneralPath;

/**
 * A glyph source that draws every character as a filled block half an em wide.
 */
public class BlockGlyphSource implements GlyphSource {

    private static final float ADVANCE = 500;
    private static final float HEIGHT = 717;

    @Override
    public GeneralPath getOutline(int codePoint) {
        GeneralPath path = new GeneralPath();
        // A little narrower than the advance so that neighbours don't merge
        path.moveTo(20, 0);
        path.lineTo(ADVANCE - 20, 0);
        path.lineTo(ADVANCE - 20, HEIGHT);
        path.lineTo(20, HEIGHT);
        path.closePath();
        return path;
    }

    @Override
    public GeneralPath getHaloOutline(int codePoint, float haloWidthPx) {
        return null;
    }

    @Override
    public float getAdvance(int codePoint) {
        return ADVANCE;
    }

    @Override
    public boolean hasGlyph(int codePoint) {
        return true;
    }
}
