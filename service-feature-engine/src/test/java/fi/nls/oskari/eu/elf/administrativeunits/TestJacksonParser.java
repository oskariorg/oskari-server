package fi.nls.oskari.eu.elf.administrativeunits;

import fi.nls.oskari.WFSTestHelper;
import fi.nls.oskari.eu.elf.recipe.administrativeunits.ELF_MasterLoD0_AdministrativeBoundary_nls_fi_wfs_Parser;
import fi.nls.oskari.eu.elf.recipe.administrativeunits.ELF_MasterLoD0_AdministrativeUnit_nls_fi_wfs_Parser;
import fi.nls.oskari.fe.engine.BasicFeatureEngine;
import fi.nls.oskari.fe.input.XMLInputProcessor;
import fi.nls.oskari.fe.input.format.gml.StaxGMLInputProcessor;
import fi.nls.oskari.fe.input.format.gml.recipe.JacksonParserRecipe;
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
    // urn:x-inspire:specification:gmlas:AdministrativeUnits:3.0
    public void test_ELF_Master_LoD1_AU_geonorge_no_wfs_GMLtoJSON()
            throws InstantiationException, IllegalAccessException, IOException,
            XMLStreamException {

        BasicFeatureEngine engine = new BasicFeatureEngine();

        XMLInputProcessor inputProcessor = new StaxGMLInputProcessor();

        OutputStreamProcessor outputProcessor = new JsonOutputProcessor();

        InputStream inp = getClass()
                .getResourceAsStream(
                        "/fi/nls/oskari/eu/elf/administrativeunits/geonorge_no-ELF-AU-wfs.xml");

        try {
            inputProcessor.setInput(inp);

            OutputStream fouts = WFSTestHelper.getTestOutputStream();
            try {
                outputProcessor.setOutput(fouts);

                JacksonParserRecipe recipe = new ELF_MasterLoD0_AdministrativeUnit_nls_fi_wfs_Parser();
                //recipe.getGeometryDeserializer().setIgnoreProps(true);


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
    // urn:x-inspire:specification:gmlas:AdministrativeUnits:3.0
    public void test_ELF_Master_LoD1_AU_boundary_geonorge_no_wfs_GMLtoJSON()
            throws InstantiationException, IllegalAccessException, IOException,
            XMLStreamException {

        BasicFeatureEngine engine = new BasicFeatureEngine();

        XMLInputProcessor inputProcessor = new StaxGMLInputProcessor();

        OutputStreamProcessor outputProcessor = new JsonOutputProcessor();

        InputStream inp = getClass()
                .getResourceAsStream(
                        "/fi/nls/oskari/eu/elf/administrativeBoundary/geonorge_no-ELF-AU-wfs.xml");

        try {
            inputProcessor.setInput(inp);

            OutputStream fouts = WFSTestHelper.getTestOutputStream();
            try {
                outputProcessor.setOutput(fouts);

                JacksonParserRecipe recipe = new ELF_MasterLoD0_AdministrativeBoundary_nls_fi_wfs_Parser();
                //recipe.getGeometryDeserializer().setIgnoreProps(true);


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
