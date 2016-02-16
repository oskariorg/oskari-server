package fi.nls.oskari.fe.input.format.gml;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.Point;
import fi.nls.oskari.fe.TestHelper;
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
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.geotools.styling.Style;
import org.json.JSONObject;
import org.junit.Ignore;
import org.junit.Test;

import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;

@Deprecated
public class TestELFGML extends TestHelper {

    static final Logger logger = Logger.getLogger(TestELFGML.class);
    static GroovyClassLoader gcl = new GroovyClassLoader();

    @SuppressWarnings("unchecked")
    public Class<GroovyParserRecipe> setupGroovyScript(final String recipePath) {

        InputStreamReader reader = new InputStreamReader(
                TestELFGML.class.getResourceAsStream(recipePath));

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
                        "/fi/nls/oskari/eu/elf/administrativeunits/geonorge_no-ELF-AU-wfs.xml");

        try {
            inputProcessor.setInput(inp);

            File f = getTempFile("AU-geonorge_no", ".png");
            logger.info(f.getAbsolutePath());
            FileOutputStream fouts = new FileOutputStream(f);
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
                        "/fi/nls/oskari/eu/elf/administrativeunits/lantmateriet_se-ELF-AU-wfs.xml");

        try {
            inputProcessor.setInput(inp);

            File f = getTempFile("AU-lantmateriet_se", ".png");
            logger.info(f.getAbsolutePath());
            FileOutputStream fouts = new FileOutputStream(f);

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
                        "/fi/nls/oskari/eu/elf/geographicalnames/geonorge_no-ELF-GN-wfs.xml");

        try {
            inputProcessor.setInput(inp);

            File f = getTempFile("GN-geonorge_no", ".png");
            logger.info(f.getAbsolutePath());
            FileOutputStream fouts = new FileOutputStream(f);
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

        InputStream inp = getClass()
                .getResourceAsStream(
                        "/fi/nls/oskari/eu/elf/geographicalnames/ign_fr-ELF-GN-wfs.xml");

        try {
            inputProcessor.setInput(inp);

            File f = getTempFile("GN-ELF-ign_fr", ".png");
            logger.info(f.getAbsolutePath());
            FileOutputStream fouts = new FileOutputStream(f);
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

            public void merge(List<JSONObject> list, Resource res) throws IOException {

            }
            public void equalizePropertyArraySize(Map<String,Integer> multiElemmap,  Map<String, Resource> resmap) {

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
                    List<Pair<Resource, Object>> simpleProperties,
                    List<Pair<Resource, Object>> linkProperties)
                    throws IOException {

            }

            @Override
            public void vertex(Resource iri, Resource type,
                    List<Pair<Resource, Object>> simpleProperties,
                    List<Pair<Resource, Object>> linkProperties,
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
                        "/fi/nls/oskari/eu/elf/geographicalnames/geonorge_no-ELF-GN-wfs.xml");

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
    @Ignore("SLD Problem")
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
                        "/fi/nls/oskari/eu/elf/hydronetwork/fgi_fi_wfs_ELF-HY-WatercourseLink-wfs.xml");

        try {
            inputProcessor.setInput(inp);

            File f = getTempFile("HY-fgi_fi-WatercourseLink", ".png");
            logger.info(f.getAbsolutePath());
            FileOutputStream fouts = new FileOutputStream(f);

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


            public void merge(List<JSONObject> list, Resource res) throws IOException {

            }

            public void equalizePropertyArraySize(Map<String,Integer> multiElemmap,  Map<String, Resource> resmap) {

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
                    List<Pair<Resource, Object>> simpleProperties,
                    List<Pair<Resource, Object>> linkProperties)
                    throws IOException {

            }

            @Override
            public void vertex(Resource iri, Resource type,
                    List<Pair<Resource, Object>> simpleProperties,
                    List<Pair<Resource, Object>> linkProperties,
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

        InputStream inp = getClass()
                .getResourceAsStream(
                        "/fi/nls/oskari/eu/elf/geographicalnames/ign_fr-ELF-GN-wfs.xml");

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
    public void test_NlsFi_TN_WFS_GMLtoPNG() throws InstantiationException,
            IllegalAccessException, IOException, XMLStreamException {

        GroovyFeatureEngine engine = new GroovyFeatureEngine();

        XMLInputProcessor inputProcessor = new StaxGMLInputProcessor();

        Style sldStyle = MapContentOutputProcessor
                .createSLDStyle("/fi/nls/oskari/fe/output/style/INSPIRE_SLD/TN.RoadTransportNetwork.RoadLink.Default.sld");

        OutputStreamProcessor outputProcessor = new MapContentOutputProcessor(
                "EPSG:3857", sldStyle);

        InputStream inp = getClass()
                .getResourceAsStream(
                        "/fi/nls/oskari/eu/elf/roadtransportnetwork/nls_fi-ELF-TN-wfs.xml");

        try {
            inputProcessor.setInput(inp);

            File f = getTempFile("TN-nls_fi", ".png");
            logger.info(f.getAbsolutePath());
            FileOutputStream fouts = new FileOutputStream(f);

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
