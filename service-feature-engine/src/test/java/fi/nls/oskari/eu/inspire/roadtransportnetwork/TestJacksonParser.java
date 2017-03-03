package fi.nls.oskari.eu.inspire.roadtransportnetwork;

import fi.nls.oskari.WFSTestHelper;
import fi.nls.oskari.eu.inspire.recipe.roadtransportnetwork.INSPIRE_TN_RoadLink;
import fi.nls.oskari.fe.TestHelper;
import fi.nls.oskari.fe.engine.BasicFeatureEngine;
import fi.nls.oskari.fe.input.XMLInputProcessor;
import fi.nls.oskari.fe.input.format.gml.StaxGMLInputProcessor;
import fi.nls.oskari.fe.input.format.gml.recipe.ParserRecipe;
import fi.nls.oskari.fe.output.OutputStreamProcessor;
import fi.nls.oskari.fe.output.format.json.JsonOutputProcessor;
import fi.nls.oskari.fe.output.format.json.LegacyJsonOutputProcessor;
import fi.nls.oskari.fe.output.format.jsonld.JsonLdOutputProcessor;
import fi.nls.oskari.fe.output.format.png.geotools.MapContentOutputProcessor;
import fi.nls.oskari.log.LogFactory;
import org.apache.log4j.Logger;
import org.geotools.styling.Style;
import org.junit.Test;

import javax.xml.stream.XMLStreamException;
import java.io.*;

public class TestJacksonParser extends TestHelper {
    static final Logger logger = Logger.getLogger(TestJacksonParser.class);

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
                        "/fi/nls/oskari/eu/inspire/roadtransportnetwork/ign_es-inspire-TN-wfs.xml");

        try {
            inputProcessor.setInput(inp);

            File f = getTempFile("TN-BasicParser-ign_es", ".png");
            logger.info(f.getAbsolutePath());
            FileOutputStream fouts = new FileOutputStream(f);

            try {
                outputProcessor.setOutput(fouts);

                ParserRecipe recipe = new INSPIRE_TN_RoadLink();

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
                        "/fi/nls/oskari/eu/inspire/roadtransportnetwork/ign_es-inspire-TN-wfs.xml");

        try {
            inputProcessor.setInput(inp);

            OutputStream fouts = WFSTestHelper.getTestOutputStream();
            try {
                outputProcessor.setOutput(fouts);

                ParserRecipe recipe = new INSPIRE_TN_RoadLink();

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
    // @Ignore("Not ready")
    @Test
    public void test_IgnEs_TN_WFS_GMLtoJSON() throws InstantiationException,
            IllegalAccessException, IOException, XMLStreamException {

        BasicFeatureEngine engine = new BasicFeatureEngine();

        XMLInputProcessor inputProcessor = new StaxGMLInputProcessor();

        OutputStreamProcessor outputProcessor = new JsonOutputProcessor();

        InputStream inp = getClass()
                .getResourceAsStream(
                        "/fi/nls/oskari/eu/inspire/roadtransportnetwork/ign_es-inspire-TN-wfs.xml");

        try {
            inputProcessor.setInput(inp);

            OutputStream fouts = WFSTestHelper.getTestOutputStream();
            try {
                outputProcessor.setOutput(fouts);

                ParserRecipe recipe = new INSPIRE_TN_RoadLink();

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
    // @Ignore("Not ready")
    @Test
    public void test_IgnEs_TN_WFS_GMLtoLegacyJSON()
            throws InstantiationException, IllegalAccessException, IOException,
            XMLStreamException {

        BasicFeatureEngine engine = new BasicFeatureEngine();

        XMLInputProcessor inputProcessor = new StaxGMLInputProcessor();

        OutputStreamProcessor outputProcessor = new LegacyJsonOutputProcessor();

        InputStream inp = getClass()
                .getResourceAsStream(
                        "/fi/nls/oskari/eu/inspire/roadtransportnetwork/ign_es-inspire-TN-wfs.xml");

        try {
            inputProcessor.setInput(inp);

            OutputStream fouts = WFSTestHelper.getTestOutputStream();
            try {
                outputProcessor.setOutput(fouts);

                ParserRecipe recipe = new INSPIRE_TN_RoadLink();

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
