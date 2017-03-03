package fi.nls.oskari.eu.inspire.geographicalnames;

import fi.nls.oskari.WFSTestHelper;
import fi.nls.oskari.eu.inspire.recipe.geographicalnames.INSPIRE_GN_NamedPlace;
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
    public void test_IgnEs_GN_WFS_GMLtoJSON() throws InstantiationException,
            IllegalAccessException, IOException, XMLStreamException {

        BasicFeatureEngine engine = new BasicFeatureEngine();

        XMLInputProcessor inputProcessor = new StaxGMLInputProcessor();

        OutputStreamProcessor outputProcessor = new JsonOutputProcessor();

        InputStream inp = getClass()
                .getResourceAsStream(
                        "/fi/nls/oskari/eu/inspire/geographicalnames/ign_es-INSPIRE-GN-wfs.xml");

        try {
            inputProcessor.setInput(inp);

            OutputStream fouts = WFSTestHelper.getTestOutputStream();
            try {
                outputProcessor.setOutput(fouts);

                ParserRecipe recipe = new INSPIRE_GN_NamedPlace();
                
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
