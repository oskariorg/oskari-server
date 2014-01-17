package fi.nls.oskari.printout.ws.jaxrs.map;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.util.Properties;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import org.geowebcache.GeoWebCacheException;
import org.geowebcache.filter.request.RequestFilterException;
import org.junit.Before;
import org.junit.Test;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.operation.TransformException;

import fi.nls.oskari.printout.ws.ProxySetup;
import fi.nls.oskari.printout.ws.jaxrs.map.WebServiceMapProducerResource;
import fi.nls.oskari.printout.ws.jaxrs.resource.MapResource;

/* 2nd generation tests - still valid */
public class WebServiceMapProducerResourceTest {

	WebServiceMapProducerResourceTestRunner runner = new WebServiceMapProducerResourceTestRunner();

	@Before
	public void setupProxy() throws IOException {

		new ProxySetup();
	}

	@Test
	public void testGeojsPrintTestJsonAsPDF() throws FactoryConfigurationError,
			Exception {
		runner.setResource(WebServiceMapProducerResourceTest.acquire());
		runner.run("geojsPrintTest",
				WebServiceMapProducerResourceTestFileType.GEOJSON,
				WebServiceMapProducerResourceTestFileType.PDF);

	}


	@Test
	public void testGeojsPrintTestJsonAsPPTX()
			throws NoSuchAuthorityCodeException, IOException,
			GeoWebCacheException, FactoryException, ParseException,
			XMLStreamException, FactoryConfigurationError,
			RequestFilterException, TransformException,
			com.vividsolutions.jts.io.ParseException, InterruptedException,
			org.json.simple.parser.ParseException, URISyntaxException,
			Exception {
		runner.setResource(WebServiceMapProducerResourceTest.acquire());
		runner.run("geojsPrintTest-pptx",
				WebServiceMapProducerResourceTestFileType.GEOJSON,
				WebServiceMapProducerResourceTestFileType.PPTX);

	}

	@Test
	public void testGeojsPrintTestJsonAsPNG()
			throws NoSuchAuthorityCodeException, IOException,
			GeoWebCacheException, FactoryException, ParseException,
			XMLStreamException, FactoryConfigurationError,
			RequestFilterException, TransformException,
			com.vividsolutions.jts.io.ParseException,
			org.json.simple.parser.ParseException, URISyntaxException,
			InterruptedException, Exception {
		runner.setResource(WebServiceMapProducerResourceTest.acquire());

		runner.run("geojsPrintTest",
				WebServiceMapProducerResourceTestFileType.GEOJSON,
				WebServiceMapProducerResourceTestFileType.PNG);

	}

	@Test
	public void testGeojsPrintTest20130423JsonAsPDF()
			throws NoSuchAuthorityCodeException, IOException,
			GeoWebCacheException, FactoryException, ParseException,
			XMLStreamException, FactoryConfigurationError,
			RequestFilterException, TransformException,
			com.vividsolutions.jts.io.ParseException, InterruptedException,
			org.json.simple.parser.ParseException, URISyntaxException,
			Exception {
		runner.setResource(WebServiceMapProducerResourceTest.acquire());
		runner.run("geojsPrintTest20130423",
				WebServiceMapProducerResourceTestFileType.GEOJSON,
				WebServiceMapProducerResourceTestFileType.PDF);

	}

	@Test
	public void testGeojsPrintTest20130423JsonAsPNG()
			throws NoSuchAuthorityCodeException, IOException,
			GeoWebCacheException, FactoryException, ParseException,
			XMLStreamException, FactoryConfigurationError,
			RequestFilterException, TransformException,
			com.vividsolutions.jts.io.ParseException,
			org.json.simple.parser.ParseException, URISyntaxException,
			InterruptedException, Exception {
		runner.setResource(WebServiceMapProducerResourceTest.acquire());

		runner.run("geojsPrintTest20130423",
				WebServiceMapProducerResourceTestFileType.GEOJSON,
				WebServiceMapProducerResourceTestFileType.PNG);

	}

