package fi.nls.oskari.fe.input.format.gml;

import static org.junit.Assert.assertTrue;
import fi.nls.oskari.fe.engine.GroovyFeatureEngine;
import fi.nls.oskari.fe.input.XMLInputProcessor;
import fi.nls.oskari.fe.input.format.gml.recipe.GroovyParserRecipe;
import fi.nls.oskari.fe.iri.Resource;
import fi.nls.oskari.fe.output.OutputProcessor;
import fi.nls.oskari.fe.output.OutputStreamProcessor;
import fi.nls.oskari.fe.output.format.png.geotools.MapContentOutputProcessor;
import fi.nls.oskari.fe.schema.XSDDatatype;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyCodeSource;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.lang3.tuple.Pair;
import org.geotools.styling.Style;
import org.junit.Test;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.Point;

public class TestELFGML {

	static GroovyClassLoader gcl = new GroovyClassLoader();

	@SuppressWarnings("unchecked")
	public Class<GroovyParserRecipe> setupGroovyScript(final String recipePath) {

		InputStreamReader reader = new InputStreamReader(
				TestInspireGML.class.getResourceAsStream(recipePath));

		GroovyCodeSource codeSource = new GroovyCodeSource(reader, recipePath,
				".");

		Class<GroovyParserRecipe> recipeClazz = (Class<GroovyParserRecipe>) gcl
				.parseClass(codeSource, true);

		return recipeClazz;

	}

	/**
	 * 
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IOException
	 * @throws XMLStreamException
	 */
	@Test
	public void test_GeonorgeNo_AU_WFS_GMLtoPNG()
			throws InstantiationException, IllegalAccessException, IOException,
			XMLStreamException {

		GroovyFeatureEngine engine = new GroovyFeatureEngine();

		XMLInputProcessor inputProcessor = new StaxGMLInputProcessor();

		Style sldStyle = MapContentOutputProcessor
				.createSLDStyle("/fi/nls/oskari/fe/output/style/INSPIRE_SLD/AU.AdministrativeUnit.Default.xml");

		OutputStreamProcessor outputProcessor = new MapContentOutputProcessor(
				"EPSG:3035", sldStyle);

		InputStream inp = getClass()
				.getResourceAsStream(
						"/fi/nls/oskari/fe/input/format/gml/au/geonorge_no-ELF-AU-wfs.xml");

		try {
			inputProcessor.setInput(inp);

			FileOutputStream fouts = new FileOutputStream("AU-geonorge_no.png");
			try {
				outputProcessor.setOutput(fouts);

				GroovyParserRecipe recipe = setupGroovyScript(
						"/fi/nls/oskari/fe/input/format/gml/au/ELF_generic_AU.groovy")
						.newInstance();
				engine.setRecipe(recipe);

				engine.setInputProcessor(inputProcessor);
				engine.setOutputProcessor(outputProcessor);

				engine.process();

			} finally {
				fouts.close();
			}

		} finally {
			inp.close();
		}

	}

	/**
	 * 
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IOException
	 * @throws XMLStreamException
	 */
	@Test
	public void test_LantmaterietSe_AU_WFS_GMLtoPNG()
			throws InstantiationException, IllegalAccessException, IOException,
			XMLStreamException {

		GroovyFeatureEngine engine = new GroovyFeatureEngine();

		XMLInputProcessor inputProcessor = new StaxGMLInputProcessor();

		Style sldStyle = MapContentOutputProcessor
				.createSLDStyle("/fi/nls/oskari/fe/output/style/INSPIRE_SLD/AU.AdministrativeUnit.Default.xml");

		OutputStreamProcessor outputProcessor = new MapContentOutputProcessor(
				"EPSG:3035", sldStyle);

		InputStream inp = getClass()
				.getResourceAsStream(
						"/fi/nls/oskari/fe/input/format/gml/au/lantmateriet_se-ELF-AU-wfs.xml");

		try {
			inputProcessor.setInput(inp);

			FileOutputStream fouts = new FileOutputStream(
					"AU-lantmateriet_se.png");
			try {
				outputProcessor.setOutput(fouts);

				GroovyParserRecipe recipe = setupGroovyScript(
						"/fi/nls/oskari/fe/input/format/gml/au/ELF_generic_AU.groovy")
						.newInstance();
				engine.setRecipe(recipe);

				engine.setInputProcessor(inputProcessor);
				engine.setOutputProcessor(outputProcessor);

				engine.process();

			} finally {
				fouts.close();
			}

		} finally {
			inp.close();
		}

	}

