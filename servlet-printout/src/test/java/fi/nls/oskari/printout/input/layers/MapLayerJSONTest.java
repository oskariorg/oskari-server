package fi.nls.oskari.printout.input.layers;

import com.vividsolutions.jts.io.ParseException;
import fi.nls.oskari.printout.output.map.MapProducer;
import fi.nls.oskari.printout.ws.ProxySetup;
import fi.nls.oskari.printout.ws.jaxrs.resource.MapResource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.net.URL;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.assertTrue;

public class MapLayerJSONTest {
	private static Log log = LogFactory.getLog(MapLayerJSONTest.class);

	private Properties props;

	@Before
	public void setUp() throws Exception {

		Properties properties = new Properties();

		properties.setProperty("log4j.logger.org.geowebcache", "DEBUG");
		properties.setProperty("log4j.logger.fi.nls.oskari", "DEBUG");

		properties.setProperty("log4j.rootLogger", "DEBUG, A1");
		properties.setProperty("log4j.appender.A1",
				"org.apache.log4j.ConsoleAppender");
		properties.setProperty("log4j.appender.A1.layout",
				"org.apache.log4j.PatternLayout");
		properties.setProperty("log4j.appender.A1.layout.ConversionPattern",
				"%-4r [%t] %-5p %c %x - %m%n");
		/* "%c %x - %m%n"); */

		PropertyConfigurator.configure(properties);

		String conf = System.getProperty("fi.paikkatietoikkuna.imaging.config");

		props = new Properties();
		Reader r = conf != null ? new FileReader(conf) : new InputStreamReader(
				MapResource.class.getResourceAsStream("default.properties"));
		try {
			props.load(r);
		} finally {
			r.close();
		}

	}

	@Before
	public void setupProxy() throws IOException {

		new ProxySetup();
	}

	@Test
	public void testMapLayerJSONParser() throws IOException, ParseException {

		URL url = MapProducer.class.getResource("layers.json");

		InputStream inp = url.openStream();

		try {

			MapLayerJSONParser layerJsonParser = new MapLayerJSONParser(props);

			Map<String, LayerDefinition> layerDefs = layerJsonParser.parse(url);

			MapLayerJSON layerJson = new MapLayerJSON(layerDefs);

			for (LayerDefinition layerDef : layerJson.getLayerDefs().values()) {

				log.info(" layer #" + layerDef.getLayerid() + "["
						+ layerDef.getWmsname() + "," + layerDef.getMinScale()
						+ "..." + layerDef.getMaxScale() + ","
						+ layerDef.getWmsurl() + "]");

				assertTrue(layerDef.getWmsname() != null
						|| !layerDef.getSubLayers().isEmpty());

				for (LayerDefinition subdef : layerDef.getSubLayers()) {
					assertTrue(subdef.getWmsname() != null);
					assertTrue(subdef.getWmsurl() != null);
					assertTrue(subdef.getLayerType() != null);
				}

				assertTrue(layerDef.getWmsurl() != null
						|| !layerDef.getSubLayers().isEmpty());

				assertTrue(layerDef.getLayerType() != null);

			}

		} finally {
			inp.close();
		}

	}

	@Test
	public void splitterMyPlacesURL() {

		String source = "http://nipsutu01.nls.fi:8080/geoserver/wms?buffer=128&tiled=yes&tilesorigin=0,0&CQL_FILTER=(uuid='e88fa3a1-5881-46d5-9929-20024f27a6d7'+OR+publisher_name+IS+NOT+NULL)+AND+category_id=437";
		System.out.println("SRC: " + source);
		String fixed = fixWmsUrl(source);
		System.out.println("FIX: " + fixed);

		String result = fixed.split("\\?")[1];

		System.out.println("RST: " + result);

		assertTrue(result
				.equals("buffer=128&tiled=yes&tilesorigin=0,0&CQL_FILTER=(uuid='e88fa3a1-5881-46d5-9929-20024f27a6d7'+OR+publisher_name+IS+NOT+NULL)+AND+category_id=437"));
	}

	@Test
	public void splitterBgMapsURL() {

		String source = "http://a.karttatiili.fi/dataset/taustakarttasarja/service/wms,http://b.karttatiili.fi/dataset/taustakarttasarja/service/wms,http://c.karttatiili.fi/dataset/taustakarttasarja/service/wms,http://d.karttatiili.fi/dataset/taustakarttasarja/service/wms";
		System.out.println(source);
		String fixed = fixWmsUrl(source);
		System.out.println(fixed);

		assertTrue(fixed
				.equals("http://a.karttatiili.fi/dataset/taustakarttasarja/service/wms"));

	}

	String fixWmsUrl(String url) {
		if (url == null) {
			return url;
		}

		if (url.indexOf(",http") != -1) {
			return url.substring(0, url.indexOf(",http"));
		}
		return url;
	}

}
