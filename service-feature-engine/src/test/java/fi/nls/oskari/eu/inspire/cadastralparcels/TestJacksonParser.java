package fi.nls.oskari.eu.inspire.cadastralparcels;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.stream.XMLStreamException;

import org.junit.Test;

import fi.nls.oskari.eu.inspire.recipe.cadastralparcels.INSPIRE_CP_CadastralBoundary_Parser;
import fi.nls.oskari.eu.inspire.recipe.cadastralparcels.INSPIRE_CP_CadastralParcel_Parser;
import fi.nls.oskari.fe.engine.BasicFeatureEngine;
import fi.nls.oskari.fe.input.XMLInputProcessor;
import fi.nls.oskari.fe.input.format.gml.StaxGMLInputProcessor;
import fi.nls.oskari.fe.input.format.gml.recipe.JacksonParserRecipe;
import fi.nls.oskari.fe.output.OutputStreamProcessor;
import fi.nls.oskari.fe.output.format.json.JsonOutputProcessor;

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
    public void test_Inspire_CadastralParcel_cuzk_cz_wfs_GMLtoJSON()
            throws InstantiationException, IllegalAccessException, IOException,
            XMLStreamException {

        BasicFeatureEngine engine = new BasicFeatureEngine();

        XMLInputProcessor inputProcessor = new StaxGMLInputProcessor();

        OutputStreamProcessor outputProcessor = new JsonOutputProcessor();

        InputStream inp = getClass()
                .getResourceAsStream(
                        "/fi/nls/oskari/eu/inspire/cadastralparcels/cuzk_cz-INSPIRE-CP-CadastralParcel.xml");

        try {
            inputProcessor.setInput(inp);

            OutputStream fouts = System.out;
            try {
                outputProcessor.setOutput(fouts);

                JacksonParserRecipe recipe = new INSPIRE_CP_CadastralParcel_Parser();
                recipe.setLenient(true);
                // recipe.getGeometryDeserializer().setIgnoreProps(true);

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
    public void test_Inspire_CadastralBoundary_cuzk_cz_wfs_GMLtoJSON()
            throws InstantiationException, IllegalAccessException, IOException,
            XMLStreamException {

        BasicFeatureEngine engine = new BasicFeatureEngine();

        XMLInputProcessor inputProcessor = new StaxGMLInputProcessor();

        OutputStreamProcessor outputProcessor = new JsonOutputProcessor();

        InputStream inp = getClass()
                .getResourceAsStream(
                        "/fi/nls/oskari/eu/inspire/cadastralparcels/cuzk_cz-INSPIRE-CP-CadastralBoundary.xml");

        try {
            inputProcessor.setInput(inp);

            OutputStream fouts = System.out;
            try {
                outputProcessor.setOutput(fouts);

                JacksonParserRecipe recipe = new INSPIRE_CP_CadastralBoundary_Parser();
                recipe.setLenient(true);
                // recipe.getGeometryDeserializer().setIgnoreProps(true);

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