	@Test
	public void testStatjsPrintTestJsonAsPNG()
			throws NoSuchAuthorityCodeException, IOException,
			GeoWebCacheException, FactoryException, ParseException,
			XMLStreamException, FactoryConfigurationError,
			RequestFilterException, TransformException,
			com.vividsolutions.jts.io.ParseException,
			org.json.simple.parser.ParseException, URISyntaxException,
			InterruptedException, Exception {
		runner.setResource(WebServiceMapProducerResourceTest.acquire());

		runner.run("testStatLayerPrint",
				WebServiceMapProducerResourceTestFileType.GEOJSON,
				WebServiceMapProducerResourceTestFileType.PNG);

	}
	
	@Test
	public void testStatjsPrintTestJsonAsPDF()
			throws NoSuchAuthorityCodeException, IOException,
			GeoWebCacheException, FactoryException, ParseException,
			XMLStreamException, FactoryConfigurationError,
			RequestFilterException, TransformException,
			com.vividsolutions.jts.io.ParseException,
			org.json.simple.parser.ParseException, URISyntaxException,
			InterruptedException, Exception {
		runner.setResource(WebServiceMapProducerResourceTest.acquire());

		runner.run("testStatLayerPrint",
				WebServiceMapProducerResourceTestFileType.GEOJSON,
				WebServiceMapProducerResourceTestFileType.PDF);

	}


	@Test
	public void testActionRouteJsonAsPNG() throws NoSuchAuthorityCodeException,
			IOException, GeoWebCacheException, FactoryException,
			ParseException, XMLStreamException, FactoryConfigurationError,
			RequestFilterException, TransformException,
			com.vividsolutions.jts.io.ParseException, URISyntaxException,
			InterruptedException, org.json.simple.parser.ParseException,
			Exception {
		runner.setResource(WebServiceMapProducerResourceTest.acquire());

		runner.run("action_route_liferay",
				WebServiceMapProducerResourceTestFileType.JSON,
				WebServiceMapProducerResourceTestFileType.PNG);
	}

	@Test
	public void testGeoJsonAsPNG() throws NoSuchAuthorityCodeException,
			IOException, GeoWebCacheException, FactoryException,
			ParseException, XMLStreamException, FactoryConfigurationError,
			RequestFilterException, TransformException,
			com.vividsolutions.jts.io.ParseException,
			org.json.simple.parser.ParseException, URISyntaxException,
			InterruptedException, Exception {
		runner.setResource(WebServiceMapProducerResourceTest.acquire());

		runner.run("action_route_parcel",
				WebServiceMapProducerResourceTestFileType.GEOJSON,
				WebServiceMapProducerResourceTestFileType.PNG);

	}

	@Test
	public void testActionRouteJsonAsPDF() throws NoSuchAuthorityCodeException,
			IOException, GeoWebCacheException, FactoryException,
			ParseException, XMLStreamException, FactoryConfigurationError,
			RequestFilterException, TransformException,
			com.vividsolutions.jts.io.ParseException, InterruptedException,
			URISyntaxException, org.json.simple.parser.ParseException,
			Exception {
		runner.setResource(WebServiceMapProducerResourceTest.acquire());

		runner.run("action_route_liferay",
				WebServiceMapProducerResourceTestFileType.JSON,
				WebServiceMapProducerResourceTestFileType.PDF);

	}

	@Test
	public void testGeojsPrintTestWithTilesJsonAsPNG()
			throws NoSuchAuthorityCodeException, IOException,
			GeoWebCacheException, FactoryException, ParseException,
			XMLStreamException, FactoryConfigurationError,
			RequestFilterException, TransformException,
			com.vividsolutions.jts.io.ParseException,
			org.json.simple.parser.ParseException, URISyntaxException,
			InterruptedException, Exception {
		runner.setResource(WebServiceMapProducerResourceTest.acquire());

		runner.run("geojsPrintTestWithTiles",
				WebServiceMapProducerResourceTestFileType.GEOJSON,
				WebServiceMapProducerResourceTestFileType.PNG);

	}

