package org.oskari.print.util;

import java.awt.Color;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorUtil {
    // rgb(255,0,0)
    private static final Pattern RGB = Pattern.compile("rgb *\\( *([0-9]+), *([0-9]+), *([0-9]+) *\\)");
    // rgba(255,0,0,0.5)
    private static final Pattern RGBA = Pattern.compile("rgba *\\( *([0-9]+), *([0-9]+), *([0-9]+), *([0,1][.]?[0-9]*) *\\)");

    public static Color parseColor(String color) {
        if (color == null || color.isEmpty()) {
            return null;
        }
        if (color.charAt(0) == '#') {
            return new Color(parseHEX(color));
        }
        Matcher matcher = RGBA.matcher(color);
        if (matcher.matches()){
            return new Color(Integer.valueOf(matcher.group(1)),
                    Integer.valueOf(matcher.group(2)),
                    Integer.valueOf(matcher.group(3)),
                    (int) (Float.parseFloat(matcher.group(4)) * 255 + 0.5) );
        }
        matcher = RGB.matcher(color);
        if (matcher.matches()) {
            return new Color(Integer.valueOf(matcher.group(1)),
                    Integer.valueOf(matcher.group(2)),
                    Integer.valueOf(matcher.group(3)));
        }
        return new Color(parseHEX(color));

    }

    private static int parseHEX(String rrggbb) {
        if (rrggbb.charAt(0) == '#') {
            rrggbb = rrggbb.substring(1);
        }
        return Integer.parseInt(rrggbb, 16);
    }

}
