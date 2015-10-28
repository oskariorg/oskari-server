package fi.nls.oskari.printout.breeding;

import fi.nls.oskari.printout.input.layers.LayerDefinition;
import fi.nls.oskari.printout.output.map.MapProducerResource;
import fi.nls.oskari.printout.output.map.MetricScaleResolutionUtils;
import fi.nls.oskari.printout.ws.jaxrs.resource.MapResource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.PropertyConfigurator;
import org.geowebcache.grid.GridSubset;
import org.geowebcache.layer.TileLayer;
import org.junit.Before;
import org.junit.Test;

import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.assertTrue;

public class DefaultGridSetTest {

	private static Log log = LogFactory.getLog(DefaultGridSetTest.class);

	final List<LayerDefinition> testLayerDefs = new ArrayList<LayerDefinition>();

	MapProducerResource resource;

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

		resource = new MapProducerResource(props) {

		};

	}

	@Test
	public void testDefaultGridSetTEMPLATE() {
		String layerTemplate = "EPSG_3067_MML_LAYER_TEMPLATE";
		String gridSubsetName = "EPSG_3067_MML";

		TileLayer tileLayer = resource.getConfig().getTileLayer(layerTemplate);

		GridSubset gridSubset = tileLayer.getGridSubset(gridSubsetName);

		log.info(tileLayer.getName());
		log.info("-" + gridSubset);

		double[] resArray = gridSubset.getResolutions();

		for (double d : resArray) {
			log.info("- resolution " + d);

			/* Map Scale = Raster resolution (in meters) * 2 * 1000 */
			log.info("-=> scale " + (d * 2 * 1000));
			log.info("-=> openlayers scale " + (d * 39.3701 * 72));
			log.info("-=> openlayers scaleUtils"
					+ MetricScaleResolutionUtils.getScaleFromResolution(
							"m_ol212", d));
		}

		assertTrue(resArray.length == 13);
	}

	@Test
	public void testJHSGridSetTEMPLATE() {
		String layerTemplate = "EPSG_3067_JHS180_LAYER_TEMPLATE";
		String gridSubsetName = "EPSG_3067_JHS180";

		TileLayer tileLayer = resource.getConfig().getTileLayer(layerTemplate);

		GridSubset gridSubset = tileLayer.getGridSubset(gridSubsetName);

		log.info(tileLayer.getName());
		log.info("-" + gridSubset);

		double[] resArray = gridSubset.getResolutions();

		for (double d : resArray) {
			log.info("- resolution " + d);

			/* Map Scale = Raster resolution (in meters) * 2 * 1000 */
			log.info("-=> scale " + (d * 2 * 1000));
			log.info("-=> openlayers scale " + (d * 39.3701 * 72));
			log.info("-=> openlayers scaleUtils"
					+ MetricScaleResolutionUtils.getScaleFromResolution(
							"m_ol212", d));
		}

		assertTrue(resArray.length == 16);
	}

}