	@Test
	public void testGeojsLegendTestJsonAsPNG()
			throws NoSuchAuthorityCodeException, IOException,
			GeoWebCacheException, FactoryException, ParseException,
			XMLStreamException, FactoryConfigurationError,
			RequestFilterException, TransformException,
			com.vividsolutions.jts.io.ParseException,
			org.json.simple.parser.ParseException, URISyntaxException,
			InterruptedException, Exception {
		runner.setResource(WebServiceMapProducerResourceTest.acquire());

		runner.run("legend_test",
				WebServiceMapProducerResourceTestFileType.GEOJSON,
				WebServiceMapProducerResourceTestFileType.PNG);
	}

	@Test
	public void testWmtsPrintJsonAsPNG() throws NoSuchAuthorityCodeException,
			IOException, GeoWebCacheException, FactoryException,
			ParseException, XMLStreamException, FactoryConfigurationError,
			RequestFilterException, TransformException,
			com.vividsolutions.jts.io.ParseException, InterruptedException,
			URISyntaxException, org.json.simple.parser.ParseException {

		Properties props = new Properties();
		Reader r = new InputStreamReader(
				MapResource.class.getResourceAsStream("jhs.properties"));
		try {
			props.load(r);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			r.close();
		}

		props.store(System.out, "");

		WebServiceMapProducerResource resource = new WebServiceMapProducerResource(
				props);
		String layersUrlFromProps = props.getProperty("layersURL");
		resource.setLayerJSONurl(new URL(layersUrlFromProps));
		resource.setLayersDirty(false);
		runner.setResource(resource);

		runner.run("testWmtsLayerPrint",
				WebServiceMapProducerResourceTestFileType.JSON,
				WebServiceMapProducerResourceTestFileType.PNG);

	}
	
	@Test
	public void testWmtsPrintZoom3JsonAsPNG() throws NoSuchAuthorityCodeException,
			IOException, GeoWebCacheException, FactoryException,
			ParseException, XMLStreamException, FactoryConfigurationError,
			RequestFilterException, TransformException,
			com.vividsolutions.jts.io.ParseException, InterruptedException,
			URISyntaxException, org.json.simple.parser.ParseException {

		Properties props = new Properties();
		Reader r = new InputStreamReader(
				MapResource.class.getResourceAsStream("jhs.properties"));
		try {
			props.load(r);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			r.close();
		}

		props.store(System.out, "");

		WebServiceMapProducerResource resource = new WebServiceMapProducerResource(
				props);
		String layersUrlFromProps = props.getProperty("layersURL");
		resource.setLayerJSONurl(new URL(layersUrlFromProps));
		resource.setLayersDirty(false);
		runner.setResource(resource);

		runner.run("testWmtsLayerPrintZoom3",
				WebServiceMapProducerResourceTestFileType.JSON,
				WebServiceMapProducerResourceTestFileType.PNG);

	}
	
	@Test
	public void testWmtsPrintZoom7JsonAsPNG() throws NoSuchAuthorityCodeException,
			IOException, GeoWebCacheException, FactoryException,
			ParseException, XMLStreamException, FactoryConfigurationError,
			RequestFilterException, TransformException,
			com.vividsolutions.jts.io.ParseException, InterruptedException,
			URISyntaxException, org.json.simple.parser.ParseException {

		Properties props = new Properties();
		Reader r = new InputStreamReader(
				MapResource.class.getResourceAsStream("jhs.properties"));
		try {
			props.load(r);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			r.close();
		}

		props.store(System.out, "");

		WebServiceMapProducerResource resource = new WebServiceMapProducerResource(
				props);
		String layersUrlFromProps = props.getProperty("layersURL");
		resource.setLayerJSONurl(new URL(layersUrlFromProps));
		resource.setLayersDirty(false);
		runner.setResource(resource);

		runner.run("testWmtsLayerPrintZoom7",
				WebServiceMapProducerResourceTestFileType.JSON,
				WebServiceMapProducerResourceTestFileType.PNG);

	}
	
