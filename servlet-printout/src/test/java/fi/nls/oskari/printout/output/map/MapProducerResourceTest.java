package fi.nls.oskari.printout.output.map;

import static org.junit.Assert.assertTrue;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.PropertyConfigurator;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.geowebcache.GeoWebCacheException;
import org.geowebcache.filter.request.RequestFilterException;
import org.geowebcache.grid.GridSubset;
import org.geowebcache.layer.TileLayer;
import org.junit.Before;
import org.junit.Test;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

import fi.nls.oskari.printout.imaging.ScaleOps;
import fi.nls.oskari.printout.input.layers.LayerDefinition;
import fi.nls.oskari.printout.input.maplink.MapLink;
import fi.nls.oskari.printout.input.maplink.MapLinkParser;
import fi.nls.oskari.printout.output.layer.AsyncLayerProcessor;
import fi.nls.oskari.printout.output.map.MetricScaleResolutionUtils.ScaleResolution;
import fi.nls.oskari.printout.printing.PDFProducer;
import fi.nls.oskari.printout.ws.ProxySetup;
import fi.nls.oskari.printout.ws.jaxrs.resource.MapResource;

/* 1st generation tests mostly obsolete */
public class MapProducerResourceTest {

	private String layerTemplate = "EPSG_3067_LAYER_TEMPLATE";
	private String gridSubsetName = "EPSG_3067_MML";
	private URL layersUrl;
	private ScaleOps scaleOps = new ScaleOps();
	private static Log log = LogFactory.getLog(MapProducerResourceTest.class);
	private ScaleResolution scaleResolution = MetricScaleResolutionUtils
			.getScaleResolver("m_ol212");

	final List<LayerDefinition> testLayerDefs = new ArrayList<LayerDefinition>();

	MapProducerResource resource;

	private BufferedImage doScaleWithFilters(BufferedImage image, int width,
			int height) throws IOException {

		return scaleOps.doScaleWithFilters(image, width, height);
	}

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

		Properties props = new Properties();
		Reader r = conf != null ? new FileReader(conf) : new InputStreamReader(
				MapResource.class.getResourceAsStream("default.properties"));
		try {
			props.load(r);
		} finally {
			r.close();
		}

		layersUrl = new URL(
				"http://localhost/dataset/layers/service/json?p_p_id=Portti2Map_WAR_portti2mapportlet&p_p_lifecycle=2&action_route=GetMapLayers&lang=fi");

