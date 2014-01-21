package fi.nls.oskari.printout.input.maplink;

import static org.junit.Assert.assertTrue;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import org.apache.log4j.PropertyConfigurator;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.geowebcache.GeoWebCacheException;
import org.geowebcache.config.XMLConfiguration;
import org.geowebcache.filter.request.RequestFilterException;
import org.geowebcache.grid.GridSubset;
import org.geowebcache.layer.TileLayer;
import org.junit.Before;
import org.junit.Test;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.Polygon;

import fi.nls.oskari.printout.input.layers.LayerDefinition;
import fi.nls.oskari.printout.input.layers.MapLayerJSONParser;
import fi.nls.oskari.printout.output.map.MapProducer;
import fi.nls.oskari.printout.output.map.MapProducerResource;
import fi.nls.oskari.printout.printing.PDFProducer;
import fi.nls.oskari.printout.ws.jaxrs.map.WebServiceMapProducerResource;
import fi.nls.oskari.printout.ws.jaxrs.resource.MapResource;

public class MapLinkJSONProcessorTest {

	final List<LayerDefinition> testLayerDefs = new ArrayList<LayerDefinition>();
	private String gridSubsetName = "EPSG_3067_MML";
	private String layerTemplate = "EPSG_3067_LAYER_TEMPLATE";
	WebServiceMapProducerResource shared;
	Properties props;

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

	@Before
	public void setupLayers() {
		LayerDefinition layerDefinition = new LayerDefinition();
		layerDefinition.setVisibility(true);
		layerDefinition.setWmsname("taustakartta_80k");
		layerDefinition
				.setWmsurl("http://karttatiili.fi/dataset/taustakarttasarja/service/wms");
		layerDefinition.setMinScale(56702d);
		layerDefinition.setMaxScale(40000d);
		layerDefinition.setOpacity(100);
		testLayerDefs.add(layerDefinition);
	}

	@Test
	public void testParseActionRouteMapLinkJSON() throws ParseException,
			IOException, GeoWebCacheException, XMLStreamException,
			FactoryConfigurationError, RequestFilterException,
			TransformException, COSVisitorException,
			NoSuchAuthorityCodeException, FactoryException,
			InterruptedException, com.vividsolutions.jts.io.ParseException {

		String mapLinkJSON = "action_route_liferay.json";

		XMLConfiguration config = shared.getConfig();

		TileLayer tileLayer = config.getTileLayer(layerTemplate);
		GridSubset gridSubset = tileLayer.getGridSubset(gridSubsetName);

		URL mapLinkJSONUrl = MapProducer.class.getResource(mapLinkJSON);

		MapLayerJSONParser mapLayerJsonParser = new MapLayerJSONParser(props);

		MapLink mapLink = mapLayerJsonParser.parseMapLinkJSON(mapLinkJSONUrl,
				shared.getGf(), gridSubset.getResolutions());

		/* we have to adjust args when using pageSize parameter */
		PDFProducer.Page page = PDFProducer.Page.valueOf(mapLink.getValues()
				.get("PAGESIZE"));

		int widthTarget = page.getWidthTargetInPoints();
		int heightTarget = page.getHeightTargetInPoints();

		mapLink.getValues().put("WIDTH", Integer.toString(widthTarget, 10));
		mapLink.getValues().put("HEIGHT", Integer.toString(heightTarget, 10));

		/* assertions */
		assertTrue(mapLink != null);
		assertTrue(mapLink.getScale() != null);

		assertTrue(mapLink.getCentre() != null);
		assertTrue(Double.valueOf(530163).equals(mapLink.getCentre().getX()));
		assertTrue(Double.valueOf(6754057).equals(mapLink.getCentre().getY()));

		assertTrue(Integer.valueOf(8).equals(mapLink.getZoom()));

		assertTrue(mapLink.getMapLinkLayers() != null);
		assertTrue(mapLink.getMapLinkLayers().size() == 5);

		assertTrue("base_35".equals(mapLink.getMapLinkLayers().get(0)
				.getLayerid()));

		assertTrue("myplaces_437".equals(mapLink.getMapLinkLayers().get(1)
				.getLayerid()));

		assertTrue("131".equals(mapLink.getMapLinkLayers().get(2).getLayerid()));

		assertTrue("251".equals(mapLink.getMapLinkLayers().get(3).getLayerid()));

		assertTrue(mapLink.getMapLinkLayers().get(3).getStyles().size() == 1);

		assertTrue(mapLink.getMapLinkLayers().get(3).getStyles()
				.get("bussipysakit").getTitle().equals("Katos"));
		assertTrue(mapLink.getMapLinkLayers().get(3).getStyles()
				.get("bussipysakit").getLegend() != null);
		assertTrue(mapLink.getMapLinkLayers().get(0).getGeom() == null);
		assertTrue(mapLink.getMapLinkLayers().get(1).getGeom() == null);
		assertTrue(mapLink.getMapLinkLayers().get(2).getGeom() == null);
		System.out.println(mapLink.getMapLinkLayers().get(3).getWmsname());
		assertTrue(mapLink.getMapLinkLayers().get(3).getGeom() != null);
		assertTrue(mapLink.getMapLinkLayers().get(3).getGeom() instanceof Polygon);
		assertTrue(mapLink.getMapLinkLayers().get(4).getGeom() != null);
		assertTrue(mapLink.getMapLinkLayers().get(4).getGeom() instanceof Polygon);

	}

}
