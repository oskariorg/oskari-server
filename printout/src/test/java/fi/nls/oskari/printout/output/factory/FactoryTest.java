package fi.nls.oskari.printout.output.factory;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class FactoryTest {
	@Test
	public void testFuncFact() {

		assertTrue(getClass().getResourceAsStream(
				"/META-INF/services/org.geotools.filter.FunctionFactory") != null);

	}

	@Test
	public void testRGBA() {
		String valPart = "rgba(215, 40, 40, 0.9)";
		String rgbPart = valPart.substring(valPart.indexOf('(') + 1,
				valPart.indexOf(')'));
		String[] rgbParts = rgbPart.split(",");

		int red = Integer.valueOf(rgbParts[0].trim(), 10);
		int green = Integer.valueOf(rgbParts[1].trim(), 10);
		int blue = Integer.valueOf(rgbParts[2].trim(), 10);
		Float alphaFloat = Float.valueOf(rgbParts[3].trim());

		assertTrue(red == 215);
		assertTrue(green == 40);
		assertTrue(blue == 40);
		assertTrue(alphaFloat == 0.9f);

		/* int alpha = new Float(alphaFloat * 256f / 256f).intValue(); */

		/* Color col = new Color(red, green, blue, alpha); */

	}

}
