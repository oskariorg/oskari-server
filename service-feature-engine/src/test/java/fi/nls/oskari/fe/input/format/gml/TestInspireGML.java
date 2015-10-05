package fi.nls.oskari.fe.input.format.gml;

import fi.nls.oskari.fe.TestHelper;
import fi.nls.oskari.fe.engine.GroovyFeatureEngine;
import fi.nls.oskari.fe.input.XMLInputProcessor;
import fi.nls.oskari.fe.input.format.gml.recipe.GroovyParserRecipe;
import fi.nls.oskari.fe.output.OutputProcessor;
import fi.nls.oskari.fe.output.OutputStreamProcessor;
import fi.nls.oskari.fe.output.format.jsonld.JsonLdOutputProcessor;
import fi.nls.oskari.fe.output.format.png.geotools.MapContentOutputProcessor;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyCodeSource;
import org.apache.log4j.Logger;
import org.geotools.styling.Style;
import org.junit.Test;

import javax.xml.stream.XMLStreamException;
import java.io.*;

/**
 * @todo implement some asserts
 * 
 */
@Deprecated
public class TestInspireGML extends TestHelper {

    static final Logger logger = Logger.getLogger(TestInspireGML.class);

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
                        "/fi/nls/oskari/eu/inspire/administrativeunits/cuzk_cz-wfs-INSPIRE-AU-wfs.xml");

        try {
            inputProcessor.setInput(inp);

            File f = getTempFile("AU-cuzk_cz", ".png");
            logger.info(f.getAbsolutePath());
            FileOutputStream fouts = new FileOutputStream(f);

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
                        "/fi/nls/oskari/eu/inspire/geographicalnames/ign_es-INSPIRE-GN-wfs.xml");

        try {
            inputProcessor.setInput(inp);

            File f = getTempFile("GN-INSPIRE-ign_es", ".png");
            logger.info(f.getAbsolutePath());
            FileOutputStream fouts = new FileOutputStream(f);

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
                        "/fi/nls/oskari/eu/inspire/geographicalnames/ign_es-INSPIRE-GN-wfs.xml");

        try {
            inputProcessor.setInput(inp);

            File f = getTempFile("GN-ign_es", ".json");
            logger.info(f.getAbsolutePath());
            FileOutputStream fouts = new FileOutputStream(f);

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
    public void test_IgnEs_TN_WFS_GMLtoPNG() throws InstantiationException,
            IllegalAccessException, IOException, XMLStreamException {

        GroovyFeatureEngine engine = new GroovyFeatureEngine();

        XMLInputProcessor inputProcessor = new StaxGMLInputProcessor();

        Style sldStyle = MapContentOutputProcessor
                .createSLDStyle("/fi/nls/oskari/fe/output/style/INSPIRE_SLD/TN.RoadTransportNetwork.RoadLink.Default.sld");

        OutputStreamProcessor outputProcessor = new MapContentOutputProcessor(
                "EPSG:3785", sldStyle);

        InputStream inp = getClass()
                .getResourceAsStream(
                        "/fi/nls/oskari/eu/inspire/roadtransportnetwork/ign_es-inspire-TN-wfs.xml");

        try {
            inputProcessor.setInput(inp);

            File f = getTempFile("TN-ign_es", ".png");
            logger.info(f.getAbsolutePath());
            FileOutputStream fouts = new FileOutputStream(f);

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