		resource = new MapProducerResource(props) {

		};
		resource.setLayerJSONurl(layersUrl);

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
		layerDefinition.setLayerType("wmslayer");
		testLayerDefs.add(layerDefinition);
	}

	@Before
	public void setupNlsFiProxy() throws IOException {
		new ProxySetup();

	}

	@Test
	public void testAdjustToReasonableScale() throws ParseException,
			IOException, GeoWebCacheException, XMLStreamException,
			FactoryConfigurationError, RequestFilterException,
			TransformException, COSVisitorException,
			NoSuchAuthorityCodeException, FactoryException,
			com.vividsolutions.jts.io.ParseException {
		MapProducer producer = resource.fork(null);
		String mapLinkArgument = "zoomLevel=8&coord=422177_6764993&mapLayers=base_45+100+,35+100+,base_2+40+&"
				+ "showMarker=false&forceCache=true&noSavedState=true";

		String additionalQueryParams = mapLinkArgument
				+ "&width=500&height=600";

		String queryParams = URLDecoder.decode(mapLinkArgument
				+ additionalQueryParams, "UTF-8");

		MapLinkParser mapLinkParser = new MapLinkParser(scaleResolution, producer.getZoomOffset());

		TileLayer tileLayer = resource.getConfig().getTileLayer(layerTemplate);
		GridSubset gridSubset = tileLayer.getGridSubset(gridSubsetName);

		MapLink mapLink = mapLinkParser.parseURLMapLink(queryParams,
				resource.getLayerJson(), resource.getGf(),
				gridSubset.getResolutions());
		assertTrue(mapLink != null);
		assertTrue(mapLink.getScale() != null);
		assertTrue(mapLink.getScale().equals(Double.valueOf(11338.5888d)));

		assertTrue(mapLink.getCentre() != null);
		assertTrue(Double.valueOf(422177).equals(mapLink.getCentre().getX()));
		assertTrue(Double.valueOf(6764993).equals(mapLink.getCentre().getY()));

		assertTrue(Integer.valueOf(8).equals(mapLink.getZoom()));

		assertTrue(mapLink.getMapLinkLayers() != null);
		assertTrue(mapLink.getMapLinkLayers().size() == 3);

		assertTrue("base_45".equals(mapLink.getMapLinkLayers().get(0)
				.getLayerid()));
		assertTrue("35".equals(mapLink.getMapLinkLayers().get(1).getLayerid()));
		assertTrue("base_2".equals(mapLink.getMapLinkLayers().get(2)
				.getLayerid()));

		Point centre = resource.getGf().createPoint(
				new Coordinate(422766, 6765178));

		int width = mapLink.getWidth();
		int height = mapLink.getHeight();

		Envelope env = producer.getProcessor().getEnvFromPointZoomAndExtent(
				centre, mapLink.getZoom(), width, height);

		double inches = width / 72.0;
		double centimeters = inches * 2.54;
		double scaleInMeters = Math.floor(mapLink.getScale() / 5000) * 5000;
		double widthInMeters = centimeters * scaleInMeters / 100;

		Envelope envAdj = new Envelope(centre.getX() - widthInMeters / 2.0,
				centre.getX() + widthInMeters / 2.0, centre.getY()
						- widthInMeters / 2.0, centre.getY() + widthInMeters
						/ 2.0);

		double adjX = env.getWidth() / envAdj.getWidth();
		double adjY = env.getHeight() / envAdj.getHeight();

		double resolution = gridSubset.getResolutions()[mapLink.getZoom()];

		log.info("cm for " + width + "points " + centimeters);
		log.info("RESOLUTION " + resolution);
		log.info("SCALE " + mapLink.getScale());
		log.info("WIDTH  in METERS" + env.getWidth());
		log.info("WIDTHAdj in METERS" + envAdj.getWidth());
		log.info("SCALE RESULT % " + adjX + "," + adjY);
		log.info("SCALE RESULT px " + adjX * width + "," + adjY * width);
		log.info("BBOX " + env);
		log.info("BBOXAdj " + envAdj);
		log.info("-----------");

	}

	/**
	 * 
	 * @throws FactoryConfigurationError
	 * @throws Exception
	 */
	@Test
	public void testAllInPrintout() throws FactoryConfigurationError, Exception {

		/*
		 * testataan otsikot, pvm, logo, mittakaava
		 */

		MapProducer producer = resource.fork(null);
		String testname = "all-in";
		String outputFile = MapProducerResourceTestFileType.PDF
				.getFilename(testname);

		String mapLinkArgument = "zoomLevel=8&coord=422177_6764993&mapLayers=base_45+100+,35+100+,base_2+40+&"
				+ "showMarker=false&forceCache=true&noSavedState=true&pageTitle=Hello";

		int widthTarget = Double.valueOf((20.9 - 1 * 2) / 2.54 * 72).intValue();
		int heightTarget = Double.valueOf((int) (29.7 - 1 * 2) / 2.54 * 72)
				.intValue();

		String additionalQueryParams = mapLinkArgument + "&WIDTH="
				+ widthTarget + "&height=" + heightTarget;

		String queryParams = URLDecoder.decode(mapLinkArgument
				+ additionalQueryParams, "UTF-8");

		MapLinkParser mapLinkParser = new MapLinkParser(scaleResolution, producer.getZoomOffset());

		TileLayer tileLayer = resource.getConfig().getTileLayer(layerTemplate);
		GridSubset gridSubset = tileLayer.getGridSubset(gridSubsetName);

		MapLink mapLink = mapLinkParser.parseURLMapLink(queryParams,
				resource.getLayerJson(), resource.getGf(),
				gridSubset.getResolutions());
		assertTrue(mapLink != null);
		assertTrue(mapLink.getScale() != null);
		assertTrue(mapLink.getScale().equals(Double.valueOf(11338.5888d)));

		assertTrue(mapLink.getCentre() != null);
		assertTrue(Double.valueOf(422177).equals(mapLink.getCentre().getX()));
		assertTrue(Double.valueOf(6764993).equals(mapLink.getCentre().getY()));

		assertTrue(Integer.valueOf(8).equals(mapLink.getZoom()));

		assertTrue(mapLink.getMapLinkLayers() != null);
		assertTrue(mapLink.getMapLinkLayers().size() == 3);

		assertTrue("base_45".equals(mapLink.getMapLinkLayers().get(0)
				.getLayerid()));
		assertTrue("35".equals(mapLink.getMapLinkLayers().get(1).getLayerid()));
		assertTrue("base_2".equals(mapLink.getMapLinkLayers().get(2)
				.getLayerid()));

		Point centre = resource.getGf().createPoint(
				new Coordinate(422766, 6765178));

		int width = mapLink.getWidth();
		int height = mapLink.getHeight();

		ArrayList<BufferedImage> images = new ArrayList<BufferedImage>();

		Envelope env = producer.getProcessor().getEnvFromPointZoomAndExtent(
				centre, mapLink.getZoom(), width, height);

		String[] layerNames = new String[] { "vino_20k", "mtk_vesistot",
				"peruskartta" };

		AsyncLayerProcessor asyncProc = new AsyncLayerProcessor();
		asyncProc.start();
		try {

			int n = 0;
			for (LayerDefinition ldef : mapLink.getMapLinkLayers()) {

				LayerDefinition inScale = mapLink
						.selectLayerDefinitionForScale(ldef);
				if (inScale == null) {
					continue;
				}

				log.info("Checking " + layerNames[n] + " vs. selected "
						+ inScale.getWmsname());
				assertTrue(layerNames[n].equals(inScale.getWmsname()));

				n++;

				final List<LayerDefinition> overlayLayers = new ArrayList<LayerDefinition>(
						1);

				overlayLayers.add(inScale);

				BufferedImage image = producer.getMap(asyncProc, env,
						mapLink.getZoom(), width, height, overlayLayers,
						MapProducer.ImageType.ARGB);

				BufferedImage scaledImage = doScaleWithFilters(image,
						width * 2, height * 2);

				images.add(scaledImage);

				image.flush();
				try {
					File fileToSave = new File(
							MapProducerResourceTestFileType.PNG
									.getFilename(testname + "-" + n));
					ImageIO.write(scaledImage, "png", fileToSave);

				} catch (IOException e) {
					throw new RuntimeException(e);
				}

			}

		} finally {
			asyncProc.shutdown();
		}

		PDFProducer.Options opts = new PDFProducer.Options();
		opts.setPageTitle("Jebujee");
		opts.setPageDate(true);
		opts.setPageLogo(true);
		opts.setPageDate(true);
		opts.setPageScale(true);

		PDFProducer pdf = new PDFProducer(PDFProducer.Page.A4, opts,
				producer.getCrs());
		pdf.createLayeredPDFFromImages(images, outputFile, width, height, env,
				centre);

		for (BufferedImage image : images) {
			image.flush();

		}
	}

	@Test
	public void testConfiguration() {

		log.info("Tile Layers " + resource.getConfig().getTileLayerCount());

		Set<String> tileLayersNames = resource.getConfig().getTileLayerNames();
		Iterator<String> iter = tileLayersNames.iterator();

		while (iter.hasNext()) {
			String s = iter.next();

			log.info("Layer " + s);
		}

		/**
    	 * 
    	 */
	}

	@Test
	public void testMapLinkToPDFWithMultiUpscaledImageLayersInReasonableScaleA3()
			throws FactoryConfigurationError, Exception {
		MapProducer producer = resource.fork(null);
		String testname = "maplink-multilayers-reasonable-a3";
		String outputFile = MapProducerResourceTestFileType.PDF
				.getFilename(testname);

		String mapLinkArgument = "zoomLevel=8&coord=422177_6764993&mapLayers=base_45+100+,35+100+,base_2+40+&"
				+ "showMarker=false&forceCache=true&noSavedState=true";

		int widthTarget = Double.valueOf((29.7 - 1 * 2) / 2.54 * 72).intValue();
		int heightTarget = Double.valueOf((42.0 - 1 * 2) / 2.54 * 72)
				.intValue();

		String additionalQueryParams = mapLinkArgument + "&WIDTH="
				+ widthTarget + "&height=" + heightTarget;

		String queryParams = URLDecoder.decode(mapLinkArgument
				+ additionalQueryParams, "UTF-8");

		MapLinkParser mapLinkParser = new MapLinkParser(scaleResolution,producer.getZoomOffset());

		TileLayer tileLayer = resource.getConfig().getTileLayer(layerTemplate);
		GridSubset gridSubset = tileLayer.getGridSubset(gridSubsetName);

		MapLink mapLink = mapLinkParser.parseURLMapLink(queryParams,
				resource.getLayerJson(), resource.getGf(),
				gridSubset.getResolutions());

		assertTrue(mapLink != null);
		assertTrue(mapLink.getScale() != null);
		assertTrue(mapLink.getScale().equals(Double.valueOf(11338.5888d)));

		assertTrue(mapLink.getCentre() != null);
		assertTrue(Double.valueOf(422177).equals(mapLink.getCentre().getX()));
		assertTrue(Double.valueOf(6764993).equals(mapLink.getCentre().getY()));

		assertTrue(Integer.valueOf(8).equals(mapLink.getZoom()));

		assertTrue(mapLink.getMapLinkLayers() != null);
		assertTrue(mapLink.getMapLinkLayers().size() == 3);

		assertTrue("base_45".equals(mapLink.getMapLinkLayers().get(0)
				.getLayerid()));
		assertTrue("35".equals(mapLink.getMapLinkLayers().get(1).getLayerid()));
		assertTrue("base_2".equals(mapLink.getMapLinkLayers().get(2)
				.getLayerid()));

		Point centre = resource.getGf().createPoint(
				new Coordinate(422766, 6765178));

		int width = mapLink.getWidth();
		int height = mapLink.getHeight();

		ArrayList<BufferedImage> images = new ArrayList<BufferedImage>();

		Envelope env = producer.getProcessor().getEnvFromPointZoomAndExtent(
				centre, mapLink.getZoom(), width, height);

		double widthInches = width / 72.0;
		double widthCm = widthInches * 2.54;
		double scaleInMeters = Math.floor(mapLink.getScale() / 5000) * 5000;
		double widthInMeters = widthCm * scaleInMeters / 100;

		double heightInches = height / 72.0;
		double heightCm = heightInches * 2.54;
		double heightInMeters = heightCm * scaleInMeters / 100;

		Envelope envAdj = new Envelope(centre.getX() - widthInMeters / 2.0,
				centre.getX() + widthInMeters / 2.0, centre.getY()
						- heightInMeters / 2.0, centre.getY() + heightInMeters
						/ 2.0);

		double adjX = env.getWidth() / envAdj.getWidth();
		double adjY = env.getHeight() / envAdj.getHeight();

		int adjWidth = Double.valueOf(adjX * width).intValue();
		int adjHeight = Double.valueOf(adjY * height).intValue();

		Envelope envReq = producer.getProcessor().getEnvFromPointZoomAndExtent(
				centre, mapLink.getZoom(), adjWidth, adjHeight);

		log.info("cm for " + width + "points " + widthCm + "," + heightCm);

		log.info("SCALE " + mapLink.getScale());
		log.info("WIDTH  in METERS" + env.getWidth());
		log.info("WIDTHAdj in METERS" + envAdj.getWidth());
		log.info("SCALE RESULT % " + adjX + "," + adjY);
		log.info("SCALE RESULT px " + adjX * width + "," + adjY * width);
		log.info("BBOX " + env);
		log.info("BBOXAdj " + envAdj);
		log.info("Adj WxH " + adjWidth + "x" + adjHeight);
		log.info("BBOXReq " + envReq);
		log.info("-----------");

		String[] layerNames = new String[] { "vino_20k", "mtk_vesistot",
				"peruskartta" };

		AsyncLayerProcessor asyncProc = new AsyncLayerProcessor();
		asyncProc.start();
		try {
			int n = 0;
			for (LayerDefinition ldef : mapLink.getMapLinkLayers()) {

				LayerDefinition inScale = mapLink
						.selectLayerDefinitionForScale(ldef);
				if (inScale == null) {
					continue;
				}

				log.info("Checking " + layerNames[n] + " vs. selected "
						+ inScale.getWmsname());
				assertTrue(layerNames[n].equals(inScale.getWmsname()));

				n++;

				final List<LayerDefinition> overlayLayers = new ArrayList<LayerDefinition>(
						1);

				overlayLayers.add(inScale);
				BufferedImage image = producer.getMap(asyncProc, envReq,
						mapLink.getZoom(), adjWidth, adjHeight, overlayLayers,
						MapProducer.ImageType.ARGB, envAdj);

				BufferedImage scaledImage = doScaleWithFilters(image,
						width * 2, height * 2);

				images.add(scaledImage);

				image.flush();

				try {
					File fileToSave = new File(
							MapProducerResourceTestFileType.PNG
									.getFilename(testname + "_" + n));

					ImageIO.write(scaledImage, "png", fileToSave);

				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		} finally {
			asyncProc.shutdown();
		}

		PDFProducer pdf = new PDFProducer(PDFProducer.Page.A3);
		pdf.createLayeredPDFFromImages(images, outputFile, width, height, env,
				centre);

		for (BufferedImage image : images) {
			image.flush();

		}
	}

	@Test
	public void testMapLinkToPDFWithMultiUpscaledImageLayersInReasonableScaleA3Landscape()
			throws FactoryConfigurationError, Exception {
		MapProducer producer = resource.fork(null);
		String testname = "maplink-multilayers-reasonable-a3l";
		String outputFile = MapProducerResourceTestFileType.PDF
				.getFilename(testname);

		String mapLinkArgument = "zoomLevel=8&coord=422177_6764993&mapLayers=base_45+100+,35+100+,base_2+40+&"
				+ "showMarker=false&forceCache=true&noSavedState=true";

		int widthTarget = Double.valueOf((42.0 - 1 * 2) / 2.54 * 72).intValue();
		int heightTarget = Double.valueOf((29.7 - 1 * 2) / 2.54 * 72)
				.intValue();

		String additionalQueryParams = mapLinkArgument + "&WIDTH="
				+ widthTarget + "&height=" + heightTarget;

		String queryParams = URLDecoder.decode(mapLinkArgument
				+ additionalQueryParams, "UTF-8");

		MapLinkParser mapLinkParser = new MapLinkParser(scaleResolution,producer.getZoomOffset());

		TileLayer tileLayer = resource.getConfig().getTileLayer(layerTemplate);
		GridSubset gridSubset = tileLayer.getGridSubset(gridSubsetName);

		MapLink mapLink = mapLinkParser.parseURLMapLink(queryParams,
				resource.getLayerJson(), resource.getGf(),
				gridSubset.getResolutions());
		assertTrue(mapLink != null);
		assertTrue(mapLink.getScale() != null);
		assertTrue(mapLink.getScale().equals(Double.valueOf(11338.5888d)));

		assertTrue(mapLink.getCentre() != null);
		assertTrue(Double.valueOf(422177).equals(mapLink.getCentre().getX()));
		assertTrue(Double.valueOf(6764993).equals(mapLink.getCentre().getY()));

		assertTrue(Integer.valueOf(8).equals(mapLink.getZoom()));

		assertTrue(mapLink.getMapLinkLayers() != null);
		assertTrue(mapLink.getMapLinkLayers().size() == 3);

		assertTrue("base_45".equals(mapLink.getMapLinkLayers().get(0)
				.getLayerid()));
		assertTrue("35".equals(mapLink.getMapLinkLayers().get(1).getLayerid()));
		assertTrue("base_2".equals(mapLink.getMapLinkLayers().get(2)
				.getLayerid()));

		Point centre = resource.getGf().createPoint(
				new Coordinate(422766, 6765178));

		int width = mapLink.getWidth();
		int height = mapLink.getHeight();

		ArrayList<BufferedImage> images = new ArrayList<BufferedImage>();

		Envelope env = producer.getProcessor().getEnvFromPointZoomAndExtent(
				centre, mapLink.getZoom(), width, height);

		double widthInches = width / 72.0;
		double widthCm = widthInches * 2.54;
		double scaleInMeters = Math.floor(mapLink.getScale() / 5000) * 5000;
		double widthInMeters = widthCm * scaleInMeters / 100;

		double heightInches = height / 72.0;
		double heightCm = heightInches * 2.54;
		double heightInMeters = heightCm * scaleInMeters / 100;

		Envelope envAdj = new Envelope(centre.getX() - widthInMeters / 2.0,
				centre.getX() + widthInMeters / 2.0, centre.getY()
						- heightInMeters / 2.0, centre.getY() + heightInMeters
						/ 2.0);

		double adjX = env.getWidth() / envAdj.getWidth();
		double adjY = env.getHeight() / envAdj.getHeight();

		int adjWidth = Double.valueOf(adjX * width).intValue();
		int adjHeight = Double.valueOf(adjY * height).intValue();

		Envelope envReq = producer.getProcessor().getEnvFromPointZoomAndExtent(
				centre, mapLink.getZoom(), adjWidth, adjHeight);

		log.info("cm for " + width + "points " + widthCm + "," + heightCm);

		log.info("SCALE " + mapLink.getScale());
		log.info("WIDTH  in METERS" + env.getWidth());
		log.info("WIDTHAdj in METERS" + envAdj.getWidth());
		log.info("SCALE RESULT % " + adjX + "," + adjY);
		log.info("SCALE RESULT px " + adjX * width + "," + adjY * width);
		log.info("BBOX " + env);
		log.info("BBOXAdj " + envAdj);
		log.info("Adj WxH " + adjWidth + "x" + adjHeight);
		log.info("BBOXReq " + envReq);
		log.info("-----------");

		String[] layerNames = new String[] { "vino_20k", "mtk_vesistot",
				"peruskartta" };
		AsyncLayerProcessor asyncProc = new AsyncLayerProcessor();
		asyncProc.start();
		try {
			int n = 0;
			for (LayerDefinition ldef : mapLink.getMapLinkLayers()) {

				LayerDefinition inScale = mapLink
						.selectLayerDefinitionForScale(ldef);
				if (inScale == null) {
					continue;
				}

				log.info("Checking " + layerNames[n] + " vs. selected "
						+ inScale.getWmsname());
				assertTrue(layerNames[n].equals(inScale.getWmsname()));

				n++;

				final List<LayerDefinition> overlayLayers = new ArrayList<LayerDefinition>(
						1);

				overlayLayers.add(inScale);
				BufferedImage image = producer.getMap(asyncProc, envReq,
						mapLink.getZoom(), adjWidth, adjHeight, overlayLayers,
						MapProducer.ImageType.ARGB, envAdj);

				BufferedImage scaledImage = doScaleWithFilters(image,
						width * 2, height * 2);

				images.add(scaledImage);

				image.flush();

				try {
					File fileToSave = new File(
							MapProducerResourceTestFileType.PNG
									.getFilename(testname + "_" + n));
					ImageIO.write(scaledImage, "png", fileToSave);

				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		} finally {
			asyncProc.shutdown();
		}
		PDFProducer pdf = new PDFProducer(PDFProducer.Page.A3_Landscape);
		pdf.createLayeredPDFFromImages(images, outputFile, width, height, env,
				centre);

		for (BufferedImage image : images) {
			image.flush();

		}
	}

	@Test
	public void testMapLinkToPDFWithMultiUpscaledImageLayersInReasonableScaleA4()
			throws FactoryConfigurationError, Exception {

		MapProducer producer = resource.fork(null);
		String testname = "maplink-multilayers-reasonable";
		String outputFile = MapProducerResourceTestFileType.PDF
				.getFilename(testname);

		String mapLinkArgument = "zoomLevel=8&coord=422177_6764993&mapLayers=base_45+100+,35+100+,base_2+40+&"
				+ "showMarker=false&forceCache=true&noSavedState=true";

		int widthTarget = Double.valueOf((20.9 - 1 * 2) / 2.54 * 72).intValue();
		int heightTarget = Double.valueOf((int) (29.7 - 1 * 2) / 2.54 * 72)
				.intValue();

		String additionalQueryParams = mapLinkArgument + "&WIDTH="
				+ widthTarget + "&height=" + heightTarget;

		String queryParams = URLDecoder.decode(mapLinkArgument
				+ additionalQueryParams, "UTF-8");

		MapLinkParser mapLinkParser = new MapLinkParser(scaleResolution,producer.getZoomOffset());

		TileLayer tileLayer = resource.getConfig().getTileLayer(layerTemplate);
		GridSubset gridSubset = tileLayer.getGridSubset(gridSubsetName);

		MapLink mapLink = mapLinkParser.parseURLMapLink(queryParams,
				resource.getLayerJson(), resource.getGf(),
				gridSubset.getResolutions());

		assertTrue(mapLink != null);
		assertTrue(mapLink.getScale() != null);
		assertTrue(mapLink.getScale().equals(Double.valueOf(11338.5888d)));

		assertTrue(mapLink.getCentre() != null);
		assertTrue(Double.valueOf(422177).equals(mapLink.getCentre().getX()));
		assertTrue(Double.valueOf(6764993).equals(mapLink.getCentre().getY()));

		assertTrue(Integer.valueOf(8).equals(mapLink.getZoom()));

		assertTrue(mapLink.getMapLinkLayers() != null);
		assertTrue(mapLink.getMapLinkLayers().size() == 3);

		assertTrue("base_45".equals(mapLink.getMapLinkLayers().get(0)
				.getLayerid()));
		assertTrue("35".equals(mapLink.getMapLinkLayers().get(1).getLayerid()));
		assertTrue("base_2".equals(mapLink.getMapLinkLayers().get(2)
				.getLayerid()));

		Point centre = resource.getGf().createPoint(
				new Coordinate(422766, 6765178));

		int width = mapLink.getWidth();
		int height = mapLink.getHeight();

		ArrayList<BufferedImage> images = new ArrayList<BufferedImage>();

		Envelope env = producer.getProcessor().getEnvFromPointZoomAndExtent(
				centre, mapLink.getZoom(), width, height);

		double widthInches = width / 72.0;
		double widthCm = widthInches * 2.54;
		double scaleInMeters = Math.floor(mapLink.getScale() / 5000) * 5000;
		double widthInMeters = widthCm * scaleInMeters / 100;

		double heightInches = height / 72.0;
		double heightCm = heightInches * 2.54;
		double heightInMeters = heightCm * scaleInMeters / 100;

		Envelope envAdj = new Envelope(centre.getX() - widthInMeters / 2.0,
				centre.getX() + widthInMeters / 2.0, centre.getY()
						- heightInMeters / 2.0, centre.getY() + heightInMeters
						/ 2.0);

		double adjX = env.getWidth() / envAdj.getWidth();
		double adjY = env.getHeight() / envAdj.getHeight();

		int adjWidth = Double.valueOf(adjX * width).intValue();
		int adjHeight = Double.valueOf(adjY * height).intValue();

		Envelope envReq = producer.getProcessor().getEnvFromPointZoomAndExtent(
				centre, mapLink.getZoom(), adjWidth, adjHeight);

		log.info("cm for " + width + "points " + widthCm + "," + heightCm);

		log.info("SCALE " + mapLink.getScale());
		log.info("WIDTH  in METERS" + env.getWidth());
		log.info("WIDTHAdj in METERS" + envAdj.getWidth());
		log.info("SCALE RESULT % " + adjX + "," + adjY);
		log.info("SCALE RESULT px " + adjX * width + "," + adjY * width);
		log.info("BBOX " + env);
		log.info("BBOXAdj " + envAdj);
		log.info("Adj WxH " + adjWidth + "x" + adjHeight);
		log.info("BBOXReq " + envReq);
		log.info("-----------");

		String[] layerNames = new String[] { "vino_20k", "mtk_vesistot",
				"peruskartta" };
		AsyncLayerProcessor asyncProc = new AsyncLayerProcessor();
		asyncProc.start();
		try {

			int n = 0;
			for (LayerDefinition ldef : mapLink.getMapLinkLayers()) {

				LayerDefinition inScale = mapLink
						.selectLayerDefinitionForScale(ldef);
				if (inScale == null) {
					continue;
				}

				log.info("Checking " + layerNames[n] + " vs. selected "
						+ inScale.getWmsname());
				assertTrue(layerNames[n].equals(inScale.getWmsname()));

				n++;

				final List<LayerDefinition> overlayLayers = new ArrayList<LayerDefinition>(
						1);

				overlayLayers.add(inScale);
				BufferedImage image = producer.getMap(asyncProc, envReq,
						mapLink.getZoom(), adjWidth, adjHeight, overlayLayers,
						MapProducer.ImageType.ARGB, envAdj);

				BufferedImage scaledImage = doScaleWithFilters(image,
						width * 2, height * 2);

				images.add(scaledImage);

				image.flush();

				try {
					File fileToSave = new File(
							MapProducerResourceTestFileType.PNG
									.getFilename(testname + "-" + n));

					ImageIO.write(scaledImage, "png", fileToSave);

				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}

		} finally {
			asyncProc.shutdown();
		}

		PDFProducer pdf = new PDFProducer(PDFProducer.Page.A4);
		pdf.createLayeredPDFFromImages(images, outputFile, width, height, env,
				centre);

		for (BufferedImage image : images) {
			image.flush();

		}
	}

	@Test
	public void testMapLinkToPDFWithMultiUpscaledImageLayersInReasonableScaleA4Landscape()
			throws FactoryConfigurationError, Exception {
		MapProducer producer = resource.fork(null);
		String testname = "maplink-multilayers-reasonable-a4l";
		String outputFile = MapProducerResourceTestFileType.PDF
				.getFilename(testname);

		String mapLinkArgument = "zoomLevel=8&coord=422177_6764993&mapLayers=base_45+100+,35+100+,base_2+40+&"
				+ "showMarker=false&forceCache=true&noSavedState=true";

		int widthTarget = Double.valueOf((29.7 - 1 * 2) / 2.54 * 72).intValue();
		int heightTarget = Double.valueOf((20.9 - 1 * 2) / 2.54 * 72)
				.intValue();

		String additionalQueryParams = mapLinkArgument + "&WIDTH="
				+ widthTarget + "&height=" + heightTarget;

		String queryParams = URLDecoder.decode(mapLinkArgument
				+ additionalQueryParams, "UTF-8");

		MapLinkParser mapLinkParser = new MapLinkParser(scaleResolution,producer.getZoomOffset());

		TileLayer tileLayer = resource.getConfig().getTileLayer(layerTemplate);
		GridSubset gridSubset = tileLayer.getGridSubset(gridSubsetName);

		MapLink mapLink = mapLinkParser.parseURLMapLink(queryParams,
				resource.getLayerJson(), resource.getGf(),
				gridSubset.getResolutions());

		assertTrue(mapLink != null);
		assertTrue(mapLink.getScale() != null);
		assertTrue(mapLink.getScale().equals(Double.valueOf(11338.5888d)));

		assertTrue(mapLink.getCentre() != null);
		assertTrue(Double.valueOf(422177).equals(mapLink.getCentre().getX()));
		assertTrue(Double.valueOf(6764993).equals(mapLink.getCentre().getY()));

		assertTrue(Integer.valueOf(8).equals(mapLink.getZoom()));

		assertTrue(mapLink.getMapLinkLayers() != null);
		assertTrue(mapLink.getMapLinkLayers().size() == 3);

		assertTrue("base_45".equals(mapLink.getMapLinkLayers().get(0)
				.getLayerid()));
		assertTrue("35".equals(mapLink.getMapLinkLayers().get(1).getLayerid()));
		assertTrue("base_2".equals(mapLink.getMapLinkLayers().get(2)
				.getLayerid()));

		Point centre = resource.getGf().createPoint(
				new Coordinate(422766, 6765178));

		int width = mapLink.getWidth();
		int height = mapLink.getHeight();

		ArrayList<BufferedImage> images = new ArrayList<BufferedImage>();

		Envelope env = producer.getProcessor().getEnvFromPointZoomAndExtent(
				centre, mapLink.getZoom(), width, height);

		double widthInches = width / 72.0;
		double widthCm = widthInches * 2.54;
		double scaleInMeters = Math.floor(mapLink.getScale() / 5000) * 5000;
		double widthInMeters = widthCm * scaleInMeters / 100;

		double heightInches = height / 72.0;
		double heightCm = heightInches * 2.54;
		double heightInMeters = heightCm * scaleInMeters / 100;

		Envelope envAdj = new Envelope(centre.getX() - widthInMeters / 2.0,
				centre.getX() + widthInMeters / 2.0, centre.getY()
						- heightInMeters / 2.0, centre.getY() + heightInMeters
						/ 2.0);

		double adjX = env.getWidth() / envAdj.getWidth();
		double adjY = env.getHeight() / envAdj.getHeight();

		int adjWidth = Double.valueOf(adjX * width).intValue();
		int adjHeight = Double.valueOf(adjY * height).intValue();

		Envelope envReq = producer.getProcessor().getEnvFromPointZoomAndExtent(
				centre, mapLink.getZoom(), adjWidth, adjHeight);

		log.info("cm for " + width + "points " + widthCm + "," + heightCm);

		log.info("SCALE " + mapLink.getScale());
		log.info("WIDTH  in METERS" + env.getWidth());
		log.info("WIDTHAdj in METERS" + envAdj.getWidth());
		log.info("SCALE RESULT % " + adjX + "," + adjY);
		log.info("SCALE RESULT px " + adjX * width + "," + adjY * width);
		log.info("BBOX " + env);
		log.info("BBOXAdj " + envAdj);
		log.info("Adj WxH " + adjWidth + "x" + adjHeight);
		log.info("BBOXReq " + envReq);
		log.info("-----------");

		String[] layerNames = new String[] { "vino_20k", "mtk_vesistot",
				"peruskartta" };

		AsyncLayerProcessor asyncProc = new AsyncLayerProcessor();
		asyncProc.start();
		try {
			int n = 0;
			for (LayerDefinition ldef : mapLink.getMapLinkLayers()) {

				LayerDefinition inScale = mapLink
						.selectLayerDefinitionForScale(ldef);
				if (inScale == null) {
					continue;
				}

				log.info("Checking " + layerNames[n] + " vs. selected "
						+ inScale.getWmsname());
				assertTrue(layerNames[n].equals(inScale.getWmsname()));

				n++;

				final List<LayerDefinition> overlayLayers = new ArrayList<LayerDefinition>(
						1);

				overlayLayers.add(inScale);
				BufferedImage image = producer.getMap(asyncProc, envReq,
						mapLink.getZoom(), adjWidth, adjHeight, overlayLayers,
						MapProducer.ImageType.ARGB, envAdj);

				BufferedImage scaledImage = doScaleWithFilters(image,
						width * 2, height * 2);

				images.add(scaledImage);

				image.flush();

				try {
					File fileToSave = new File(
							MapProducerResourceTestFileType.PNG
									.getFilename(testname + "_" + n));
					ImageIO.write(scaledImage, "png", fileToSave);

				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		} finally {
			asyncProc.shutdown();

		}

		PDFProducer pdf = new PDFProducer(PDFProducer.Page.A4_Landscape);
		pdf.createLayeredPDFFromImages(images, outputFile, width, height, env,
				centre);

		for (BufferedImage image : images) {
			image.flush();

		}
	}

	@Test
	public void testMapLinkToPDFWithMultiUpscaledImageLayersInUnadjustedScaleA4()
			throws FactoryConfigurationError, Exception {
		MapProducer producer = resource.fork(null);
		String testname = "maplink-multilayers-unadjusted";
		String outputFile = MapProducerResourceTestFileType.PDF
				.getFilename(testname);

		String mapLinkArgument = "zoomLevel=9&coord=385863_6675454&mapLayers=base_35+100+&showMarker=false&forceCache=true&noSavedState=true";

		int widthTarget = Double.valueOf((20.9 - 1 * 2) / 2.54 * 72).intValue();
		int heightTarget = Double.valueOf((int) (29.7 - 1 * 2) / 2.54 * 72)
				.intValue();

		String additionalQueryParams = mapLinkArgument + "&WIDTH="
				+ widthTarget + "&height=" + heightTarget;

		String queryParams = URLDecoder.decode(mapLinkArgument
				+ additionalQueryParams, "UTF-8");

		MapLinkParser mapLinkParser = new MapLinkParser(scaleResolution,producer.getZoomOffset());

		TileLayer tileLayer = resource.getConfig().getTileLayer(layerTemplate);
		GridSubset gridSubset = tileLayer.getGridSubset(gridSubsetName);

		MapLink mapLink = mapLinkParser.parseURLMapLink(queryParams,
				resource.getLayerJson(), resource.getGf(),
				gridSubset.getResolutions());

		assertTrue(mapLink != null);
		assertTrue(mapLink.getScale() != null);

		assertTrue(mapLink.getCentre() != null);

		assertTrue(mapLink.getMapLinkLayers() != null);

		assertTrue("base_35".equals(mapLink.getMapLinkLayers().get(0)
				.getLayerid()));

		Point centre = mapLink.getCentre();

		int width = mapLink.getWidth();
		int height = mapLink.getHeight();

		ArrayList<BufferedImage> images = new ArrayList<BufferedImage>();

		Envelope env = producer.getProcessor().getEnvFromPointZoomAndExtent(
				centre, mapLink.getZoom(), width, height);

		AsyncLayerProcessor asyncProc = new AsyncLayerProcessor();
		asyncProc.start();
		try {

			int n = 0;
			for (LayerDefinition ldef : mapLink.getMapLinkLayers()) {

				LayerDefinition inScale = mapLink
						.selectLayerDefinitionForScale(ldef);
				if (inScale == null) {
					continue;
				}

				n++;

				final List<LayerDefinition> overlayLayers = new ArrayList<LayerDefinition>(
						1);

				overlayLayers.add(inScale);
				BufferedImage image = producer.getMap(asyncProc, env,
						mapLink.getZoom(), width, height, overlayLayers,
						MapProducer.ImageType.ARGB, null);

				BufferedImage scaledImage = doScaleWithFilters(image,
						width * 2, height * 2);

				images.add(scaledImage);

				image.flush();

				try {
					File fileToSave = new File(
							MapProducerResourceTestFileType.PNG
									.getFilename(testname + "_" + n));
					ImageIO.write(scaledImage, "png", fileToSave);

				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}

		} finally {
			asyncProc.shutdown();
		}

		PDFProducer pdf = new PDFProducer(PDFProducer.Page.A4);
		pdf.createLayeredPDFFromImages(images, outputFile, width, height, env,
				centre);

		for (BufferedImage image : images) {
			image.flush();

		}
	}

	@Test
	public void testMapLinkToPNGThumbnail()
			throws NoSuchAuthorityCodeException, IOException,
			GeoWebCacheException, FactoryException, ParseException,
			XMLStreamException, FactoryConfigurationError,
			RequestFilterException, TransformException, InterruptedException,
			com.vividsolutions.jts.io.ParseException, URISyntaxException {
		MapProducer producer = resource.fork(null);

		String mapLinkArgument = "zoomLevel=8&coord=422177_6764993&mapLayers=base_45+100+,35+100+,base_2+40+&"
				+ "showMarker=false&forceCache=true&noSavedState=true";

		String additionalQueryParams = mapLinkArgument
				+ "&WIDTH=512&height=256";

		String queryParams = URLDecoder.decode(mapLinkArgument
				+ additionalQueryParams, "UTF-8");

		MapLinkParser mapLinkParser = new MapLinkParser(scaleResolution,producer.getZoomOffset());

		TileLayer tileLayer = resource.getConfig().getTileLayer(layerTemplate);
		GridSubset gridSubset = tileLayer.getGridSubset(gridSubsetName);

		MapLink mapLink = mapLinkParser.parseURLMapLink(queryParams,
				resource.getLayerJson(), resource.getGf(),
				gridSubset.getResolutions());
		assertTrue(mapLink != null);
		assertTrue(mapLink.getScale() != null);
		assertTrue(mapLink.getScale().equals(Double.valueOf(11338.5888d)));

		assertTrue(mapLink.getCentre() != null);
		assertTrue(Double.valueOf(422177).equals(mapLink.getCentre().getX()));
		assertTrue(Double.valueOf(6764993).equals(mapLink.getCentre().getY()));

		assertTrue(Integer.valueOf(8).equals(mapLink.getZoom()));

		assertTrue(mapLink.getMapLinkLayers() != null);
		assertTrue(mapLink.getMapLinkLayers().size() == 3);

		assertTrue("base_45".equals(mapLink.getMapLinkLayers().get(0)
				.getLayerid()));
		assertTrue("35".equals(mapLink.getMapLinkLayers().get(1).getLayerid()));
		assertTrue("base_2".equals(mapLink.getMapLinkLayers().get(2)
				.getLayerid()));

		final List<LayerDefinition> opacityLayers = new ArrayList<LayerDefinition>();

		for (LayerDefinition ldef : mapLink.getMapLinkLayers()) {

			LayerDefinition inScale = mapLink
					.selectLayerDefinitionForScale(ldef);

			assertTrue(inScale != null);
			assertTrue(inScale.getWmsname() != null);
			assertTrue(inScale.getWmsurl() != null);

			opacityLayers.add(inScale);

		}

		Point centre = mapLink.getCentre();

		int zoom = mapLink.getZoom();

		String file = MapProducerResourceTestFileType.PNG
				.getFilename("maplink-thumbnail-hdready");
		int hdreadywidth = mapLink.getWidth();
		int hdreadyheight = mapLink.getHeight();

		String fileMedium = MapProducerResourceTestFileType.PNG
				.getFilename("maplink-thumbnail-medium");
		int mediumwidth = 256;
		int mediumheight = 256 * mapLink.getHeight() / mapLink.getWidth();

		Envelope env = producer.getProcessor().getEnvFromPointZoomAndExtent(
				centre, zoom, hdreadywidth, hdreadyheight);

		AsyncLayerProcessor asyncProc = new AsyncLayerProcessor();
		asyncProc.start();
		try {

			BufferedImage image = producer.getMap(asyncProc, env, zoom,
					hdreadywidth, hdreadyheight, opacityLayers,
					MapProducer.ImageType.ARGB);

			try {
				File fileToSave = new File(file);
				ImageIO.write(image, "png", fileToSave);

			} catch (IOException e) {
				throw new RuntimeException(e);
			}

			BufferedImage scaledImage = doScaleWithFilters(image, mediumwidth,
					mediumheight);

			File fileToSave = new File(fileMedium);
			ImageIO.write(scaledImage, "png", fileToSave);

			scaledImage.flush();

			image.flush();
		} finally {
			asyncProc.shutdown();
		}
	}

	@Test
	public void testMaplinkToPNGThumbnail2()
			throws NoSuchAuthorityCodeException, IOException,
			GeoWebCacheException, FactoryException, ParseException,
			XMLStreamException, FactoryConfigurationError,
			RequestFilterException, TransformException, InterruptedException,
			com.vividsolutions.jts.io.ParseException, URISyntaxException {
		MapProducer producer = resource.fork(null);

		String mapLinkArgument = "zoomLevel=4&coord=514920_6831842&mapLayers=base_35+100+,264+50+&"
				+ "showMarker=false&forceCache=true&noSavedState=true";
		;

		String additionalQueryParams = mapLinkArgument
				+ "&WIDTH=512&height=256";
		String queryParams = URLDecoder.decode(mapLinkArgument
				+ additionalQueryParams, "UTF-8");

		MapLinkParser mapLinkParser = new MapLinkParser(scaleResolution,producer.getZoomOffset());

		TileLayer tileLayer = resource.getConfig().getTileLayer(layerTemplate);
		GridSubset gridSubset = tileLayer.getGridSubset(gridSubsetName);

		MapLink mapLink = mapLinkParser.parseURLMapLink(queryParams,
				resource.getLayerJson(), resource.getGf(),
				gridSubset.getResolutions());
		assertTrue(mapLink != null);
		assertTrue(mapLink.getScale() != null);
		assertTrue(mapLink.getScale().equals(
				Double.valueOf(283464.72000000003d)));

		assertTrue(mapLink.getCentre() != null);
		assertTrue(Double.valueOf(514920).equals(mapLink.getCentre().getX()));
		assertTrue(Double.valueOf(6831842).equals(mapLink.getCentre().getY()));

		assertTrue(Integer.valueOf(4).equals(mapLink.getZoom()));

		assertTrue(mapLink.getMapLinkLayers() != null);
		assertTrue(mapLink.getMapLinkLayers().size() == 2);

		assertTrue("base_35".equals(mapLink.getMapLinkLayers().get(0)
				.getLayerid()));
		assertTrue("264".equals(mapLink.getMapLinkLayers().get(1).getLayerid()));

		final List<LayerDefinition> opacityLayers = new ArrayList<LayerDefinition>();

		for (LayerDefinition ldef : mapLink.getMapLinkLayers()) {

			LayerDefinition inScale = mapLink
					.selectLayerDefinitionForScale(ldef);

			assertTrue(inScale != null);
			assertTrue(inScale.getWmsname() != null);
			assertTrue(inScale.getWmsurl() != null);

			opacityLayers.add(inScale);

		}

		assertTrue("taustakartta_320k"
				.equals(opacityLayers.get(0).getWmsname()));
		assertTrue("etela_savo_maakuntakaava".equals(opacityLayers.get(1)
				.getWmsname()));

		Point centre = mapLink.getCentre();

		int zoom = mapLink.getZoom();

		String file = MapProducerResourceTestFileType.PNG
				.getFilename("maplink2-thumbnail-hdready");
		int hdreadywidth = mapLink.getWidth();
		int hdreadyheight = mapLink.getHeight();

		String fileMedium = MapProducerResourceTestFileType.PNG
				.getFilename("maplink2-thumbnail-medium");
		int mediumwidth = 256;
		int mediumheight = 256 * mapLink.getHeight() / mapLink.getWidth();

		Envelope env = producer.getProcessor().getEnvFromPointZoomAndExtent(
				centre, zoom, hdreadywidth, hdreadyheight);

		AsyncLayerProcessor asyncProc = new AsyncLayerProcessor();
		asyncProc.start();
		try {
			BufferedImage image = producer.getMap(asyncProc, env, zoom,
					hdreadywidth, hdreadyheight, opacityLayers,
					MapProducer.ImageType.ARGB);

			try {
				File fileToSave = new File(file);
				ImageIO.write(image, "png", fileToSave);

			} catch (IOException e) {
				throw new RuntimeException(e);
			}

			BufferedImage scaledImage = doScaleWithFilters(image, mediumwidth,
					mediumheight);
			try {
				File fileToSave = new File(fileMedium);
				ImageIO.write(scaledImage, "png", fileToSave);

			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			scaledImage.flush();

			image.flush();
		} finally {
			asyncProc.shutdown();
		}
	}

	@Test
	public void testPDFTemplate() throws IOException {
		new PDFProducer(PDFProducer.Page.A4);
	}

	@Test
	public void testWriteTestPDF() throws FactoryConfigurationError, Exception {

		String outputFile = MapProducerResourceTestFileType.PDF
				.getFilename("test");

		MapProducer producer = resource.fork(null);

		GeometryFactory gf = new GeometryFactory();
		Point centre = gf.createPoint(new Coordinate(422766, 6765178));

		int zoom = 6;
		int width = 864;
		int height = 864;

		Envelope env = producer.getProcessor().getEnvFromPointZoomAndExtent(
				centre, zoom, width, height);

		AsyncLayerProcessor asyncProc = new AsyncLayerProcessor();
		asyncProc.start();
		try {

			BufferedImage image = producer.getMap(asyncProc, env, zoom, width,
					height, testLayerDefs, MapProducer.ImageType.RGB);
			ArrayList<BufferedImage> images = new ArrayList<BufferedImage>();
			images.add(image);

			PDFProducer.Options opts = new PDFProducer.Options();
			opts.setPageTitle("Jebujee");
			opts.setPageDate(true);
			opts.setPageLogo(true);
			opts.setPageDate(true);

			PDFProducer pdf = new PDFProducer(PDFProducer.Page.A4, opts,
					producer.getCrs());
			pdf.createLayeredPDFFromImages(images, outputFile, width, height,
					env, centre);

			image.flush();

		} finally {
			asyncProc.shutdown();
		}
	}

	@Test
	public void testWriteTestPDFWithLayers() throws FactoryConfigurationError,
			Exception {
		MapProducer producer = resource.fork(null);

		String outputFile = MapProducerResourceTestFileType.PDF
				.getFilename("test-with-layers");

		GeometryFactory gf = new GeometryFactory();
		Point centre = gf.createPoint(new Coordinate(422766, 6765178));

		int zoom = 6;
		int width = 864;
		int height = 864;

		Envelope env = producer.getProcessor().getEnvFromPointZoomAndExtent(
				centre, zoom, width, height);

		AsyncLayerProcessor asyncProc = new AsyncLayerProcessor();
		asyncProc.start();
		try {

			BufferedImage image = producer.getMap(asyncProc, env, zoom, width,
					height, testLayerDefs, MapProducer.ImageType.RGB);

			ArrayList<BufferedImage> images = new ArrayList<BufferedImage>();
			images.add(image);
			PDFProducer pdf = new PDFProducer(PDFProducer.Page.A4);
			pdf.createLayeredPDFFromImages(images, outputFile, width, height,
					env, centre);

			image.flush();
		} finally {
			asyncProc.shutdown();
		}
	}

	@Test
	public void testWriteTestPNG_FullHD() throws ParseException, IOException,
			GeoWebCacheException, XMLStreamException,
			FactoryConfigurationError, RequestFilterException,
			TransformException, NoSuchAuthorityCodeException, FactoryException,
			InterruptedException, com.vividsolutions.jts.io.ParseException,
			URISyntaxException {
		MapProducer producer = resource.fork(null);

		String file = MapProducerResourceTestFileType.PNG
				.getFilename("test-fullhd");

		Point centre = resource.getGf().createPoint(
				new Coordinate(422766, 6765178));

		int zoom = 6;
		int width = 1920;
		int height = 1080;

		Envelope env = producer.getProcessor().getEnvFromPointZoomAndExtent(
				centre, zoom, width, height);

		AsyncLayerProcessor asyncProc = new AsyncLayerProcessor();
		asyncProc.start();
		try {

			BufferedImage image = producer.getMap(asyncProc, env, zoom, width,
					height, testLayerDefs, MapProducer.ImageType.ARGB);

			try {
				File fileToSave = new File(file);
				ImageIO.write(image, "png", fileToSave);

			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			image.flush();
		} finally {
			asyncProc.shutdown();
		}
	}

	@Test
	public void testWriteTestPNG_FullHD_ScaledWithFilters()
			throws ParseException, IOException, GeoWebCacheException,
			XMLStreamException, FactoryConfigurationError,
			RequestFilterException, TransformException,
			NoSuchAuthorityCodeException, FactoryException,
			InterruptedException, com.vividsolutions.jts.io.ParseException,
			URISyntaxException {

		MapProducer producer = resource.fork(null);

		Point centre = resource.getGf().createPoint(
				new Coordinate(422766, 6765178));

		int zoom = 6;

		String fileHd = MapProducerResourceTestFileType.PNG
				.getFilename("test-fullhd-filters-hd");
		int hdwidth = 1920;
		int hdheight = 1080;

		String fileHdReady = MapProducerResourceTestFileType.PNG
				.getFilename("test-fullhd-filters-hdready");
		int hdreadywidth = 1280;
		int hdreadyheight = hdreadywidth * 1080 / 1920;

		String fileMedium = MapProducerResourceTestFileType.PNG
				.getFilename("test-fullhd-filters-medium");
		int mediumwidth = 1920 / 4;
		int mediumheight = mediumwidth * 1080 / 1920;

		String fileSmall = MapProducerResourceTestFileType.PNG
				.getFilename("test-fullhd-filters-small");
		int smallwidth = 1920 / 8;
		int smallheight = smallwidth * 1080 / 1920;

		Envelope env = producer.getProcessor().getEnvFromPointZoomAndExtent(
				centre, zoom, hdwidth, hdheight);

		AsyncLayerProcessor asyncProc = new AsyncLayerProcessor();
		asyncProc.start();
		try {

			BufferedImage image = producer.getMap(asyncProc, env, zoom,
					hdwidth, hdheight, testLayerDefs,
					MapProducer.ImageType.ARGB);

			try {
				File fileToSave = new File(fileHd);
				ImageIO.write(image, "png", fileToSave);

			} catch (IOException e) {
				throw new RuntimeException(e);
			}

			BufferedImage scaledImage = doScaleWithFilters(image, hdreadywidth,
					hdreadyheight);
			try {
				File fileToSave = new File(fileHdReady);
				ImageIO.write(scaledImage, "png", fileToSave);

			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			scaledImage.flush();

			scaledImage = doScaleWithFilters(image, mediumwidth, mediumheight);
			try {
				File fileToSave = new File(fileMedium);
				ImageIO.write(scaledImage, "png", fileToSave);

			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			scaledImage.flush();

			scaledImage = doScaleWithFilters(image, smallwidth, smallheight);
			try {
				File fileToSave = new File(fileSmall);
				ImageIO.write(scaledImage, "png", fileToSave);

			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			scaledImage.flush();

			image.flush();
		} finally {
			asyncProc.shutdown();
		}

	}

}
