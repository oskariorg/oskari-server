package fi.nls.oskari.fe.input.format.gml;

import static org.junit.Assert.assertTrue;
import fi.nls.oskari.fe.engine.GroovyFeatureEngine;
import fi.nls.oskari.fe.input.XMLInputProcessor;
import fi.nls.oskari.fe.input.format.gml.StaxGMLInputProcessor;
import fi.nls.oskari.fe.input.format.gml.recipe.GroovyParserRecipe;
import fi.nls.oskari.fe.output.OutputProcessor;
import fi.nls.oskari.fe.output.OutputStreamProcessor;
import fi.nls.oskari.fe.iri.Resource;
import fi.nls.oskari.fe.output.format.jsonld.JsonLdOutputProcessor;
import fi.nls.oskari.fe.output.format.png.geotools.MapContentOutputProcessor;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyCodeSource;
import fi.nls.oskari.fe.schema.XSDDatatype;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.stream.XMLStreamException;
import org.apache.commons.lang3.tuple.Pair;
import org.geotools.styling.AbstractStyleVisitor;
import org.geotools.styling.AnchorPoint;
import org.geotools.styling.ChannelSelection;
import org.geotools.styling.ColorMap;
import org.geotools.styling.ColorMapEntry;
import org.geotools.styling.ContrastEnhancement;
import org.geotools.styling.Displacement;
import org.geotools.styling.ExternalGraphic;
import org.geotools.styling.FeatureTypeConstraint;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Fill;
import org.geotools.styling.Graphic;
import org.geotools.styling.Halo;
import org.geotools.styling.ImageOutline;
import org.geotools.styling.LinePlacement;
import org.geotools.styling.LineSymbolizer;
import org.geotools.styling.Mark;
import org.geotools.styling.NamedLayer;
import org.geotools.styling.OverlapBehavior;
import org.geotools.styling.PointPlacement;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.PolygonSymbolizer;
import org.geotools.styling.RasterSymbolizer;
import org.geotools.styling.Rule;
import org.geotools.styling.SelectedChannelType;
import org.geotools.styling.ShadedRelief;
import org.geotools.styling.Stroke;
import org.geotools.styling.Style;
import org.geotools.styling.StyleVisitor;
import org.geotools.styling.StyledLayerDescriptor;
import org.geotools.styling.Symbolizer;
import org.geotools.styling.TextSymbolizer;
import org.geotools.styling.UserLayer;
import org.junit.Before;
import org.junit.Test;
import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.IllegalAttributeException;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.Name;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.identity.FeatureId;
import org.opengis.geometry.BoundingBox;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.MultiPoint;


/**
 * @todo implement some asserts
 * 
 */
public class TestInspireGML {

	static GroovyClassLoader gcl = new GroovyClassLoader();

	@SuppressWarnings("unchecked")
	public Class<GroovyParserRecipe> setupGroovyScript(final String recipePath) {

			InputStreamReader reader = new InputStreamReader(
					TestInspireGML.class.getResourceAsStream(recipePath));

			GroovyCodeSource codeSource = new GroovyCodeSource(reader,
					recipePath, ".");

			Class<GroovyParserRecipe> recipeClazz = (Class<GroovyParserRecipe>) gcl
					.parseClass(codeSource, true);

			return recipeClazz;
	
			
	}

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

				GroovyParserRecipe recipe = 
						setupGroovyScript("/fi/nls/oskari/fe/input/format/gml/au/INSPIRE_generic_AU.groovy").
						newInstance();
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
	
