package fi.nls.oskari.fe.input.format.gml;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.codehaus.staxmate.in.SMInputCursor;
import org.geotools.styling.Style;
import org.junit.Test;
import org.xml.sax.SAXException;

import com.vividsolutions.jts.geom.Geometry;

import fi.nls.oskari.fe.engine.BasicFeatureEngine;
import fi.nls.oskari.fe.input.XMLInputProcessor;
import fi.nls.oskari.fe.input.format.gml.FEPullParser.PullParserHandler;
import fi.nls.oskari.fe.input.format.gml.recipe.AbstractGroovyGMLParserRecipe;
import fi.nls.oskari.fe.input.format.gml.recipe.GML31_Configuration;
import fi.nls.oskari.fe.input.format.gml.recipe.PullParserGMLParserRecipe;
import fi.nls.oskari.fe.input.format.gml.recipe.StaxMateGMLParserRecipeBase;
import fi.nls.oskari.fe.input.format.gml.recipe.StaxMateXMLParserRecipeBase.InputEvent;
import fi.nls.oskari.fe.input.format.gml.tn.ELF_TN_RoadLinkParserRecipe;
import fi.nls.oskari.fe.iri.Resource;
import fi.nls.oskari.fe.output.OutputProcessor;
import fi.nls.oskari.fe.output.OutputStreamProcessor;
import fi.nls.oskari.fe.output.format.png.geotools.MapContentOutputProcessor;
import fi.nls.oskari.fe.schema.XSDDatatype;

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
}