	@Test
	public void testWmtsPrintZoom7png8opacityJsonAsPNG() throws NoSuchAuthorityCodeException,
			IOException, GeoWebCacheException, FactoryException,
			ParseException, XMLStreamException, FactoryConfigurationError,
			RequestFilterException, TransformException,
			com.vividsolutions.jts.io.ParseException, InterruptedException,
			URISyntaxException, org.json.simple.parser.ParseException {

		Properties props = new Properties();
		Reader r = new InputStreamReader(
				MapResource.class.getResourceAsStream("jhs.properties"));
		try {
			props.load(r);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			r.close();
		}

		props.store(System.out, "");

		WebServiceMapProducerResource resource = new WebServiceMapProducerResource(
				props);
		String layersUrlFromProps = props.getProperty("layersURL");
		resource.setLayerJSONurl(new URL(layersUrlFromProps));
		resource.setLayersDirty(false);
		runner.setResource(resource);

		runner.run("testWmtsLayerPrintZoom7png8opacity",
				WebServiceMapProducerResourceTestFileType.GEOJSON,
				WebServiceMapProducerResourceTestFileType.PNG);

	}
	
	@Test
	public void testWmtsPrintZoom7png8opacityJsonAsPDF() throws NoSuchAuthorityCodeException,
			IOException, GeoWebCacheException, FactoryException,
			ParseException, XMLStreamException, FactoryConfigurationError,
			RequestFilterException, TransformException,
			com.vividsolutions.jts.io.ParseException, InterruptedException,
			URISyntaxException, org.json.simple.parser.ParseException {

		Properties props = new Properties();
		Reader r = new InputStreamReader(
				MapResource.class.getResourceAsStream("jhs.properties"));
		try {
			props.load(r);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			r.close();
		}

		props.store(System.out, "");

		WebServiceMapProducerResource resource = new WebServiceMapProducerResource(
				props);
		String layersUrlFromProps = props.getProperty("layersURL");
		resource.setLayerJSONurl(new URL(layersUrlFromProps));
		resource.setLayersDirty(false);
		runner.setResource(resource);

		runner.run("testWmtsLayerPrintZoom7png8opacity",
				WebServiceMapProducerResourceTestFileType.GEOJSON,
				WebServiceMapProducerResourceTestFileType.PDF);

	}

	/* FOR TESTING ONLY */
	/* synchronized for create on call only */
	static Object getmapResourceLock = new Object();
	static WebServiceMapProducerResource shared;

	public static WebServiceMapProducerResource acquire() throws Exception {
		synchronized (getmapResourceLock) {

			if (shared != null) {
				return shared;
			}

			String conf = System
					.getProperty("fi.paikkatietoikkuna.imaging.config");

			Properties props = new Properties();
			Reader r = conf != null ? new FileReader(conf)
					: new InputStreamReader(
							MapResource.class
									.getResourceAsStream("default.properties"));
			try {
				props.load(r);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				r.close();
			}

			props.store(System.out, "");

			shared = new WebServiceMapProducerResource(props);

			String layersUrlFromProps = props.getProperty("layersURL");

			URL layerJSONurl = null;

			if (layersUrlFromProps != null) {
				layerJSONurl = new URL(layersUrlFromProps);
			} else {
				layerJSONurl = new URL("http://n.a");
				shared.setLayersDirty(false);
			}

			shared.setLayerJSONurl(layerJSONurl);

			try {
				shared.loadLayerJson();
			} catch (IOException ioe) {
				if (shared.getLayerJson() != null) {
					/* we'll use the old one */
				} else {
					throw ioe;
				}
			}

		}

		return shared;
	}

}