	@Test
	public void test_GeonorgeNo_AU_WFS_GMLtoPNG() throws InstantiationException,
			IllegalAccessException, IOException, XMLStreamException {

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

				GroovyParserRecipe recipe = 
						setupGroovyScript("/fi/nls/oskari/fe/input/format/gml/au/ELF_generic_AU.groovy").
						newInstance();
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
	
		@Test
	public void test_LantmaterietSe_AU_WFS_GMLtoPNG() throws InstantiationException,
			IllegalAccessException, IOException, XMLStreamException {

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

			FileOutputStream fouts = new FileOutputStream("AU-lantmateriet_se.png");
			try {
				outputProcessor.setOutput(fouts);

				GroovyParserRecipe recipe = 
						setupGroovyScript("/fi/nls/oskari/fe/input/format/gml/au/ELF_generic_AU.groovy").
						newInstance();
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
		@Test
	public void test_IgnFr_AU_WFS_GMLtoPNG() throws InstantiationException,
			IllegalAccessException, IOException, XMLStreamException {

		GroovyFeatureEngine engine = new GroovyFeatureEngine();

		XMLInputProcessor inputProcessor = new StaxGMLInputProcessor();

		Style sldStyle = MapContentOutputProcessor
				.createSLDStyle("/fi/nls/oskari/fe/output/style/INSPIRE_SLD/AU.AdministrativeUnit.Default.xml");

		OutputStreamProcessor outputProcessor = new MapContentOutputProcessor(
				"EPSG:3857", sldStyle);

		InputStream inp = getClass()
				.getResourceAsStream(
						"/fi/nls/oskari/fe/input/format/gml/au/ign_fr-ELF-AU-wfs.xml");

		try {
			inputProcessor.setInput(inp);

			FileOutputStream fouts = new FileOutputStream("AU-ign_fr.png");
			try {
				outputProcessor.setOutput(fouts);

				GroovyParserRecipe recipe = 
						setupGroovyScript("/fi/nls/oskari/fe/input/format/gml/au/ELF_generic_AU.groovy").
						newInstance();
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
	*/		
	

	/* Let's not - JSON-LD a bit too slow atm */
	/* 
	@Test
	public void test_CuzkCz_AU_WFS_GMLtoJSONLD() throws InstantiationException,
			IllegalAccessException, IOException, XMLStreamException {

		GroovyFeatureEngine engine = new GroovyFeatureEngine();

		XMLInputProcessor inputProcessor = new StaxGMLInputProcessor();
		OutputStreamProcessor outputProcessor = new JsonLdOutputProcessor();

		InputStream inp = getClass()
				.getResourceAsStream(
						"/fi/nls/oskari/fe/input/format/gml/au/cuzk_cz-wfs-ELF-AU-wfs.xml.xml");

		try {
			inputProcessor.setInput(inp);
			FileOutputStream fouts = new FileOutputStream("AU.json");
			try {
				outputProcessor.setOutput(fouts);

				GroovyParserRecipe recipe = setupGroovyScript("/fi/nls/oskari/fe/input/format/gml/au/ELF_generic_AU.groovy")
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

				GroovyParserRecipe recipe =
						setupGroovyScript("/fi/nls/oskari/fe/input/format/gml/gn/ELF_generic_GN.groovy").
						newInstance();
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
	
	
	@Test
	public void test_IgnEs_GN_WFS_GMLtoPNG()
			throws InstantiationException, IllegalAccessException, IOException,
			XMLStreamException {

		GroovyFeatureEngine engine = new GroovyFeatureEngine();

		XMLInputProcessor inputProcessor = new StaxGMLInputProcessor();
		OutputStreamProcessor outputProcessor = new MapContentOutputProcessor(
				"EPSG:3857");

		InputStream inp = getClass()
				.getResourceAsStream(
						"/fi/nls/oskari/fe/input/format/gml/gn/ign_es-INSPIRE-GN-wfs.xml");

		try {
			inputProcessor.setInput(inp);

			FileOutputStream fouts = new FileOutputStream("GN-INSPIRE-ign_es.png");
			try {
				outputProcessor.setOutput(fouts);

				GroovyParserRecipe recipe =
						setupGroovyScript("/fi/nls/oskari/fe/input/format/gml/gn/INSPIRE_generic_GN.groovy").
						newInstance();
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

				GroovyParserRecipe recipe = 
						setupGroovyScript("/fi/nls/oskari/fe/input/format/gml/gn/INSPIRE_generic_GN.groovy").
						newInstance();
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
	@Test
	public void test_IgnEs_AU_WFS_GMLtoPNG()
			throws InstantiationException, IllegalAccessException, IOException,
			XMLStreamException {

		GroovyFeatureEngine engine = new GroovyFeatureEngine();

		XMLInputProcessor inputProcessor = new StaxGMLInputProcessor();
		OutputStreamProcessor outputProcessor = new MapContentOutputProcessor(
				"EPSG:3857");

		InputStream inp = getClass()
				.getResourceAsStream(
						"/fi/nls/oskari/fe/input/format/gml/au/ig_es-INSPIRE-AU-wfs.xml");

		try {
			inputProcessor.setInput(inp);

			FileOutputStream fouts = new FileOutputStream("AU-INSPIRE-ign_es.png");
			try {
				outputProcessor.setOutput(fouts);

				GroovyParserRecipe recipe =
						setupGroovyScript("/fi/nls/oskari/fe/input/format/gml/au/INSPIRE_generic_AU.groovy").
						newInstance();
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
	*/
	
	/*
	@Test
	public void test_IgnEs_AU_WFS_GMLtoJSONLD() throws InstantiationException,
			IllegalAccessException, IOException, XMLStreamException {

		GroovyFeatureEngine engine = new GroovyFeatureEngine();

		XMLInputProcessor inputProcessor = new StaxGMLInputProcessor();
		OutputStreamProcessor outputProcessor = new JsonLdOutputProcessor();

		InputStream inp = getClass()
				.getResourceAsStream(
						"/fi/nls/oskari/fe/input/format/gml/au/ig_es-INSPIRE-AU-wfs.xml");

		try {
			inputProcessor.setInput(inp);
			FileOutputStream fouts = new FileOutputStream("AU-ign_es.json");
			try {
				outputProcessor.setOutput(fouts);

				GroovyParserRecipe recipe = 
						setupGroovyScript("/fi/nls/oskari/fe/input/format/gml/au/INSPIRE_generic_AU.groovy").
						newInstance();
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
	*/
	
	@Test
	public void test_IgnFr_GN_WFS_GMLtoPNG()
			throws InstantiationException, IllegalAccessException, IOException,
			XMLStreamException {

		GroovyFeatureEngine engine = new GroovyFeatureEngine();

		XMLInputProcessor inputProcessor = new StaxGMLInputProcessor();
		OutputStreamProcessor outputProcessor = new MapContentOutputProcessor(
				"EPSG:3857");

		InputStream inp = getClass()
				.getResourceAsStream(
						"/fi/nls/oskari/fe/input/format/gml/gn/ign_fr-ELF-GN-wfs.xml");

		try {
			inputProcessor.setInput(inp);

			FileOutputStream fouts = new FileOutputStream("GN-ELF-ign_fr.png");
			try {
				outputProcessor.setOutput(fouts);

				GroovyParserRecipe recipe =
						setupGroovyScript("/fi/nls/oskari/fe/input/format/gml/gn/ELF_generic_GN.groovy").
						newInstance();
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

	/* Let's not - JSON-LD a bit too slow atm */
	/*
	@Test
	public void test_GeonorgeNo_GN_WFS_GMLtoJSONLD()
			throws InstantiationException, IllegalAccessException, IOException,
			XMLStreamException {

		GroovyFeatureEngine engine = new GroovyFeatureEngine();

		XMLInputProcessor inputProcessor = new StaxGMLInputProcessor();
		OutputStreamProcessor outputProcessor = new JsonLdOutputProcessor();

		InputStream inp = getClass()
				.getResourceAsStream(
						"/fi/nls/oskari/fe/input/format/gml/gn/geonorge_no-ELF-GN-wfs.xml");

		try {
			inputProcessor.setInput(inp);
			FileOutputStream fouts = new FileOutputStream("GN.json");
			try {
				outputProcessor.setOutput(fouts);

				GroovyParserRecipe recipe = setupGroovyScript("/fi/nls/oskari/fe/input/format/gml/gn/ELF_generic_GN.groovy").
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
	*/
	
	interface TestOutputProcessor extends OutputProcessor {

		public int getFeatureCount();
	}

	@Test
	public void test_GeonorgeNo_GN_WFS_Counts() throws InstantiationException,
			IllegalAccessException, IOException, XMLStreamException {

		GroovyFeatureEngine engine = new GroovyFeatureEngine();

		XMLInputProcessor inputProcessor = new StaxGMLInputProcessor();
		TestOutputProcessor outputProcessor = new TestOutputProcessor() {
			
			public static final String NS =
					"http://www.locationframework.eu/schemas/GeographicalNames/MasterLoD1/1.0#";

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
				assertTrue(type
						.getNs()
						.equals(NS));

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

				assertTrue(iri
						.getNs()
						.equals(NS));
				assertTrue(type
						.getNs()
						.equals(NS));

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

		InputStream inp = getClass().getResourceAsStream(
				"/fi/nls/oskari/fe/input/format/gml/gn/geonorge_no-ELF-GN-wfs.xml");

		try {
			inputProcessor.setInput(inp);

			GroovyParserRecipe recipe = 
					setupGroovyScript("/fi/nls/oskari/fe/input/format/gml/gn/ELF_generic_GN.groovy").
					newInstance();
			engine.setRecipe(recipe);

			engine.setInputProcessor(inputProcessor);
			engine.setOutputProcessor(outputProcessor);

			engine.process();

		} finally {
			inp.close();
		}

		System.out.println(outputProcessor.getFeatureCount());
		assertTrue(outputProcessor.getFeatureCount() == 44);

	}
	
	@Test
	public void test_IgnFr_GN_WFS_Counts() throws InstantiationException,
			IllegalAccessException, IOException, XMLStreamException {

		GroovyFeatureEngine engine = new GroovyFeatureEngine();

		XMLInputProcessor inputProcessor = new StaxGMLInputProcessor();
		TestOutputProcessor outputProcessor = new TestOutputProcessor() {
			
			public static final String NS =
					"http://www.locationframework.eu/schemas/GeographicalNames/MasterLoD1/1.0#";

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
				assertTrue(type
						.getNs()
						.equals(NS));

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

				assertTrue(iri
						.getNs()
						.equals(NS));
				assertTrue(type
						.getNs()
						.equals(NS));

				assertTrue(!geometryProperties.isEmpty());
				assertTrue(geometryProperties.get(0).getValue() != null);
				assertTrue(geometryProperties.get(0).getValue() instanceof MultiPoint || 
						(geometryProperties.get(0).getValue() instanceof Point));
				
				
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

			GroovyParserRecipe recipe = 
					setupGroovyScript("/fi/nls/oskari/fe/input/format/gml/gn/ELF_generic_GN.groovy").
					newInstance();
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
	
	
	@Test
	public void test_FgiFi_HY_WatercourseLink_WFS_GMLtoPNG()
			throws InstantiationException, IllegalAccessException, IOException,
			XMLStreamException {

		GroovyFeatureEngine engine = new GroovyFeatureEngine();


		Style sldStyle = MapContentOutputProcessor
				.createSLDStyle("/fi/nls/oskari/fe/output/style/inspire/hy/fgi_fi_WatercourseLink.xml");

		
		XMLInputProcessor inputProcessor = new StaxGMLInputProcessor();
		OutputStreamProcessor outputProcessor = new MapContentOutputProcessor(
				"EPSG:3857", sldStyle );

		InputStream inp = getClass()
				.getResourceAsStream(
						"/fi/nls/oskari/fe/input/format/gml/hy/fgi_fi_wfs_ELF-HY-WatercourseLink-wfs.xml");

		try {
			inputProcessor.setInput(inp);

			FileOutputStream fouts = new FileOutputStream("HY-fgi_fi-WatercourseLink.png");
			try {
				outputProcessor.setOutput(fouts);

				GroovyParserRecipe recipe = 
						setupGroovyScript("/fi/nls/oskari/fe/input/format/gml/hy/ELF_generic_HY.groovy").
						newInstance();
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
