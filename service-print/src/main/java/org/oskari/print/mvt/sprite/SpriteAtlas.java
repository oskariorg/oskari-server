package org.oskari.print.mvt.sprite;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

/**
 * A Mapbox sprite sheet: the icons of a style packed into one image, with an
 * index saying where each one sits.
 */
public class SpriteAtlas {

    private final BufferedImage sheet;
    private final Map<String, Icon> icons;

    public SpriteAtlas(BufferedImage sheet, Map<String, Icon> icons) {
        this.sheet = sheet;
        this.icons = icons;
    }

    /**
     * One icon's place in the sheet. A 2x sheet holds icons at twice the size,
     * so sizes are divided by the pixel ratio.
     */
    public static class Icon {
        private final int x;
        private final int y;
        private final int width;
        private final int height;
        private final double pixelRatio;

        public Icon(int x, int y, int width, int height, double pixelRatio) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.pixelRatio = pixelRatio <= 0 ? 1 : pixelRatio;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        /**
         * @return width in the sheet's own pixels
         */
        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        /**
         * @return width the icon is drawn at before icon-size, in style pixels
         */
        public double getDisplayWidth() {
            return width / pixelRatio;
        }

        public double getDisplayHeight() {
            return height / pixelRatio;
        }
    }

    /**
     * @return the icon, or null when the style asks for one the sheet has no image for
     */
    public Icon getIcon(String name) {
        return name == null ? null : icons.get(name);
    }

    /**
     * @return the icon's pixels, or null when there is no such icon
     */
    public BufferedImage getImage(String name) {
        Icon icon = getIcon(name);
        if (icon == null) {
            return null;
        }
        if (icon.getX() < 0 || icon.getY() < 0
                || icon.getX() + icon.getWidth() > sheet.getWidth()
                || icon.getY() + icon.getHeight() > sheet.getHeight()) {
            // An index that doesn't match the sheet it came with
            return null;
        }
        return sheet.getSubimage(icon.getX(), icon.getY(), icon.getWidth(), icon.getHeight());
    }

    public static Map<String, Icon> parseIndex(JSONObject json) {
        Map<String, Icon> icons = new HashMap<>();
        for (String name : json.keySet()) {
            JSONObject icon = json.optJSONObject(name);
            if (icon == null) {
                continue;
            }
            icons.put(name, new Icon(
                    icon.optInt("x"),
                    icon.optInt("y"),
                    icon.optInt("width"),
                    icon.optInt("height"),
                    icon.optDouble("pixelRatio", 1)));
        }
        return icons;
    }
}
