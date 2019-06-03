package org.oskari.print.util;

import java.awt.Color;

public class ColorUtil {

    public static Color parseColor(String rrggbb) {
        return new Color(parseRGB(rrggbb));
    }

    public static int parseRGB(String rrggbb) {
        if (rrggbb.charAt(0) == '#') {
            rrggbb = rrggbb.substring(1);
        }
        return Integer.parseInt(rrggbb, 16);
    }

}