	/**
	 * 
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IOException
	 * @throws XMLStreamException
	 */
	@Test
	public void test_GeonorgeNo_GN_WFS_GMLtoPNG()
			throws InstantiationException, IllegalAccessException, IOException,
			XMLStreamException {

		GroovyFeatureEngine engine = new GroovyFeatureEngine();

		XMLInputProcessor inputProcessor = new StaxGMLInputProcessor();
		OutputStreamProcessor outputProcessor = new MapContentOutputProcessor(
				"EPSG:3035");

		InputStream inp = getClass()
				.getResourceAsStream(
						"/fi/nls/oskari/fe/input/format/gml/gn/geonorge_no-ELF-GN-wfs.xml");

		try {
			inputProcessor.setInput(inp);

			FileOutputStream fouts = new FileOutputStream("GN-geonorge_no.png");
			try {
				outputProcessor.setOutput(fouts);

				GroovyParserRecipe recipe = setupGroovyScript(
						"/fi/nls/oskari/fe/input/format/gml/gn/ELF_generic_GN.groovy")
						.newInstance();
				engine.setRecipe(recipe);

				engine.setInputProcessor(inputProcessor);
				engine.setOutputProcessor(outputProcessor);

				engine.process();

			} finally {
				fouts.close();
			}

		} finally {
			inp.close();
		}

	}

	/**
	 * 
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IOException
	 * @throws XMLStreamException
	 */
	@Test
	public void test_IgnFr_GN_WFS_GMLtoPNG() throws InstantiationException,
			IllegalAccessException, IOException, XMLStreamException {

		GroovyFeatureEngine engine = new GroovyFeatureEngine();

		XMLInputProcessor inputProcessor = new StaxGMLInputProcessor();
		OutputStreamProcessor outputProcessor = new MapContentOutputProcessor(
				"EPSG:3785");

		InputStream inp = getClass().getResourceAsStream(
				"/fi/nls/oskari/fe/input/format/gml/gn/ign_fr-ELF-GN-wfs.xml");

		try {
			inputProcessor.setInput(inp);

			FileOutputStream fouts = new FileOutputStream("GN-ELF-ign_fr.png");
			try {
				outputProcessor.setOutput(fouts);

				GroovyParserRecipe recipe = setupGroovyScript(
						"/fi/nls/oskari/fe/input/format/gml/gn/ELF_generic_GN.groovy")
						.newInstance();
				engine.setRecipe(recipe);

				engine.setInputProcessor(inputProcessor);
				engine.setOutputProcessor(outputProcessor);

				engine.process();

			} finally {
				fouts.close();
			}

		} finally {
			inp.close();
		}

	}

	/**
	 * 
	 *
	 */
	interface TestOutputProcessor extends OutputProcessor {

		public int getFeatureCount();
	}

