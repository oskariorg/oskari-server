package fi.nls.oskari.printout.imaging;

import java.awt.*;

public class ColorOps {

	final CharSequence HASHMARK = "#";

	final CharSequence RGBA = "rgba";

	public Color get(String valPart, Color defaultValue) {

		if (valPart == null) {
			return defaultValue;
		}

		Color col = defaultValue;

		if (valPart.contains(HASHMARK)) {
			String hexPart = valPart.substring(1);
			Integer color = Integer.parseInt(hexPart, 16);

			int red = (color & 0xFF0000) >> 16;
			int green = (color & 0x00FF00) >> 8;
			int blue = (color & 0x0000ff);
			col = new Color(red, green, blue);

		} else if (valPart.contains(RGBA)) {

			String rgbPart = valPart.substring(valPart.indexOf('(') + 1,
					valPart.indexOf(')'));
			String[] rgbParts = rgbPart.split(",");

			int red = Integer.valueOf(rgbParts[0].trim(), 10);
			int green = Integer.valueOf(rgbParts[1].trim(), 10);
			int blue = Integer.valueOf(rgbParts[2].trim(), 10);
			Float alphaFloat = Float.valueOf(rgbParts[3].trim());

			int alpha = new Float(alphaFloat * 256f / 256f).intValue();

			col = new Color(red, green, blue, alpha);

		}

		return col;
	}

}
