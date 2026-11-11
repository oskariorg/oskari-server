package org.oskari.print.mvt.glyph;

import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SDFVectorizerTest {

    /**
     * Builds a field for a filled rectangle, encoded the way the glyph endpoint
     * does it: value = 191.25 - 31.875 * distance, distance positive outside.
     */
    private static SDFGlyph rectangle(int width, int height) {
        int bw = width + 2 * SDFGlyph.BUFFER;
        int bh = height + 2 * SDFGlyph.BUFFER;
        byte[] bitmap = new byte[bw * bh];
        for (int y = 0; y < bh; y++) {
            for (int x = 0; x < bw; x++) {
                // Distance to the rectangle that spans the buffer inset
                double dx = Math.max(Math.max(SDFGlyph.BUFFER - x, x - (bw - SDFGlyph.BUFFER - 1)), 0);
                double dy = Math.max(Math.max(SDFGlyph.BUFFER - y, y - (bh - SDFGlyph.BUFFER - 1)), 0);
                double outside = Math.hypot(dx, dy);
                double inside = 0;
                if (outside == 0) {
                    inside = Math.min(
                            Math.min(x - SDFGlyph.BUFFER + 1, bw - SDFGlyph.BUFFER - x),
                            Math.min(y - SDFGlyph.BUFFER + 1, bh - SDFGlyph.BUFFER - y));
                }
                double d = outside - inside;
                int value = (int) Math.round(SDFGlyph.EDGE - SDFGlyph.VALUES_PER_PIXEL * d);
                bitmap[y * bw + x] = (byte) Math.max(0, Math.min(255, value));
            }
        }
        // top is counted down from the ascender, so a glyph standing on the
        // baseline reports height - ASCENT
        return new SDFGlyph('X', bitmap, width, height, 0, height - SDFGlyph.ASCENT, width + 2);
    }

    @Test
    public void rectangleContoursToARectangle() {
        SDFGlyph glyph = rectangle(10, 12);
        GeneralPath path = SDFVectorizer.getOutline(glyph, SDFGlyph.EDGE);
        Assertions.assertNotNull(path, "a filled field must produce an outline");

        Rectangle2D bounds = path.getBounds2D();
        // 10 x 12 pixels of a 24 pixel em, in a 1000 unit em square
        double expectedWidth = 10.0 / SDFGlyph.UNITS_PER_EM * 1000;
        double expectedHeight = 12.0 / SDFGlyph.UNITS_PER_EM * 1000;
        Assertions.assertEquals(expectedWidth, bounds.getWidth(), 45, "outline width in em units");
        Assertions.assertEquals(expectedHeight, bounds.getHeight(), 45, "outline height in em units");
    }

    @Test
    public void outlineIsPlacedVerticallyByTheGlyphsTop() {
        SDFGlyph glyph = rectangle(10, 12);
        Rectangle2D standing = SDFVectorizer.getOutline(glyph, SDFGlyph.EDGE).getBounds2D();
        Assertions.assertEquals(0, standing.getMinY(), 45, "bottom of the glyph is the baseline");

        // Four pixels lower than a glyph that stands on the baseline
        SDFGlyph descender = new SDFGlyph('p', glyph.getBitmap(), glyph.getWidth(),
                glyph.getHeight(), 0, glyph.getTop() - 4, glyph.getAdvance());
        Rectangle2D bounds = SDFVectorizer.getOutline(descender, SDFGlyph.EDGE).getBounds2D();
        double fourPixels = 4.0 / SDFGlyph.UNITS_PER_EM * 1000;
        Assertions.assertEquals(-fourPixels, bounds.getMinY(), 45,
                "a descender hangs below the baseline");
    }

    @Test
    public void wideHaloIsCappedAtTheBuffer() {
        SDFGlyph glyph = rectangle(10, 12);
        GeneralPath capped = SDFVectorizer.getOutline(glyph, SDFGlyph.getHaloLevel(SDFGlyph.BUFFER));
        GeneralPath wide = SDFVectorizer.getOutline(glyph, SDFGlyph.getHaloLevel(20));
        Assertions.assertNotNull(wide, "a halo wider than the field still draws");
        Rectangle2D bounds = wide.getBounds2D();
        Assertions.assertEquals(capped.getBounds2D(), bounds, "a halo wider than the buffer is capped at it");
        double expectedWidth = (10.0 + 2 * SDFGlyph.BUFFER) / SDFGlyph.UNITS_PER_EM * 1000;
        Assertions.assertEquals(expectedWidth, bounds.getWidth(), 45, "halo reaches the edge of the field");
    }

    @Test
    public void simplificationKeepsTheCorners() {
        SDFGlyph glyph = rectangle(10, 12);
        GeneralPath path = SDFVectorizer.getOutline(glyph, SDFGlyph.EDGE);
        int points = 0;
        PathIterator it = path.getPathIterator(null);
        float[] coords = new float[6];
        while (!it.isDone()) {
            int type = it.currentSegment(coords);
            if (type == PathIterator.SEG_MOVETO || type == PathIterator.SEG_LINETO) {
                points++;
            }
            it.next();
        }
        // A rectangle needs four corners; marching squares would give ~44
        // vertices without simplification
        Assertions.assertTrue(points >= 4, "rectangle keeps its corners, got " + points);
        Assertions.assertTrue(points <= 12, "collinear vertices are removed, got " + points);
    }
}