	/**
	 * 
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IOException
	 * @throws XMLStreamException
	 */
	@Test
	public void test_GeonorgeNo_GN_WFS_Counts() throws InstantiationException,
			IllegalAccessException, IOException, XMLStreamException {

		GroovyFeatureEngine engine = new GroovyFeatureEngine();

		XMLInputProcessor inputProcessor = new StaxGMLInputProcessor();
		TestOutputProcessor outputProcessor = new TestOutputProcessor() {

			public static final String NS = "http://www.locationframework.eu/schemas/GeographicalNames/MasterLoD1/1.0#";

			int featureCount = 0;

			@Override
			public void begin() throws IOException {

			}

			@Override
			public void edge(Resource subject, Resource predicate,
					Resource value) throws IOException {

			}

			@Override
			public void end() throws IOException {

			}

			@Override
			public void flush() throws IOException {

			}

			@Override
			public void prefix(String prefix, String ns) throws IOException {

			}

			@Override
			public void type(Resource type,
					List<Pair<Resource, XSDDatatype>> simpleProperties,
					List<Pair<Resource, Object>> linkProperties,
					List<Pair<Resource, String>> geometryProperties)
					throws IOException {
				assertTrue(type.getNs().equals(NS));

			}

			@Override
			public void vertex(Resource iri, Resource type,
					List<Pair<Resource, ?>> simpleProperties,
					List<Pair<Resource, ?>> linkProperties) throws IOException {

			}

			@Override
			public void vertex(Resource iri, Resource type,
					List<Pair<Resource, ?>> simpleProperties,
					List<Pair<Resource, ?>> linkProperties,
					List<Pair<Resource, Geometry>> geometryProperties)
					throws IOException {

				assertTrue(iri.getNs().equals(NS));
				assertTrue(type.getNs().equals(NS));

				assertTrue(!geometryProperties.isEmpty());
				assertTrue(geometryProperties.get(0).getValue() != null);
				assertTrue(geometryProperties.get(0).getValue() instanceof Point);

				featureCount++;

			}

			@Override
			public int getFeatureCount() {
				return featureCount;
			}

		};

		InputStream inp = getClass()
				.getResourceAsStream(
						"/fi/nls/oskari/fe/input/format/gml/gn/geonorge_no-ELF-GN-wfs.xml");

		try {
			inputProcessor.setInput(inp);

			GroovyParserRecipe recipe = setupGroovyScript(
					"/fi/nls/oskari/fe/input/format/gml/gn/ELF_generic_GN.groovy")
					.newInstance();
			engine.setRecipe(recipe);

			engine.setInputProcessor(inputProcessor);
			engine.setOutputProcessor(outputProcessor);

			engine.process();

		} finally {
			inp.close();
		}

		System.out.println(outputProcessor.getFeatureCount());
		assertTrue(outputProcessor.getFeatureCount() == 65);

	}

	/**
	 * 
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IOException
	 * @throws XMLStreamException
	 */
	@Test
	public void test_FgiFi_HY_WatercourseLink_WFS_GMLtoPNG()
			throws InstantiationException, IllegalAccessException, IOException,
			XMLStreamException {

		GroovyFeatureEngine engine = new GroovyFeatureEngine();

		Style sldStyle = MapContentOutputProcessor
				.createSLDStyle("/fi/nls/oskari/fe/output/style/inspire/hy/fgi_fi_WatercourseLink.xml");

		XMLInputProcessor inputProcessor = new StaxGMLInputProcessor();
		OutputStreamProcessor outputProcessor = new MapContentOutputProcessor(
				"EPSG:3857", sldStyle);

		InputStream inp = getClass()
				.getResourceAsStream(
						"/fi/nls/oskari/fe/input/format/gml/hy/fgi_fi_wfs_ELF-HY-WatercourseLink-wfs.xml");

		try {
			inputProcessor.setInput(inp);

			FileOutputStream fouts = new FileOutputStream(
					"HY-fgi_fi-WatercourseLink.png");
			try {
				outputProcessor.setOutput(fouts);

				GroovyParserRecipe recipe = setupGroovyScript(
						"/fi/nls/oskari/fe/input/format/gml/hy/ELF_generic_HY.groovy")
						.newInstance();
				engine.setRecipe(recipe);

				engine.setInputProcessor(inputProcessor);
				engine.setOutputProcessor(outputProcessor);

				engine.process();

			} finally {
				fouts.close();
			}

		} finally {
			inp.close();
		}

	}

