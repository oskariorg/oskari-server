package org.oskari.print.mvt.glyph;

/**
 * One glyph as the glyph endpoint serves it: a signed distance field of the
 * glyph's shape together with the metrics needed to place it.
 *
 * The field is generated at a 24 pixel em with a 3 pixel border on every side,
 * so the bitmap is (width + 6) * (height + 6) bytes, row major. Distance is
 * encoded as
 *
 * <pre>
 * value = 191.25 - 31.875 * d
 * </pre>
 *
 * where d is the distance in pixels, positive outside the glyph. The outline is
 * therefore the 191 level set, not the midpoint of the byte range.
 */
public class SDFGlyph {

    /** Border the field carries on each side, in pixels */
    public static final int BUFFER = 3;
    /** Em size the field is generated at, in pixels */
    public static final float UNITS_PER_EM = 24f;
    /** Value the glyph's outline sits at: 255 * (1 - cutoff), cutoff = 0.25 */
    public static final float EDGE = 191.25f;
    /** How many byte values one pixel of distance is worth: 255 / radius, radius = 8 */
    public static final float VALUES_PER_PIXEL = 31.875f;
    /**
     * Ascender height in field pixels. The endpoint's "top" counts down from
     * this line, not up from the baseline.
     */
    public static final int ASCENT = 17;

    private final int codePoint;
    private final byte[] bitmap;
    private final int width;
    private final int height;
    private final int left;
    private final int top;
    private final int advance;

    public SDFGlyph(int codePoint, byte[] bitmap, int width, int height,
            int left, int top, int advance) {
        this.codePoint = codePoint;
        this.bitmap = bitmap;
        this.width = width;
        this.height = height;
        this.left = left;
        this.top = top;
        this.advance = advance;
    }

    public int getCodePoint() {
        return codePoint;
    }

    /**
     * @return the distance field, (width + 2 * BUFFER) * (height + 2 * BUFFER)
     *         bytes, or null for a glyph with no shape such as a space
     */
    public byte[] getBitmap() {
        return bitmap;
    }

    /**
     * @return width of the glyph in pixels, the bitmap is wider by 2 * BUFFER
     */
    public int getWidth() {
        return width;
    }

    /**
     * @return height of the glyph in pixels, the bitmap is taller by 2 * BUFFER
     */
    public int getHeight() {
        return height;
    }

    /**
     * @return offset from the pen position to the left edge of the glyph, in pixels
     */
    public int getLeft() {
        return left;
    }

    /**
     * @return offset of the glyph's top edge down from the ascender, in pixels.
     *         Use {@link #getTopAboveBaseline()} to place it.
     */
    public int getTop() {
        return top;
    }

    /**
     * @return how far the glyph's top edge sits above the baseline, in pixels
     */
    public int getTopAboveBaseline() {
        return ASCENT + top;
    }

    /**
     * @return how far the pen moves after the glyph, in pixels
     */
    public int getAdvance() {
        return advance;
    }

    public int getBitmapWidth() {
        return width + 2 * BUFFER;
    }

    public int getBitmapHeight() {
        return height + 2 * BUFFER;
    }

    public boolean hasShape() {
        return bitmap != null && width > 0 && height > 0
                && bitmap.length >= getBitmapWidth() * getBitmapHeight();
    }

    /**
     * @return the distance field value at the position, 0 outside the bitmap so
     *         that contours close at the border
     */
    public float getValue(int x, int y) {
        if (x < 0 || y < 0 || x >= getBitmapWidth() || y >= getBitmapHeight()) {
            return 0;
        }
        return bitmap[y * getBitmapWidth() + x] & 0xFF;
    }

    /**
     * Field value of the outline grown by the halo width (field pixels), capped
     * at BUFFER since the field reaches no further.
     */
    public static float getHaloLevel(float haloWidthPx) {
        return EDGE - VALUES_PER_PIXEL * Math.min(haloWidthPx, BUFFER);
    }
}
