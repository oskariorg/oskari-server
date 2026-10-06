package org.oskari.print.util;

import java.awt.Color;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorUtil {
    // rgb(255,0,0)
    private static final Pattern RGB = Pattern.compile("rgb *\\( *([0-9]+), *([0-9]+), *([0-9]+) *\\)");
    // rgba(255,0,0,0.5)
    private static final Pattern RGBA = Pattern.compile("rgba *\\( *([0-9]+), *([0-9]+), *([0-9]+), *([0,1][.]?[0-9]*) *\\)");
    // hsl(210, 67%, 85%) and hsla(0, 0%, 89%, 0.56), used by Mapbox styles
    private static final Pattern HSL = Pattern.compile(
            "hsla? *\\( *(-?[0-9.]+) *, *([0-9.]+)% *, *([0-9.]+)% *(?:, *([0-9.]+) *)?\\)");

    public static Color parseColor(String color) {
        if (color == null || color.isEmpty()) {
            return null;
        }
        if (color.charAt(0) == '#') {
            return parseHexColor(color);
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
        matcher = HSL.matcher(color);
        if (matcher.matches()) {
            return parseHSL(matcher);
        }
        return parseHexColor(color);

    }

    /**
     * CSS #rgb, #rgba, #rrggbb and #rrggbbaa.
     */
    private static Color parseHexColor(String color) {
        String hex = color.charAt(0) == '#' ? color.substring(1) : color;
        if (hex.length() == 3 || hex.length() == 4) {
            StringBuilder expanded = new StringBuilder(hex.length() * 2);
            for (int i = 0; i < hex.length(); i++) {
                expanded.append(hex.charAt(i)).append(hex.charAt(i));
            }
            hex = expanded.toString();
        }
        long value = Long.parseLong(hex, 16);
        if (hex.length() == 8) {
            return new Color((int) (value >> 24) & 0xff, (int) (value >> 16) & 0xff,
                    (int) (value >> 8) & 0xff, (int) value & 0xff);
        }
        return new Color((int) value);
    }

    private static Color parseHSL(Matcher matcher) {
        double hue = Double.parseDouble(matcher.group(1)) / 360;
        double saturation = Double.parseDouble(matcher.group(2)) / 100;
        double lightness = Double.parseDouble(matcher.group(3)) / 100;
        String alpha = matcher.group(4);

        double q = lightness < 0.5
                ? lightness * (1 + saturation)
                : lightness + saturation - lightness * saturation;
        double p = 2 * lightness - q;
        int red = toRGB(p, q, hue + 1.0 / 3);
        int green = toRGB(p, q, hue);
        int blue = toRGB(p, q, hue - 1.0 / 3);
        if (alpha == null) {
            return new Color(red, green, blue);
        }
        return new Color(red, green, blue,
                (int) (Double.parseDouble(alpha) * 255 + 0.5));
    }

    private static int toRGB(double p, double q, double t) {
        if (t < 0) {
            t += 1;
        } else if (t > 1) {
            t -= 1;
        }
        double value;
        if (t < 1.0 / 6) {
            value = p + (q - p) * 6 * t;
        } else if (t < 1.0 / 2) {
            value = q;
        } else if (t < 2.0 / 3) {
            value = p + (q - p) * (2.0 / 3 - t) * 6;
        } else {
            value = p;
        }
        return (int) Math.round(value * 255);
    }

}
