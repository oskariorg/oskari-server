package fi.nls.oskari.fe.input.format.gml;

import static org.junit.Assert.assertTrue;
import fi.nls.oskari.fe.engine.GroovyFeatureEngine;
import fi.nls.oskari.fe.input.XMLInputProcessor;
import fi.nls.oskari.fe.input.format.gml.StaxGMLInputProcessor;
import fi.nls.oskari.fe.input.format.gml.recipe.GroovyParserRecipe;
import fi.nls.oskari.fe.output.OutputProcessor;
import fi.nls.oskari.fe.output.OutputStreamProcessor;
import fi.nls.oskari.fe.output.format.jsonld.JsonLdOutputProcessor;
import fi.nls.oskari.fe.output.format.png.geotools.MapContentOutputProcessor;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyCodeSource;

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

public class TestInspireGML {

	final static Map<String, String> recipeNames = new HashMap<String, String>() {
		/**
         *
         */
		private static final long serialVersionUID = -8424681181879387940L;

		{
			put("gn",
					"/fi/nls/oskari/fe/input/format/gml/gn/ELF_generic_GN.groovy");
			put("au",
					"/fi/nls/oskari/fe/input/format/gml/au/ELF_generic_AU.groovy");
		}
	};

	static GroovyClassLoader gcl = new GroovyClassLoader();

	Map<String, Class<GroovyParserRecipe>> recipeClazzes = new HashMap<String, Class<GroovyParserRecipe>>();

	@Before
	@SuppressWarnings("unchecked")
	public void setUp() {

		for (String recipeKey : recipeNames.keySet()) {
			String recipePath = recipeNames.get(recipeKey);
			InputStreamReader reader = new InputStreamReader(
					TestInspireGML.class.getResourceAsStream(recipePath));

			GroovyCodeSource codeSource = new GroovyCodeSource(reader,
					recipePath, ".");

			Class<GroovyParserRecipe> recipeClazz = (Class<GroovyParserRecipe>) gcl
					.parseClass(codeSource, true);

			recipeClazzes.put(recipeKey, recipeClazz);
		}

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
						"/fi/nls/oskari/fe/input/format/gml/au/services_cuzk_cz_wfs_inspire-au-wfs.xml");

		try {
			inputProcessor.setInput(inp);

			FileOutputStream fouts = new FileOutputStream("AU.png");
			try {
				outputProcessor.setOutput(fouts);

				GroovyParserRecipe recipe = recipeClazzes.get("au")
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

	@Test
	public void test_CuzkCz_AU_WFS_GMLtoJSONLD() throws InstantiationException,
			IllegalAccessException, IOException, XMLStreamException {

		GroovyFeatureEngine engine = new GroovyFeatureEngine();

		XMLInputProcessor inputProcessor = new StaxGMLInputProcessor();
		OutputStreamProcessor outputProcessor = new JsonLdOutputProcessor();

		InputStream inp = getClass()
				.getResourceAsStream(
						"/fi/nls/oskari/fe/input/format/gml/au/services_cuzk_cz_wfs_inspire-au-wfs.xml");

		try {
			inputProcessor.setInput(inp);
			FileOutputStream fouts = new FileOutputStream("AU.json");
			try {
				outputProcessor.setOutput(fouts);

				GroovyParserRecipe recipe = recipeClazzes.get("au")
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
						"/fi/nls/oskari/fe/input/format/gml/gn/services_geonorge_no_wfs_inspire-gn-wfs.xml");

		try {
			inputProcessor.setInput(inp);

			FileOutputStream fouts = new FileOutputStream("GN.png");
			try {
				outputProcessor.setOutput(fouts);

				GroovyParserRecipe recipe = recipeClazzes.get("gn")
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

	@Test
	public void test_GeonorgeNo_GN_WFS_GMLtoJSONLD()
			throws InstantiationException, IllegalAccessException, IOException,
			XMLStreamException {

		GroovyFeatureEngine engine = new GroovyFeatureEngine();

		XMLInputProcessor inputProcessor = new StaxGMLInputProcessor();
		OutputStreamProcessor outputProcessor = new JsonLdOutputProcessor();

		InputStream inp = getClass()
				.getResourceAsStream(
						"/fi/nls/oskari/fe/input/format/gml/gn/services_geonorge_no_wfs_inspire-gn-wfs.xml");

		try {
			inputProcessor.setInput(inp);
			FileOutputStream fouts = new FileOutputStream("GN.json");
			try {
				outputProcessor.setOutput(fouts);

				GroovyParserRecipe recipe = recipeClazzes.get("gn")
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
