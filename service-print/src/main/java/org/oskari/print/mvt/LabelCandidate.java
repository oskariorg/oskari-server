package org.oskari.print.mvt;

import java.awt.geom.Rectangle2D;
import java.util.Collections;
import java.util.List;

/**
 * A symbol laid out but not yet drawn. The bounds are in PDF coordinates and
 * include the layer's padding.
 */
class LabelCandidate {

    /**
     * One run of a label: a wrapped line, or the characters that sit on one
     * segment of a line placement.
     */
    public static class Chunk {
        private final String text;
        private final double x;
        private final double y;
        private final double angle;

        public Chunk(String text, double x, double y, double angle) {
            this.text = text;
            this.x = x;
            this.y = y;
            this.angle = angle;
        }

        public String getText() {
            return text;
        }

        /**
         * @return where the run starts, in PDF coordinates
         */
        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }

        /**
         * @return direction the run reads in, in radians. Zero for a point label.
         */
        public double getAngle() {
            return angle;
        }
    }

    private final MVTStyleLayer styleLayer;
    private final List<Chunk> chunks;
    private final float fontSize;
    private final String iconName;
    private final double iconX;
    private final double iconY;
    private final double iconWidth;
    private final double iconHeight;
    private final Rectangle2D bounds;
    private final boolean allowOverlap;
    private final boolean ignorePlacement;
    private final boolean optional;
    private LabelCandidate pairedWith;

    private LabelCandidate(MVTStyleLayer styleLayer, List<Chunk> chunks,
            float fontSize, String iconName, double iconX, double iconY,
            double iconWidth, double iconHeight, Rectangle2D bounds,
            boolean allowOverlap, boolean ignorePlacement, boolean optional) {
        this.styleLayer = styleLayer;
        this.chunks = chunks;
        this.fontSize = fontSize;
        this.iconName = iconName;
        this.iconX = iconX;
        this.iconY = iconY;
        this.iconWidth = iconWidth;
        this.iconHeight = iconHeight;
        this.bounds = bounds;
        this.allowOverlap = allowOverlap;
        this.ignorePlacement = ignorePlacement;
        this.optional = optional;
    }

    public static LabelCandidate text(MVTStyleLayer styleLayer, List<Chunk> chunks,
            float fontSize, Rectangle2D bounds, boolean allowOverlap, boolean ignorePlacement,
            boolean optional) {
        return new LabelCandidate(styleLayer, chunks, fontSize,
                null, 0, 0, 0, 0, bounds, allowOverlap, ignorePlacement, optional);
    }

    public static LabelCandidate icon(MVTStyleLayer styleLayer, String iconName,
            double x, double y, double width, double height, Rectangle2D bounds,
            boolean allowOverlap, boolean ignorePlacement, boolean optional) {
        return new LabelCandidate(styleLayer, Collections.emptyList(), 0,
                iconName, x, y, width, height, bounds, allowOverlap, ignorePlacement, optional);
    }

    public MVTStyleLayer getStyleLayer() {
        return styleLayer;
    }

    public List<Chunk> getChunks() {
        return chunks;
    }

    public float getFontSize() {
        return fontSize;
    }

    /**
     * @return name of the sprite to draw, null for a text candidate
     */
    public String getIconName() {
        return iconName;
    }

    public double getIconX() {
        return iconX;
    }

    public double getIconY() {
        return iconY;
    }

    public double getIconWidth() {
        return iconWidth;
    }

    public double getIconHeight() {
        return iconHeight;
    }

    public boolean isIcon() {
        return iconName != null;
    }

    /**
     * @return the space the symbol takes, in PDF coordinates, padding included
     */
    public Rectangle2D getBounds() {
        return bounds;
    }

    /**
     * @return whether the symbol is drawn even where it overlaps another one
     */
    public boolean isAllowOverlap() {
        return allowOverlap;
    }

    /**
     * @return whether other symbols may be drawn over this one
     */
    public boolean isIgnorePlacement() {
        return ignorePlacement;
    }

    /**
     * @return whether this half of a symbol may be dropped on its own when the
     *         layer draws both an icon and a label
     */
    public boolean isOptional() {
        return optional;
    }

    /**
     * Ties an icon and a label of the same feature together.
     *
     * Mapbox places the two as one symbol: if either does not fit, neither is
     * drawn, unless the one that does not fit is marked optional.
     */
    public static void pair(LabelCandidate icon, LabelCandidate text) {
        icon.pairedWith = text;
        text.pairedWith = icon;
    }

    /**
     * @return the other half of the symbol, null when the layer draws only one
     */
    public LabelCandidate getPairedWith() {
        return pairedWith;
    }
}
