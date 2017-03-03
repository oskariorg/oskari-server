package fi.nls.oskari.fi.rysp.kantakartta;

import fi.nls.oskari.WFSTestHelper;
import fi.nls.oskari.fe.TestHelper;
import fi.nls.oskari.fe.engine.BasicFeatureEngine;
import fi.nls.oskari.fe.input.XMLInputProcessor;
import fi.nls.oskari.fe.input.format.gml.StaxGMLInputProcessor;
import fi.nls.oskari.fe.input.format.gml.recipe.JacksonParserRecipe;
import fi.nls.oskari.fe.output.OutputStreamProcessor;
import fi.nls.oskari.fe.output.format.json.JsonOutputProcessor;
import fi.nls.oskari.fi.rysp.recipe.kanta.RYSP_kanta_Liikennevayla_Parser;
import fi.nls.oskari.fi.rysp.recipe.kanta.RYSP_kanta_Rakennus_Parser;
import org.apache.log4j.Logger;
import org.junit.Test;

import javax.xml.stream.XMLStreamException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class TestJacksonParser extends TestHelper {
    static final Logger logger = Logger.getLogger(TestJacksonParser.class);

    @Test
    public void test_RYSP_kanta_Liikennevayla_wfs_GMLtoJSON()
            throws InstantiationException, IllegalAccessException, IOException,
            XMLStreamException {

        BasicFeatureEngine engine = new BasicFeatureEngine();

        XMLInputProcessor inputProcessor = new StaxGMLInputProcessor();

        OutputStreamProcessor outputProcessor = new JsonOutputProcessor();

        InputStream inp = getClass().getResourceAsStream(
                "/fi/nls/oskari/fi/rysp/kanta_Liikennevayla.xml");

        try {
            inputProcessor.setInput(inp);

            OutputStream fouts = WFSTestHelper.getTestOutputStream();
            try {
                outputProcessor.setOutput(fouts);

                JacksonParserRecipe recipe = new RYSP_kanta_Liikennevayla_Parser();
                // recipe.getGeometryDeserializer().setIgnoreProps(true);
                // recipe.setLenient(true);
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

    // @Ignore("Unfinished - ATM requires some manual tuning for Jackson mappers plus receives some non-schema data from Service")
    @Test
    public void test_RYSP_kanta_Rakennus_wfs_GMLtoJSON()
            throws InstantiationException, IllegalAccessException, IOException,
            XMLStreamException {

        BasicFeatureEngine engine = new BasicFeatureEngine();

        XMLInputProcessor inputProcessor = new StaxGMLInputProcessor();

        OutputStreamProcessor outputProcessor = new JsonOutputProcessor();

        InputStream inp = getClass().getResourceAsStream(
                "/fi/nls/oskari/fi/rysp/kanta_Rakennus.xml");

        try {
            inputProcessor.setInput(inp);

            java.io.File f = getTempFile("RYSP_kanta_Rakennus", ".json");
            logger.info(f.getAbsolutePath());
            OutputStream fouts = new FileOutputStream(f);
            try {
                outputProcessor.setOutput(fouts);

                JacksonParserRecipe recipe = new RYSP_kanta_Rakennus_Parser();
                // recipe.getGeometryDeserializer().setIgnoreProps(true);
                
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
