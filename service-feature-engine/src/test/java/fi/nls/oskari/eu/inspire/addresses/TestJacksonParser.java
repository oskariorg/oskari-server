package fi.nls.oskari.eu.inspire.addresses;

import fi.nls.oskari.WFSTestHelper;
import fi.nls.oskari.eu.inspire.recipe.addresses.INSPIRE_AD_Address_Parser;
import fi.nls.oskari.fe.engine.BasicFeatureEngine;
import fi.nls.oskari.fe.input.XMLInputProcessor;
import fi.nls.oskari.fe.input.format.gml.StaxGMLInputProcessor;
import fi.nls.oskari.fe.input.format.gml.recipe.ParserRecipe;
import fi.nls.oskari.fe.output.OutputStreamProcessor;
import fi.nls.oskari.fe.output.format.json.JsonOutputProcessor;
import org.junit.Test;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class TestJacksonParser {

    /**
     * 
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws IOException
     * @throws XMLStreamException
     */
    // @Ignore("Not ready")
    @Test
    public void test_Inspire_Address_ign_es_wfs_GMLtoJSON()
            throws InstantiationException, IllegalAccessException, IOException,
            XMLStreamException {

        BasicFeatureEngine engine = new BasicFeatureEngine();

        XMLInputProcessor inputProcessor = new StaxGMLInputProcessor();

        OutputStreamProcessor outputProcessor = new JsonOutputProcessor();

        InputStream inp = getClass()
                .getResourceAsStream(
                        "/fi/nls/oskari/eu/inspire/addresses/ign_es-INSPIRE-AD-Address.xml");

        try {
            inputProcessor.setInput(inp);

            OutputStream fouts = WFSTestHelper.getTestOutputStream();
            try {
                outputProcessor.setOutput(fouts);

                ParserRecipe recipe = new INSPIRE_AD_Address_Parser();

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

    /**
     * 
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws IOException
     * @throws XMLStreamException
     */
    // @Ignore("Not ready")
    @Test
    public void test_Inspire_Address_cuzk_cz_wfs_GMLtoJSON()
            throws InstantiationException, IllegalAccessException, IOException,
            XMLStreamException {

        BasicFeatureEngine engine = new BasicFeatureEngine();

        XMLInputProcessor inputProcessor = new StaxGMLInputProcessor();

        OutputStreamProcessor outputProcessor = new JsonOutputProcessor();

        InputStream inp = getClass()
                .getResourceAsStream(
                        "/fi/nls/oskari/eu/inspire/addresses/cuzk_cz-INSPIRE-AD-Address.xml");

        try {
            inputProcessor.setInput(inp);

            OutputStream fouts = WFSTestHelper.getTestOutputStream();
            try {
                outputProcessor.setOutput(fouts);

                ParserRecipe recipe = new INSPIRE_AD_Address_Parser();

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
