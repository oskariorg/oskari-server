package fi.nls.oskari.printout.input.maplink;

import static org.junit.Assert.assertTrue;

import org.apache.commons.lang3.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.junit.Test;

public class MapLinkJSoupClensingTest {

	@Test
	public void testClensing() {
		String pageTitle = StringEscapeUtils.unescapeHtml4(Jsoup.clean(
				"Jeppist&auml; jee", Whitelist.simpleText()));

		assertTrue("Jeppistä jee".equals(pageTitle));

	}

	@Test
	public void testWhiteList() {
		String regx = "^http:\\/\\/(www|demo|static|cdn)\\.paikkatietoikkuna\\.fi\\/.+$|^http:\\/\\/(a|b|c|d)\\.karttatiili\\.fi\\/.+$|^http:\\/\\/karttatiili\\.fi\\/.+$|^data:image\\/png;base64,.+$|^data:image\\/jpeg\\;base64,.+|^http:\\/\\/(www|demo|static|cdn)\\.paikkatietoikkuna\\.fi:80\\/.+$";

		Object[][] tests = new Object[][] {
				{ "http://www.paikkatietoikkuna.fi/asdasdasdad", Boolean.TRUE },
				{ "http://demo.paikkatietoikkuna.fi", Boolean.FALSE },
				{ "http://static.paikkatietoikkuna.fi", Boolean.FALSE },
				{ "http://karttatiili.fi", Boolean.FALSE },
				{ "http://a.karttatiili.fi/dataset/xxx/service/wms",
						Boolean.TRUE },
				{ "http://b.karttatiili.fi/dataset/xxx/service/wms",
						Boolean.TRUE },
				{ "http://c.karttatiili.fi/dataset/xxx/service/wms",
						Boolean.TRUE },
				{ "http://d.karttatiili.fi/dataset/xxx/service/wms",
						Boolean.TRUE },
				{ "http://xxx.org", Boolean.FALSE },
				{ "http://pxyz.fi", Boolean.FALSE },
				{ "http://paikkatietoikkuna.fi.tunari.org", Boolean.FALSE },
				{ "file://etc/passwd", Boolean.FALSE },
				{ "/etc/secure", Boolean.FALSE },
				{
						"data:image/png;base64,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",
						Boolean.TRUE },
				{ "http://www.paikkatietoikkuna.fi:80/transport-0.0.1/image?layerId=216&style=oskari_custom&srs=EPSG:3067&bbox=329216.0,6820864.0,329728.0,6821376.0&zoom=9&session=XFFDFDDBA1A", Boolean.TRUE }

		};

		for (Object[] obj : tests) {

			boolean expected = (Boolean) obj[1];
			boolean matchResult = ((String) obj[0]).matches(regx);
			boolean result = matchResult == expected;

			System.out.println("[" + result + "] " + obj[0] + " " + expected
					+ " === " + matchResult);

			assertTrue(result);
		}

	}
}