	/**
	 * 
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IOException
	 * @throws XMLStreamException
	 */
	@Test
	public void test_IgnFr_GN_WFS_Counts() throws InstantiationException,
			IllegalAccessException, IOException, XMLStreamException {

		GroovyFeatureEngine engine = new GroovyFeatureEngine();

		XMLInputProcessor inputProcessor = new StaxGMLInputProcessor();
		TestOutputProcessor outputProcessor = new TestOutputProcessor() {

			public static final String NS = "http://www.locationframework.eu/schemas/GeographicalNames/MasterLoD1/1.0#";

			int featureCount = 0;

			@Override
			public void begin() throws IOException {

			}

			@Override
			public void edge(Resource subject, Resource predicate,
					Resource value) throws IOException {

			}

			@Override
			public void end() throws IOException {

			}

			@Override
			public void flush() throws IOException {

			}

			@Override
			public void prefix(String prefix, String ns) throws IOException {

			}

			@Override
			public void type(Resource type,
					List<Pair<Resource, XSDDatatype>> simpleProperties,
					List<Pair<Resource, Object>> linkProperties,
					List<Pair<Resource, String>> geometryProperties)
					throws IOException {
				assertTrue(type.getNs().equals(NS));

			}

			@Override
			public void vertex(Resource iri, Resource type,
					List<Pair<Resource, ?>> simpleProperties,
					List<Pair<Resource, ?>> linkProperties) throws IOException {

			}

			@Override
			public void vertex(Resource iri, Resource type,
					List<Pair<Resource, ?>> simpleProperties,
					List<Pair<Resource, ?>> linkProperties,
					List<Pair<Resource, Geometry>> geometryProperties)
					throws IOException {

				assertTrue(iri.getNs().equals(NS));
				assertTrue(type.getNs().equals(NS));

				assertTrue(!geometryProperties.isEmpty());
				assertTrue(geometryProperties.get(0).getValue() != null);
				assertTrue(geometryProperties.get(0).getValue() instanceof MultiPoint
						|| (geometryProperties.get(0).getValue() instanceof Point));

				featureCount++;

			}

			@Override
			public int getFeatureCount() {
				return featureCount;
			}

		};

		InputStream inp = getClass().getResourceAsStream(
				"/fi/nls/oskari/fe/input/format/gml/gn/ign_fr-ELF-GN-wfs.xml");

		try {
			inputProcessor.setInput(inp);

			GroovyParserRecipe recipe = setupGroovyScript(
					"/fi/nls/oskari/fe/input/format/gml/gn/ELF_generic_GN.groovy")
					.newInstance();
			engine.setRecipe(recipe);

			engine.setInputProcessor(inputProcessor);
			engine.setOutputProcessor(outputProcessor);

			engine.process();

		} finally {
			inp.close();
		}

		System.out.println(outputProcessor.getFeatureCount());
		assertTrue(outputProcessor.getFeatureCount() == 5);

	}
	
	/**
	 * 
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IOException
	 * @throws XMLStreamException
	 */
	@Test
	public void test_NlsFi_TN_WFS_GMLtoPNG()
			throws InstantiationException, IllegalAccessException, IOException,
			XMLStreamException {

		GroovyFeatureEngine engine = new GroovyFeatureEngine();

		XMLInputProcessor inputProcessor = new StaxGMLInputProcessor();
		
		Style sldStyle = MapContentOutputProcessor
				.createSLDStyle("/fi/nls/oskari/fe/output/style/INSPIRE_SLD/TN.RoadTransportNetwork.RoadLink.Default.sld");

		OutputStreamProcessor outputProcessor = new MapContentOutputProcessor(
				"EPSG:3035", sldStyle);

		InputStream inp = getClass()
				.getResourceAsStream(
						"/fi/nls/oskari/fe/input/format/gml/tn/nls_fi-ELF-TN-wfs.xml");

		try {
			inputProcessor.setInput(inp);

			FileOutputStream fouts = new FileOutputStream("TN-nls_fi.png");
			try {
				outputProcessor.setOutput(fouts);

				GroovyParserRecipe recipe = setupGroovyScript(
						"/fi/nls/oskari/fe/input/format/gml/tn/ELF_generic_TN.groovy")
						.newInstance();
				engine.setRecipe(recipe);

				engine.setInputProcessor(inputProcessor);
				engine.setOutputProcessor(outputProcessor);

				engine.process();

			} finally {
				fouts.close();
			}

		} finally {
			inp.close();
		}

	}

}
