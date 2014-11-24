package fi.nls.oskari.fe.input.format.gml;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.stream.XMLStreamException;

import org.geotools.styling.Style;
import org.junit.Ignore;
import org.junit.Test;

import fi.nls.oskari.fe.engine.BasicFeatureEngine;
import fi.nls.oskari.fe.input.XMLInputProcessor;
import fi.nls.oskari.fe.input.format.gml.recipe.PullParserGMLParserRecipe;
import fi.nls.oskari.fe.input.format.gml.tn.ELF_TN_RoadLinkParserRecipe;
import fi.nls.oskari.fe.input.format.gml.tn.Inspire_TN_RoadLinkParserRecipe;
import fi.nls.oskari.fe.output.OutputStreamProcessor;
import fi.nls.oskari.fe.output.format.json.JsonOutputProcessor;
import fi.nls.oskari.fe.output.format.jsonld.JsonLdOutputProcessor;
import fi.nls.oskari.fe.output.format.png.geotools.MapContentOutputProcessor;

public class TestBasicParser {

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

        BasicFeatureEngine engine = new BasicFeatureEngine();

        XMLInputProcessor inputProcessor = new StaxGMLInputProcessor();

        Style sldStyle = MapContentOutputProcessor
                .createSLDStyle("/fi/nls/oskari/fe/output/style/INSPIRE_SLD/TN.RoadTransportNetwork.RoadLink.Default.sld");

        OutputStreamProcessor outputProcessor = new MapContentOutputProcessor(
                "EPSG:3857", sldStyle);

        InputStream inp = getClass().getResourceAsStream(
                "/fi/nls/oskari/fe/input/format/gml/tn/nls_fi-ELF-TN-wfs.xml");

        try {
            inputProcessor.setInput(inp);

            FileOutputStream fouts = new FileOutputStream(
                    "TN-BasicParser-nls_fi.png");
            try {
                outputProcessor.setOutput(fouts);

                PullParserGMLParserRecipe recipe = new ELF_TN_RoadLinkParserRecipe();
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
    public void test_IgnEs_TN_WFS_GMLtoPNG() throws InstantiationException,
            IllegalAccessException, IOException, XMLStreamException {

        BasicFeatureEngine engine = new BasicFeatureEngine();

        XMLInputProcessor inputProcessor = new StaxGMLInputProcessor();

        Style sldStyle = MapContentOutputProcessor
                .createSLDStyle("/fi/nls/oskari/fe/output/style/INSPIRE_SLD/TN.RoadTransportNetwork.RoadLink.Default.sld");

        OutputStreamProcessor outputProcessor = new MapContentOutputProcessor(
                "EPSG:3857", sldStyle);

        InputStream inp = getClass()
                .getResourceAsStream(
                        "/fi/nls/oskari/fe/input/format/gml/tn/ign_es-inspire-TN-wfs.xml");

        try {
            inputProcessor.setInput(inp);

            FileOutputStream fouts = new FileOutputStream(
                    "TN-BasicParser-ign_es.png");
            try {
                outputProcessor.setOutput(fouts);

                PullParserGMLParserRecipe recipe = new Inspire_TN_RoadLinkParserRecipe();
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
    public void test_IgnEs_TN_WFS_GMLtoJSONLD() throws InstantiationException,
            IllegalAccessException, IOException, XMLStreamException {

        BasicFeatureEngine engine = new BasicFeatureEngine();

        XMLInputProcessor inputProcessor = new StaxGMLInputProcessor();

        OutputStreamProcessor outputProcessor = new JsonLdOutputProcessor();

        InputStream inp = getClass()
                .getResourceAsStream(
                        "/fi/nls/oskari/fe/input/format/gml/tn/ign_es-inspire-TN-wfs.xml");

        try {
            inputProcessor.setInput(inp);

            OutputStream fouts = System.out;
            try {
                outputProcessor.setOutput(fouts);

                PullParserGMLParserRecipe recipe = new Inspire_TN_RoadLinkParserRecipe();
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
    @Ignore("Not ready")
    @Test
    public void test_IgnEs_TN_WFS_GMLtoJSON() throws InstantiationException,
            IllegalAccessException, IOException, XMLStreamException {

        BasicFeatureEngine engine = new BasicFeatureEngine();

        XMLInputProcessor inputProcessor = new StaxGMLInputProcessor();

        OutputStreamProcessor outputProcessor = new JsonOutputProcessor();

        InputStream inp = getClass()
                .getResourceAsStream(
                        "/fi/nls/oskari/fe/input/format/gml/tn/ign_es-inspire-TN-wfs.xml");

        try {
            inputProcessor.setInput(inp);

            OutputStream fouts = System.out;
            try {
                outputProcessor.setOutput(fouts);

                PullParserGMLParserRecipe recipe = new Inspire_TN_RoadLinkParserRecipe();
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
    public void test_NlsFi_TN_WFS_GMLtoJSONLD() throws InstantiationException,
            IllegalAccessException, IOException, XMLStreamException {

        BasicFeatureEngine engine = new BasicFeatureEngine();

        XMLInputProcessor inputProcessor = new StaxGMLInputProcessor();

        OutputStreamProcessor outputProcessor = new JsonLdOutputProcessor();

        InputStream inp = getClass().getResourceAsStream(
                "/fi/nls/oskari/fe/input/format/gml/tn/nls_fi-ELF-TN-wfs.xml");

        try {
            inputProcessor.setInput(inp);

            OutputStream fouts = System.out;
            try {
                outputProcessor.setOutput(fouts);

                PullParserGMLParserRecipe recipe = new ELF_TN_RoadLinkParserRecipe();
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
