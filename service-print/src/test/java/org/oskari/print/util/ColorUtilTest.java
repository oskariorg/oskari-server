package org.oskari.print.util;

import org.junit.Assert;
import org.junit.Test;
import java.awt.*;


public class ColorUtilTest {
    @Test
    public void testParseColors() {
        Color rgb = ColorUtil.parseColor("rgb(255,0,0)");
        Color hex = ColorUtil.parseColor("#FF0000");
        Color rrggbb = ColorUtil.parseColor("FF0000");
        Color rgba = ColorUtil.parseColor("rgba(255,0,0,1)");
        Assert.assertEquals(Color.RED, rgb);
        Assert.assertEquals(Color.RED, hex);
        Assert.assertEquals(Color.RED, rrggbb);
        Assert.assertEquals(Color.RED, rgba);
    }
    @Test
    public void testParseRGBA() {
        Color rgba = ColorUtil.parseColor("rgba(255,0,0,0.5)");
        Assert.assertTrue(128 == rgba.getAlpha());
        Assert.assertTrue(Transparency.TRANSLUCENT == rgba.getTransparency());
    }
}
