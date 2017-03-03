package fi.nls.oskari.eu.elf.hydronetwork;

import fi.nls.oskari.WFSTestHelper;
import fi.nls.oskari.eu.elf.recipe.hydronetwork.ELF_MasterLoD1_WatercourseLink_Parser;
import fi.nls.oskari.eu.elf.recipe.universal.ELF_path_parse_worker;
import fi.nls.oskari.eu.elf.recipe.universal.ELF_wfs_Parser;
import fi.nls.oskari.fe.TestHelper;
import fi.nls.oskari.fe.engine.BasicFeatureEngine;
import fi.nls.oskari.fe.input.XMLInputProcessor;
import fi.nls.oskari.fe.input.format.gml.StaxGMLInputProcessor;
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
    public void test_ELF_HyP_WFS_GMLtoJSON() throws InstantiationException,
            IllegalAccessException, IOException, XMLStreamException {

        BasicFeatureEngine engine = new BasicFeatureEngine();

        XMLInputProcessor inputProcessor = new StaxGMLInputProcessor();

        OutputStreamProcessor outputProcessor = new JsonOutputProcessor();

        InputStream inp = getClass()
                .getResourceAsStream(
                        "/fi/nls/oskari/eu/elf/hydronetwork/fgi_fi_wfs_ELF-HY-WatercourseLink-wfs.xml");

        try {
            inputProcessor.setInput(inp);

            OutputStream fouts = WFSTestHelper.getTestOutputStream();
            try {
                outputProcessor.setOutput(fouts);

                ParserRecipe recipe = new ELF_MasterLoD1_WatercourseLink_Parser();

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
    public void test_ELF_hyn_WatercourseLink_GMLtoJSON()
            throws InstantiationException, IllegalAccessException, IOException,
            XMLStreamException {

        BasicFeatureEngine engine = new BasicFeatureEngine();

        XMLInputProcessor inputProcessor = new StaxGMLInputProcessor();

        OutputStreamProcessor outputProcessor = new JsonOutputProcessor();

        InputStream inp = getClass().getResourceAsStream(
                "/fi/nls/oskari/eu/elf/hydronetwork/fin_ELF-HYN-wfs.xml");

        String testConf ="{\n" +
                "       \"scan\": {\n" +
                "         \"scanNS\": \"http://www.opengis.net/wfs/2.0\",\n" +
                "         \"name\": \"member\"\n" +
                "       },\n" +
                "       \"root\": { \n" +
                "         \"rootNS\": \"http://inspire.ec.europa.eu/schemas/hy-n/4.0\",\n" +
                "         \"name\": \"WatercourseLink\"\n" +
                "       },\n" +
                "       \"paths\": [\n" +
                "         {\n" +
                "           \"path\": \"/hy-n:WatercourseLink/@gml:id\",\n" +
                "           \"type\": \"String\",\n" +
                "           \"label\": \"id\"\n" +
                "         },\n" +
                "         {\n" +
                "           \"path\": \"/hy-n:WatercourseLink/net:beginLifespanVersion\",\n" +
                "           \"type\": \"String\",\n" +
                "           \"label\": \"Begin Lifespan\"\n" +
                "         },\n" +
                "         {\n" +
                "           \"path\": \"/hy-n:WatercourseLink/net:inNetwork\",\n" +
                "           \"type\": \"String\",\n" +
                "           \"label\": \"In Network\"\n" +
                "         },\n" +
                "         {\n" +
                "           \"path\": \"/hy-n:WatercourseLink/net:fictitious\",\n" +
                "           \"type\": \"String\",\n" +
                "           \"label\": \"Fictitious\"\n" +
                "         },\n" +
                "         {\n" +
                "           \"path\": \"/hy-n:WatercourseLink/hy-n:flowDirection/@xlink:href\",\n" +
                "           \"type\": \"String\",\n" +
                "           \"label\": \"Flow Direction\"\n" +
                "         },\n" +
                "         {\n" +
                "           \"path\": \"/hy-n:WatercourseLink/hy-n:length/@:uom\",\n" +
                "           \"type\": \"String\",\n" +
                "           \"label\": \"UOM\"\n" +
                "         },\n" +
                "         {\n" +
                "           \"path\": \"/hy-n:WatercourseLink/hy-n:length\",\n" +
                "           \"type\": \"String\",\n" +
                "           \"label\": \"Length\"\n" +
                "         },\n" +
                "         {\n" +
                "           \"path\": \"/hy-n:WatercourseLink/net:centrelineGeometry\",\n" +
                "           \"type\": \"Geometry\",\n" +
                "           \"label\": \"geom\"\n" +
                "         }\n" +
                "       ]\n" +
                "     }";


        JSONObject conf = JSONHelper.createJSONObject(testConf);


        try {
            inputProcessor.setInput(inp);


            OutputStream fouts = WFSTestHelper.getTestOutputStream();
            try {
                outputProcessor.setOutput(fouts);

                ParserRecipe recipe = new ELF_wfs_Parser();
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
