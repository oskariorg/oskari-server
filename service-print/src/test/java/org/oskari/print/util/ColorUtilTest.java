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
}
