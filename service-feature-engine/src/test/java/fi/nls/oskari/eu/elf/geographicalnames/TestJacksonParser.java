package fi.nls.oskari.eu.elf.geographicalnames;

import fi.nls.oskari.WFSTestHelper;
import fi.nls.oskari.eu.elf.recipe.geographicalnames.ELF_MasterLoD1_NamedPlace_Parser;
import fi.nls.oskari.eu.elf.recipe.universal.ELF_path_parse_worker;
import fi.nls.oskari.eu.elf.recipe.universal.ELF_wfs_Parser;
import fi.nls.oskari.fe.engine.BasicFeatureEngine;
import fi.nls.oskari.fe.input.XMLInputProcessor;
import fi.nls.oskari.fe.input.format.gml.StaxGMLInputProcessor;
import fi.nls.oskari.fe.input.format.gml.recipe.ParserRecipe;
import fi.nls.oskari.fe.output.OutputStreamProcessor;
import fi.nls.oskari.fe.output.format.json.JsonOutputProcessor;
import fi.nls.oskari.util.JSONHelper;
import org.json.JSONObject;
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
    public void test_ELF_Master_LoD1_NamedPlace_nls_fi_GMLtoJSON()
            throws InstantiationException, IllegalAccessException, IOException,
            XMLStreamException {

        BasicFeatureEngine engine = new BasicFeatureEngine();

        XMLInputProcessor inputProcessor = new StaxGMLInputProcessor();

        OutputStreamProcessor outputProcessor = new JsonOutputProcessor();

        InputStream inp = getClass()
                .getResourceAsStream(
                     //   "/fi/nls/oskari/eu/elf/geographicalnames/nls_fi-ELF-GN-wfs.xml");
                          "/fi/nls/oskari/eu/elf/geographicalnames/geonorge_no-ELF-GN-wfs.xml");


        try {
            inputProcessor.setInput(inp);

            OutputStream fouts = WFSTestHelper.getTestOutputStream();
            try {
                outputProcessor.setOutput(fouts);

                ELF_MasterLoD1_NamedPlace_Parser recipe = new ELF_MasterLoD1_NamedPlace_Parser();
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
    public void test_ELF_Master_LoD1_NamedPlace_geonorge_no_GMLtoJSON()
            throws InstantiationException, IllegalAccessException, IOException,
            XMLStreamException {

        BasicFeatureEngine engine = new BasicFeatureEngine();

        XMLInputProcessor inputProcessor = new StaxGMLInputProcessor();

        OutputStreamProcessor outputProcessor = new JsonOutputProcessor();

        InputStream inp = getClass()
                .getResourceAsStream(
                        "/fi/nls/oskari/eu/elf/geographicalnames/geonorge_no-ELF-GN-wfs.xml");

        try {
            inputProcessor.setInput(inp);

            OutputStream fouts = WFSTestHelper.getTestOutputStream();
            try {
                outputProcessor.setOutput(fouts);

                ELF_MasterLoD1_NamedPlace_Parser recipe = new ELF_MasterLoD1_NamedPlace_Parser();
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
    public void test_ELF_Master_elfgn_geographicalnames_wfs_GMLtoJSON()
            throws InstantiationException, IllegalAccessException, IOException,
            XMLStreamException {

        BasicFeatureEngine engine = new BasicFeatureEngine();

        XMLInputProcessor inputProcessor = new StaxGMLInputProcessor();

        OutputStreamProcessor outputProcessor = new JsonOutputProcessor();

        InputStream inp = getClass().getResourceAsStream(
                "/fi/nls/oskari/eu/elf/geographicalnames/ign_no_-ELF-GN-wfs.xml");

        String testConf ="{\n" +
                "  \"scan\": {\n" +
                "    \"scanNS\": \"http://www.opengis.net/wfs/2.0\",\n" +
                "    \"name\": \"member\"\n" +
                "  },\n" +
                "  \"root\": {\n" +
                "    \"rootNS\": \"http://www.locationframework.eu/schemas/GeographicalNames/1.0\",\n" +
                "    \"name\": \"NamedPlace\"\n" +
                "  },\n" +
                "  \"paths\": [{\n" +
                "    \"path\": \"/elf-gn:NamedPlace/@gml:id\",\n" +
                "    \"type\": \"String\",\n" +
                "    \"label\": \"id\"\n" +
                "  }, {\n" +
                "    \"path\": \"/elf-gn:NamedPlace/gn:inspireId/base:Identifier/base:localId\",\n" +
                "    \"type\": \"String\",\n" +
                "    \"label\": \"INSPIRE Local ID\"\n" +
                "  }, {\n" +
                "    \"path\": \"/elf-gn:NamedPlace/gn:localType/gmd:LocalisedCharacterString\",\n" +
                "    \"type\": \"String\",\n" +
                "    \"label\": \"Type\"\n" +
                "  }, {\n" +
                "    \"path\": \"/elf-gn:NamedPlace/gn:name/elf-gn:GeographicalName/gn:spelling/gn:SpellingOfName/gn:text\",\n" +
                "    \"type\": \"String\",\n" +
                "    \"label\": \"Name\"\n" +
                "  }, {\n" +
                "    \"path\": \"/elf-gn:NamedPlace/gn:beginLifespanVersion\",\n" +
                "    \"type\": \"String\",\n" +
                "    \"label\": \"Lifespan\"\n" +
                "  }, {\n" +
                "    \"path\": \"/elf-gn:NamedPlace/gn:geometry\",\n" +
                "    \"type\": \"Geometry\",\n" +
                "    \"label\": \"geom\"\n" +
                "  }]\n" +
                "}";

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
