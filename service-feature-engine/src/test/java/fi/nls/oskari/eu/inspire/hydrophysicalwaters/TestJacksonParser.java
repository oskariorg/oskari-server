package fi.nls.oskari.eu.inspire.hydrophysicalwaters;

import fi.nls.oskari.WFSTestHelper;
import fi.nls.oskari.eu.elf.recipe.universal.ELF_path_parse_worker;
import fi.nls.oskari.eu.elf.recipe.universal.ELF_wfs_DefaultParser;
import fi.nls.oskari.eu.inspire.recipe.hydrophysicalwaters.INSPIRE_HYp_LandWaterBoundary_Parser;
import fi.nls.oskari.eu.inspire.recipe.hydrophysicalwaters.INSPIRE_HYp_StandingWater_Parser;
import fi.nls.oskari.eu.inspire.recipe.hydrophysicalwaters.INSPIRE_HYp_Watercourse_Parser;
import fi.nls.oskari.fe.TestHelper;
import fi.nls.oskari.fe.engine.BasicFeatureEngine;
import fi.nls.oskari.fe.input.XMLInputProcessor;
import fi.nls.oskari.fe.input.format.gml.StaxGMLInputProcessor;
import fi.nls.oskari.fe.input.format.gml.recipe.JacksonParserRecipe;
import fi.nls.oskari.fe.input.format.gml.recipe.ParserRecipe;
import fi.nls.oskari.fe.output.OutputStreamProcessor;
import fi.nls.oskari.fe.output.format.json.JsonOutputProcessor;
import fi.nls.oskari.util.JSONHelper;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.junit.Test;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class TestJacksonParser extends TestHelper {

    static final Logger logger = Logger.getLogger(TestJacksonParser.class);

    /**
     * 
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws IOException
     * @throws XMLStreamException
     */

    /**
     * 
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws IOException
     * @throws XMLStreamException
     */
    // @Ignore("Not ready")
    @Test
    public void test_INSPIRE_CascadingWFS_StandingWaterGMLtoJSON()
            throws InstantiationException, IllegalAccessException, IOException,
            XMLStreamException {

        BasicFeatureEngine engine = new BasicFeatureEngine();

        XMLInputProcessor inputProcessor = new StaxGMLInputProcessor();

        OutputStreamProcessor outputProcessor = new JsonOutputProcessor();

        InputStream inp = getClass()
                .getResourceAsStream(
                        "/fi/nls/oskari/eu/inspire/hydrophysicalwaters/fgi_fi-INSPIRE-HY-StandingWater-wfs.xml");

        try {
            inputProcessor.setInput(inp);

            OutputStream fouts = WFSTestHelper.getTestOutputStream();
            try {
                outputProcessor.setOutput(fouts);

                JacksonParserRecipe recipe = new INSPIRE_HYp_StandingWater_Parser();
                //recipe.getGeometryDeserializer().setIgnoreProps(true);

                recipe.setLenient(true);
                engine.setRecipe(recipe);

                engine.setInputProcessor(inputProcessor);
                engine.setOutputProcessor(outputProcessor);

                engine.process();

            } finally {
                // fouts.close();
            }

        } finally {
            inp.close();
        }

    }

    @Test
    public void test_INSPIRE_CascadingWFS_WatercourseGMLtoJSON()
            throws InstantiationException, IllegalAccessException, IOException,
            XMLStreamException {

        BasicFeatureEngine engine = new BasicFeatureEngine();

        XMLInputProcessor inputProcessor = new StaxGMLInputProcessor();

        OutputStreamProcessor outputProcessor = new JsonOutputProcessor();

        InputStream inp = getClass()
                .getResourceAsStream(
                        "/fi/nls/oskari/eu/inspire/hydrophysicalwaters/fgi_fi-INSPIRE-HY-Watercourse-wfs.xml");

        try {
            inputProcessor.setInput(inp);

            OutputStream fouts = WFSTestHelper.getTestOutputStream();
            try {
                outputProcessor.setOutput(fouts);

                JacksonParserRecipe recipe = new INSPIRE_HYp_Watercourse_Parser();
                //recipe.getGeometryDeserializer().setIgnoreProps(true);

                recipe.setLenient(true);
                engine.setRecipe(recipe);

                engine.setInputProcessor(inputProcessor);
                engine.setOutputProcessor(outputProcessor);

                engine.process();

            } finally {
                // fouts.close();
            }

        } finally {
            inp.close();
        }

    }

    @Test
    public void test_INSPIRE_GeonorgeNoWFS_WatercourseGMLtoJSON()
            throws InstantiationException, IllegalAccessException, IOException,
            XMLStreamException {

        BasicFeatureEngine engine = new BasicFeatureEngine();

        XMLInputProcessor inputProcessor = new StaxGMLInputProcessor();

        OutputStreamProcessor outputProcessor = new JsonOutputProcessor();

        InputStream inp = getClass()
                .getResourceAsStream(
                        "/fi/nls/oskari/eu/inspire/hydrophysicalwaters/geonorge_no-INSPIRE-HY-Watercourse-wfs.xml");

        try {
            inputProcessor.setInput(inp);

            OutputStream fouts = WFSTestHelper.getTestOutputStream();
            try {
                outputProcessor.setOutput(fouts);

                ParserRecipe recipe = new INSPIRE_HYp_Watercourse_Parser();
                engine.setRecipe(recipe);

                engine.setInputProcessor(inputProcessor);
                engine.setOutputProcessor(outputProcessor);

                engine.process();

            } finally {
                // fouts.close();
            }

        } finally {
            inp.close();
        }

    }

    @Test
    public void test_INSPIRE_GeonorgeNoWFS_StandingWaterGMLtoJSON()
            throws InstantiationException, IllegalAccessException, IOException,
            XMLStreamException {

        BasicFeatureEngine engine = new BasicFeatureEngine();

        XMLInputProcessor inputProcessor = new StaxGMLInputProcessor();

        OutputStreamProcessor outputProcessor = new JsonOutputProcessor();

        InputStream inp = getClass()
                .getResourceAsStream(
                        "/fi/nls/oskari/eu/inspire/hydrophysicalwaters/geonorge_no-INSPIRE-HY-StandingWater-wfs.xml");

        try {
            inputProcessor.setInput(inp);

            OutputStream fouts = WFSTestHelper.getTestOutputStream();
            try {
                outputProcessor.setOutput(fouts);

                ParserRecipe recipe = new INSPIRE_HYp_StandingWater_Parser();

                engine.setRecipe(recipe);

                engine.setInputProcessor(inputProcessor);
                engine.setOutputProcessor(outputProcessor);

                engine.process();

            } finally {
                // fouts.close();
            }

        } finally {
            inp.close();
        }

    }

    @Test
    public void test_INSPIRE_GeonorgeNoWFS_LandWaterBoundaryGMLtoJSON()
            throws InstantiationException, IllegalAccessException, IOException,
            XMLStreamException {

        BasicFeatureEngine engine = new BasicFeatureEngine();

        XMLInputProcessor inputProcessor = new StaxGMLInputProcessor();

        OutputStreamProcessor outputProcessor = new JsonOutputProcessor();

        InputStream inp = getClass()
                .getResourceAsStream(
                        "/fi/nls/oskari/eu/inspire/hydrophysicalwaters/geonorge_no-INSPIRE-HY-LandWaterBoundary-wfs.xml");

        try {
            inputProcessor.setInput(inp);

            OutputStream fouts = WFSTestHelper.getTestOutputStream();
            try {
                outputProcessor.setOutput(fouts);

                ParserRecipe recipe = new INSPIRE_HYp_LandWaterBoundary_Parser();

                engine.setRecipe(recipe);

                engine.setInputProcessor(inputProcessor);
                engine.setOutputProcessor(outputProcessor);

                engine.process();

            } finally {
                // fouts.close();
            }

        } finally {
            inp.close();
        }

    }

    @Test
    public void test_INSPIRE_HYP_CascadingWFS_WatercourseGMLtoJSON()
            throws InstantiationException, IllegalAccessException, IOException,
            XMLStreamException {

        BasicFeatureEngine engine = new BasicFeatureEngine();

        XMLInputProcessor inputProcessor = new StaxGMLInputProcessor();

        OutputStreamProcessor outputProcessor = new JsonOutputProcessor();

        InputStream inp = getClass()
                .getResourceAsStream(
                        "/fi/nls/oskari/eu/inspire/hydrophysicalwaters/fgi_fi-INSPIRE-HYP-Watercourse-wfs.xml");

        try {
            inputProcessor.setInput(inp);

            OutputStream fouts = WFSTestHelper.getTestOutputStream();
            try {
                outputProcessor.setOutput(fouts);

                JacksonParserRecipe recipe = new INSPIRE_HYp_Watercourse_Parser();
                //recipe.getGeometryDeserializer().setIgnoreProps(true);

                recipe.setLenient(true);
                engine.setRecipe(recipe);

                engine.setInputProcessor(inputProcessor);
                engine.setOutputProcessor(outputProcessor);

                engine.process();

            } finally {
                // fouts.close();
            }

        } finally {
            inp.close();
        }

    }

    @Test
    public void test_INSPIRE_HYP_geonorge_DefaultPathParser_WFS_WatercourseGMLtoJSON()
            throws InstantiationException, IllegalAccessException, IOException,
            XMLStreamException {

        BasicFeatureEngine engine = new BasicFeatureEngine();

        XMLInputProcessor inputProcessor = new StaxGMLInputProcessor();

        OutputStreamProcessor outputProcessor = new JsonOutputProcessor();

        InputStream inp = getClass()
                .getResourceAsStream(
                        "/fi/nls/oskari/eu/inspire/hydrophysicalwaters/geonorge_no-INSPIRE-HY-StandingWater-wfs.xml");

        String testConf ="{\"paths\":[{\"path\":\"/hy-p:StandingWater/@gml:id\",\"label\":\"id\",\"type\":\"String\"}],\"root\":{\"rootNS\":\"urn:x-inspire:specification:gmlas:HydroPhysicalWaters:3.0\",\"name\":\"StandingWater\"},\"scan\":{\"scanNS\":\"http://www.opengis.net/wfs/2.0\",\"name\":\"member\"}} ";


        JSONObject conf = JSONHelper.createJSONObject(testConf);


        try {
            inputProcessor.setInput(inp);


            OutputStream fouts = WFSTestHelper.getTestOutputStream();
            try {
                outputProcessor.setOutput(fouts);

                ParserRecipe recipe = new ELF_wfs_DefaultParser();
                ELF_path_parse_worker worker = new ELF_path_parse_worker(conf);
                recipe.setParseWorker(worker);

                engine.setRecipe(recipe);

                engine.setInputProcessor(inputProcessor);
                engine.setOutputProcessor(outputProcessor);

                engine.process();

            } finally {
                // fouts.close();
            }

        } finally {
            inp.close();
        }

    }
}
