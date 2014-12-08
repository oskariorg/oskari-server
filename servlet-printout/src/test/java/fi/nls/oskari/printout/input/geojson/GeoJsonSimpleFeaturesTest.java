package fi.nls.oskari.printout.input.geojson;

import static org.junit.Assert.assertTrue;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Stack;

import org.apache.log4j.PropertyConfigurator;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.geojson.feature.FeatureJSON;
import org.geotools.map.FeatureLayer;
import org.geotools.styling.Style;
import org.geowebcache.config.XMLConfiguration;
import org.geowebcache.grid.GridSubset;
import org.geowebcache.layer.TileLayer;
import org.json.simple.parser.ParseException;
import org.junit.Before;
import org.junit.Test;
import org.opengis.feature.Feature;
import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.simple.SimpleFeature;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

import fi.nls.oskari.printout.input.layers.LayerDefinition;
import fi.nls.oskari.printout.input.layers.MapLayerJSONParser;
import fi.nls.oskari.printout.input.maplink.MapLink;
import fi.nls.oskari.printout.output.map.MapProducer;
import fi.nls.oskari.printout.output.map.MapProducerResource;
import fi.nls.oskari.printout.ws.jaxrs.map.WebServiceMapProducerResource;
import fi.nls.oskari.printout.ws.jaxrs.resource.MapResource;

public class GeoJsonSimpleFeaturesTest {

	/*
	 * JSON Path? https://code.google.com/p/json-path/
	 * https://github.com/jayway/JsonPath
	 */
	private String gridSubsetName = "EPSG_3067_MML";
	private String layerTemplate = "EPSG_3067_MML_LAYER_TEMPLATE";
	final List<LayerDefinition> testLayerDefs = new ArrayList<LayerDefinition>();

	WebServiceMapProducerResource shared;

	private Properties props;

	@Before	
	public void setUp() throws Exception {

		Properties properties = new Properties();

		properties.setProperty("log4j.logger.org.geowebcache", "WARN");

		properties.setProperty("log4j.logger.fi.paikkatietoikkuna", "DEBUG");
		properties.setProperty("lo44j.logger.org.apache.http.wire", "WARN");

		properties.setProperty("log4j.rootLogger", "WARN, A1");
		properties.setProperty("log4j.appender.A1",
				"org.apache.log4j.ConsoleAppender");
		properties.setProperty("log4j.appender.A1.layout",
				"org.apache.log4j.PatternLayout");
		properties.setProperty("log4j.appender.A1.layout.ConversionPattern",
				"%-4r [%t] %-5p %c %x - %m%n");
		/* "%c %x - %m%n"); */

		PropertyConfigurator.configure(properties);

		/** config */
		String conf = System.getProperty("fi.paikkatietoikkuna.imaging.config");

		props = new Properties();
		Reader r = conf != null ? new FileReader(conf) : new InputStreamReader(
				MapResource.class.getResourceAsStream("default.properties"));
		try {
			props.load(r);
		} finally {
			r.close();
		}

		shared = new WebServiceMapProducerResource(props);
		shared.setLayerJSONurl(MapProducerResource.class
				.getResource("blank-layers.json"));

	}

	@Test
	public void testParseGeoJSONFromJsonText() throws IOException {

		FeatureJSON fjson = new FeatureJSON();

		SimpleFeature f = fjson.readFeature(MapProducer.class
				.getResourceAsStream("feature.json"));

		assertTrue(f.getAttribute("geometry") instanceof Geometry);

	}

	

	@Test
	public void testParseMaplinkAndLayersWithGeoJSONFromJsonTest()
			throws IOException, ParseException,
			com.vividsolutions.jts.io.ParseException {

		XMLConfiguration config = shared.getConfig();

		TileLayer tileLayer = config.getTileLayer(layerTemplate);
		GridSubset gridSubset = tileLayer.getGridSubset(gridSubsetName);

		MapLayerJSONParser layerJsonParser = new MapLayerJSONParser(props);

		MaplinkGeoJsonParser parser = new MaplinkGeoJsonParser();
		parser.setDebug(true);

		InputStream inp = MapProducer.class
				.getResourceAsStream("geojsPrintTest.json");

		try {

			assertTrue(MapLinkGeoJsonParseContext.Default.getPm().buildMap);

			Map<String, ?> root = parser.parse(inp);

			assertTrue(root.size() != 0);
			assertTrue(root.get("layers") != null);
			assertTrue(root.get("maplink") != null);
			assertTrue(root.get("state") != null);

			assertTrue(root.get("layers") != null);

			MapLink mapLink = layerJsonParser.parseMapLinkJSON(root,
					shared.getGf(), gridSubset.getResolutions());

			assertTrue(mapLink != null);

		} finally {
			inp.close();
		}

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected Map<String, ?> getJSONFromURL(URL url) throws IOException {
		flexjson.JSONDeserializer deserializer = new flexjson.JSONDeserializer();

		InputStream inp = url.openStream();
		try {
			InputStreamReader reader = new InputStreamReader(inp);

			Map<String, ?> obj = (Map<String, ?>) deserializer
					.deserialize(reader);

			return obj;
		} finally {
			inp.close();
		}

	}

	@Test
	public void testStackEq() {

		Stack<String> s1 = new Stack<String>();
		Stack<String> s2 = new Stack<String>();

		for (String s : MapLinkGeoJsonParseContext.Data_Feature.getPath()) {
			s1.push(s);
		}
		for (String s : MapLinkGeoJsonParseContext.Data_Feature.getPath()) {
			s2.push(new String(s));
		}

		assertTrue(s1.equals(s2));
	}

	@Test
	public void testStackPartialEq() {

		Stack<String> s1 = new Stack<String>();
		Stack<String> s2 = new Stack<String>();

		for (String s : MapLinkGeoJsonParseContext.Data_Feature.getPath()) {
			s1.push(s);
		}
		for (String s : MapLinkGeoJsonParseContext.Data_Feature.getPath()) {
			s2.push(new String(s));
		}

		s2.push("[#]");

		assertTrue(s1.equals(s2.subList(0, s1.size())));
		assertTrue(!s1.equals(s2));
	}

}
