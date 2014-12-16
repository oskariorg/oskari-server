package fi.nls.oskari.fi.rysp.kantakartta;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.stream.XMLStreamException;

import org.junit.Ignore;
import org.junit.Test;

import fi.nls.oskari.fe.engine.BasicFeatureEngine;
import fi.nls.oskari.fe.input.XMLInputProcessor;
import fi.nls.oskari.fe.input.format.gml.StaxGMLInputProcessor;
import fi.nls.oskari.fe.input.format.gml.recipe.ParserRecipe;
import fi.nls.oskari.fe.output.OutputStreamProcessor;
import fi.nls.oskari.fe.output.format.json.JsonOutputProcessor;
import fi.nls.oskari.fi.rysp.recipe.kanta.RYSP_kanta_Liikennevayla_Parser;
import fi.nls.oskari.fi.rysp.recipe.kanta.RYSP_kanta_Rakennus_Parser;

public class TestJacksonParser {

    @Test
    public void test_RYSP_kanta_Liikennevayla_wfs_GMLtoJSON()
            throws InstantiationException, IllegalAccessException, IOException,
            XMLStreamException {

        BasicFeatureEngine engine = new BasicFeatureEngine();

        XMLInputProcessor inputProcessor = new StaxGMLInputProcessor();

        OutputStreamProcessor outputProcessor = new JsonOutputProcessor();

        InputStream inp = getClass()
                .getResourceAsStream(
                        "/fi/nls/oskari/fi/rysp/kanta_Liikennevayla.xml");

        try {
            inputProcessor.setInput(inp);

            OutputStream fouts = System.out;
            try {
                outputProcessor.setOutput(fouts);

                ParserRecipe recipe = new RYSP_kanta_Liikennevayla_Parser();
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

    @Ignore("Unfinished")
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

            OutputStream fouts = System.out;
            try {
                outputProcessor.setOutput(fouts);

                RYSP_kanta_Rakennus_Parser recipe = new RYSP_kanta_Rakennus_Parser();
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
}
