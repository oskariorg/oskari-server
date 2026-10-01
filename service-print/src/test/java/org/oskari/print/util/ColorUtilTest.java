package org.oskari.print.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.awt.*;


public class ColorUtilTest {
    @Test
    public void testParseColors() {
        Color rgb = ColorUtil.parseColor("rgb(255,0,0)");
        Color hex = ColorUtil.parseColor("#FF0000");
        Color rrggbb = ColorUtil.parseColor("FF0000");
        Color rgba = ColorUtil.parseColor("rgba(255,0,0,1)");
        Assertions.assertEquals(Color.RED, rgb);
        Assertions.assertEquals(Color.RED, hex);
        Assertions.assertEquals(Color.RED, rrggbb);
        Assertions.assertEquals(Color.RED, rgba);
    }
    @Test
    public void testParseRGBA() {
        Color rgba = ColorUtil.parseColor("rgba(255,0,0,0.5)");
        Assertions.assertTrue(128 == rgba.getAlpha());
        Assertions.assertTrue(Transparency.TRANSLUCENT == rgba.getTransparency());
    }

    @Test
    public void testParseShorthandHex() {
        // Each digit is doubled, "#fff" is white rather than 0x000fff
        Assertions.assertEquals(Color.WHITE, ColorUtil.parseColor("#fff"), "#fff");
        Assertions.assertEquals(Color.RED, ColorUtil.parseColor("#f00"), "#f00");
        Assertions.assertEquals(new Color(255, 204, 136), ColorUtil.parseColor("#fc8"), "#fc8");
    }

    @Test
    public void testParseHexWithAlpha() {
        Assertions.assertEquals(new Color(255, 0, 0, 128), ColorUtil.parseColor("#ff000080"),
                "#rrggbbaa");
        Assertions.assertEquals(new Color(255, 0, 0, 136), ColorUtil.parseColor("#f008"),
                "#rgba");
    }

    @Test
    public void testParseHSL() {
        // The water colour of the OSM Bright style
        Assertions.assertEquals(new Color(191, 217, 242),
                ColorUtil.parseColor("hsl(210, 67%, 85%)"), "hsl()");
        Assertions.assertEquals(Color.RED, ColorUtil.parseColor("hsl(0, 100%, 50%)"), "pure red");
        Assertions.assertEquals(new Color(128, 128, 128),
                ColorUtil.parseColor("hsl(0, 0%, 50%)"), "grey has no hue");
        Color hsla = ColorUtil.parseColor("hsla(0, 0%, 89%, 0.56)");
        Assertions.assertEquals(227, hsla.getRed(), "lightness");
        Assertions.assertEquals(143, hsla.getAlpha(), "alpha");
    }
}
