package fi.nls.oskari.fe.input.format.gml;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.xml.stream.XMLStreamException;

import org.geotools.styling.Style;
import org.junit.Test;

import fi.nls.oskari.fe.engine.GroovyFeatureEngine;
import fi.nls.oskari.fe.input.XMLInputProcessor;
import fi.nls.oskari.fe.input.format.gml.recipe.GroovyParserRecipe;
import fi.nls.oskari.fe.output.OutputProcessor;
import fi.nls.oskari.fe.output.OutputStreamProcessor;
import fi.nls.oskari.fe.output.format.jsonld.JsonLdOutputProcessor;
import fi.nls.oskari.fe.output.format.png.geotools.MapContentOutputProcessor;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyCodeSource;

/**
 * @todo implement some asserts
 * 
 */
public class TestInspireGML {

	interface TestOutputProcessor extends OutputProcessor {

		public int getFeatureCount();
	}

	static GroovyClassLoader gcl = new GroovyClassLoader();

	/**
	 * 
	 * @param recipePath
	 * @return
	 */
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
	public void test_CuzkCz_AU_WFS_GMLtoPNG() throws InstantiationException,
			IllegalAccessException, IOException, XMLStreamException {

		GroovyFeatureEngine engine = new GroovyFeatureEngine();

		XMLInputProcessor inputProcessor = new StaxGMLInputProcessor();

		Style sldStyle = MapContentOutputProcessor
				.createSLDStyle("/fi/nls/oskari/fe/output/style/INSPIRE_SLD/AU.AdministrativeUnit.Default.xml");

		OutputStreamProcessor outputProcessor = new MapContentOutputProcessor(
				"EPSG:3035", sldStyle);

		InputStream inp = getClass()
				.getResourceAsStream(
						"/fi/nls/oskari/fe/input/format/gml/au/cuzk_cz-wfs-ELF-AU-wfs.xml");

		try {
			inputProcessor.setInput(inp);

			FileOutputStream fouts = new FileOutputStream("AU-cuzk_cz.png");
			try {
				outputProcessor.setOutput(fouts);

				GroovyParserRecipe recipe = setupGroovyScript(
						"/fi/nls/oskari/fe/input/format/gml/au/INSPIRE_generic_AU.groovy")
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

	/*
	 * @Test public void test_IgnFr_AU_WFS_GMLtoPNG() throws
	 * InstantiationException, IllegalAccessException, IOException,
	 * XMLStreamException {
	 * 
	 * GroovyFeatureEngine engine = new GroovyFeatureEngine();
	 * 
	 * 
	 * Style sldStyle = MapContentOutputProcessor .createSLDStyle(
	 * "/fi/nls/oskari/fe/output/style/INSPIRE_SLD/AU.AdministrativeUnit.Default.xml"
	 * );
	 * 
	 * OutputStreamProcessor outputProcessor = new MapContentOutputProcessor(
	 * "EPSG:3857", sldStyle);
	 * 
	 * InputStream inp = getClass() .getResourceAsStream(
	 * "/fi/nls/oskari/fe/input/format/gml/au/ign_fr-ELF-AU-wfs.xml");
	 * 
	 * try { inputProcessor.setInput(inp);
	 * 
	 * FileOutputStream fouts = new FileOutputStream("AU-ign_fr.png"); try {
	 * outputProcessor.setOutput(fouts);
	 * 
	 * GroovyParserRecipe recipe = setupGroovyScript(
	 * "/fi/nls/oskari/fe/input/format/gml/au/ELF_generic_AU.groovy").
	 * newInstance(); engine.setRecipe(recipe);
	 * 
	 * engine.setInputProcessor(inputProcessor);
	 * engine.setOutputProcessor(outputProcessor);
	 * 
	 * engine.process();
	 * 
	 * } finally { fouts.close(); }
	 * 
	 * } finally { inp.close(); }
	 * 
	 * }
	 */

	/* Let's not - JSON-LD a bit too slow atm */
	/*
	 * @Test public void test_CuzkCz_AU_WFS_GMLtoJSONLD() throws
	 * InstantiationException, IllegalAccessException, IOException,
	 * XMLStreamException {
	 * 
	 * GroovyFeatureEngine engine = new GroovyFeatureEngine();
	 * 
	 * XMLInputProcessor inputProcessor = new StaxGMLInputProcessor();
	 * OutputStreamProcessor outputProcessor = new JsonLdOutputProcessor();
	 * 
	 * InputStream inp = getClass() .getResourceAsStream(
	 * "/fi/nls/oskari/fe/input/format/gml/au/cuzk_cz-wfs-ELF-AU-wfs.xml.xml");
	 * 
	 * try { inputProcessor.setInput(inp); FileOutputStream fouts = new
	 * FileOutputStream("AU.json"); try { outputProcessor.setOutput(fouts);
	 * 
	 * GroovyParserRecipe recipe = setupGroovyScript(
	 * "/fi/nls/oskari/fe/input/format/gml/au/ELF_generic_AU.groovy")
	 * .newInstance(); engine.setRecipe(recipe);
	 * 
	 * engine.setInputProcessor(inputProcessor);
	 * engine.setOutputProcessor(outputProcessor);
	 * 
	 * engine.process(); } finally { fouts.close(); }
	 * 
	 * } finally { inp.close(); }
	 * 
	 * }
	 */

	/**
	 * 
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IOException
	 * @throws XMLStreamException
	 */
	@Test
	public void test_IgnEs_GN_WFS_GMLtoPNG() throws InstantiationException,
			IllegalAccessException, IOException, XMLStreamException {

		GroovyFeatureEngine engine = new GroovyFeatureEngine();

		XMLInputProcessor inputProcessor = new StaxGMLInputProcessor();
		OutputStreamProcessor outputProcessor = new MapContentOutputProcessor(
				"EPSG:3857");

		InputStream inp = getClass()
				.getResourceAsStream(
						"/fi/nls/oskari/fe/input/format/gml/gn/ign_es-INSPIRE-GN-wfs.xml");

		try {
			inputProcessor.setInput(inp);

			FileOutputStream fouts = new FileOutputStream(
					"GN-INSPIRE-ign_es.png");
			try {
				outputProcessor.setOutput(fouts);

				GroovyParserRecipe recipe = setupGroovyScript(
						"/fi/nls/oskari/fe/input/format/gml/gn/INSPIRE_generic_GN.groovy")
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
	public void test_IgnEs_GN_WFS_GMLtoJSONLD() throws InstantiationException,
			IllegalAccessException, IOException, XMLStreamException {

		GroovyFeatureEngine engine = new GroovyFeatureEngine();

		XMLInputProcessor inputProcessor = new StaxGMLInputProcessor();
		OutputStreamProcessor outputProcessor = new JsonLdOutputProcessor();

		InputStream inp = getClass()
				.getResourceAsStream(
						"/fi/nls/oskari/fe/input/format/gml/gn/ign_es-INSPIRE-GN-wfs.xml");

		try {
			inputProcessor.setInput(inp);
			FileOutputStream fouts = new FileOutputStream("GN-ign_es.json");
			try {
				outputProcessor.setOutput(fouts);

				GroovyParserRecipe recipe = setupGroovyScript(
						"/fi/nls/oskari/fe/input/format/gml/gn/INSPIRE_generic_GN.groovy")
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

	/*
	 * @Test public void test_IgnEs_AU_WFS_GMLtoPNG() throws
	 * InstantiationException, IllegalAccessException, IOException,
	 * XMLStreamException {
	 * 
	 * GroovyFeatureEngine engine = new GroovyFeatureEngine();
	 * 
	 * XMLInputProcessor inputProcessor = new StaxGMLInputProcessor();
	 * OutputStreamProcessor outputProcessor = new MapContentOutputProcessor(
	 * "EPSG:3857");
	 * 
	 * InputStream inp = getClass() .getResourceAsStream(
	 * "/fi/nls/oskari/fe/input/format/gml/au/ig_es-INSPIRE-AU-wfs.xml");
	 * 
	 * try { inputProcessor.setInput(inp);
	 * 
	 * FileOutputStream fouts = new FileOutputStream("AU-INSPIRE-ign_es.png");
	 * try { outputProcessor.setOutput(fouts);
	 * 
	 * GroovyParserRecipe recipe = setupGroovyScript(
	 * "/fi/nls/oskari/fe/input/format/gml/au/INSPIRE_generic_AU.groovy").
	 * newInstance(); engine.setRecipe(recipe);
	 * 
	 * engine.setInputProcessor(inputProcessor);
	 * engine.setOutputProcessor(outputProcessor);
	 * 
	 * engine.process();
	 * 
	 * } finally { fouts.close(); }
	 * 
	 * } finally { inp.close(); }
	 * 
	 * }
	 */

	/*
	 * @Test public void test_IgnEs_AU_WFS_GMLtoJSONLD() throws
	 * InstantiationException, IllegalAccessException, IOException,
	 * XMLStreamException {
	 * 
	 * GroovyFeatureEngine engine = new GroovyFeatureEngine();
	 * 
	 * XMLInputProcessor inputProcessor = new StaxGMLInputProcessor();
	 * OutputStreamProcessor outputProcessor = new JsonLdOutputProcessor();
	 * 
	 * InputStream inp = getClass() .getResourceAsStream(
	 * "/fi/nls/oskari/fe/input/format/gml/au/ig_es-INSPIRE-AU-wfs.xml");
	 * 
	 * try { inputProcessor.setInput(inp); FileOutputStream fouts = new
	 * FileOutputStream("AU-ign_es.json"); try {
	 * outputProcessor.setOutput(fouts);
	 * 
	 * GroovyParserRecipe recipe = setupGroovyScript(
	 * "/fi/nls/oskari/fe/input/format/gml/au/INSPIRE_generic_AU.groovy").
	 * newInstance(); engine.setRecipe(recipe);
	 * 
	 * engine.setInputProcessor(inputProcessor);
	 * engine.setOutputProcessor(outputProcessor);
	 * 
	 * engine.process(); } finally { fouts.close(); }
	 * 
	 * } finally { inp.close(); }
	 * 
	 * }
	 */

	/* Let's not - JSON-LD a bit too slow atm */
	/*
	 * @Test public void test_GeonorgeNo_GN_WFS_GMLtoJSONLD() throws
	 * InstantiationException, IllegalAccessException, IOException,
	 * XMLStreamException {
	 * 
	 * GroovyFeatureEngine engine = new GroovyFeatureEngine();
	 * 
	 * XMLInputProcessor inputProcessor = new StaxGMLInputProcessor();
	 * OutputStreamProcessor outputProcessor = new JsonLdOutputProcessor();
	 * 
	 * InputStream inp = getClass() .getResourceAsStream(
	 * "/fi/nls/oskari/fe/input/format/gml/gn/geonorge_no-ELF-GN-wfs.xml");
	 * 
	 * try { inputProcessor.setInput(inp); FileOutputStream fouts = new
	 * FileOutputStream("GN.json"); try { outputProcessor.setOutput(fouts);
	 * 
	 * GroovyParserRecipe recipe = setupGroovyScript(
	 * "/fi/nls/oskari/fe/input/format/gml/gn/ELF_generic_GN.groovy").
	 * .newInstance(); engine.setRecipe(recipe);
	 * 
	 * engine.setInputProcessor(inputProcessor);
	 * engine.setOutputProcessor(outputProcessor);
	 * 
	 * engine.process(); } finally { fouts.close(); }
	 * 
	 * } finally { inp.close(); }
	 * 
	 * }
	 */
	
	/**
	 * 
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IOException
	 * @throws XMLStreamException
	 */
	@Test
	public void test_IgnEs_TN_WFS_GMLtoPNG()
			throws InstantiationException, IllegalAccessException, IOException,
			XMLStreamException {

		GroovyFeatureEngine engine = new GroovyFeatureEngine();

		XMLInputProcessor inputProcessor = new StaxGMLInputProcessor();
		
		Style sldStyle = MapContentOutputProcessor
				.createSLDStyle("/fi/nls/oskari/fe/output/style/INSPIRE_SLD/TN.RoadTransportNetwork.RoadLink.Default.sld");

		OutputStreamProcessor outputProcessor = new MapContentOutputProcessor(
				"EPSG:3785", sldStyle);

		InputStream inp = getClass()
				.getResourceAsStream(
						"/fi/nls/oskari/fe/input/format/gml/tn/ign_es-inspire-TN-wfs.xml");

		try {
			inputProcessor.setInput(inp);

			FileOutputStream fouts = new FileOutputStream("TN-ign_es.png");
			try {
				outputProcessor.setOutput(fouts);

				GroovyParserRecipe recipe = setupGroovyScript(
						"/fi/nls/oskari/fe/input/format/gml/tn/INSPIRE_generic_TN.groovy")
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
