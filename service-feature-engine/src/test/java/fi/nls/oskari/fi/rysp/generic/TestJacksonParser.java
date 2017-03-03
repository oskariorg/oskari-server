package fi.nls.oskari.fi.rysp.generic;

import fi.nls.oskari.WFSTestHelper;
import fi.nls.oskari.eu.elf.recipe.addresses.ELF_MasterLoD0_Address_nls_fi_wfs_Parser;
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
    //@Ignore("Not ready")

    @Test
    public void test_RYSP_Rakennusala_PathParser_local_href_wfs_GMLtoJSON()
            throws InstantiationException, IllegalAccessException, IOException,
            XMLStreamException {

        BasicFeatureEngine engine = new BasicFeatureEngine();

        XMLInputProcessor inputProcessor = new StaxGMLInputProcessor();

        OutputStreamProcessor outputProcessor = new JsonOutputProcessor();

        InputStream inp = getClass().getResourceAsStream(
                "/fi/nls/oskari/fi/rysp/akaava_Rakennusala.xml");

        String testConf = "{\n" +
                "\t\"paths\": [{\n" +
                "\t\t\"path\": \"/akaava:Rakennusala/@gml:id\",\n" +
                "\t\t\"label\": \"id\",\n" +
                "\t\t\"type\": \"String\"\n" +
                "\t}, {\n" +
                "\t\t\"path\": \"/akaava:Rakennusala/akaava:tunnus\",\n" +
                "\t\t\"label\": \"Tunnus\",\n" +
                "\t\t\"type\": \"String\"\n" +
                "\t}, {\n" +
                "\t\t\"path\": \"/akaava:Rakennusala/akaava:kaavatunnus\",\n" +
                "\t\t\"label\": \"Kaavatunnus\",\n" +
                "\t\t\"type\": \"String\"\n" +
                "\t}, {\n" +
                "\t\t\"path\": \"/akaava:Rakennusala/akaava:kaavamerkinta\",\n" +
                "\t\t\"label\": \"Kaavamerkintä\",\n" +
                "\t\t\"type\": \"String\"\n" +
                "\t}, {\n" +
                "\t\t\"path\": \"/akaava:Rakennusala/akaava:sijainti/akaava:AlueSijainti/akaava:reunaviiva\",\n" +
                "\t\t\"label\": \"geom\",\n" +
                "\t\t\"type\": \"Geometry\"\n" +
                "\t},{\n" +
                "\t\t\"path\": \"/akaava:Rakennusala/akaava:sijainti/akaava:AlueSijainti/akaava:alue\",\n" +
                "\t\t\"label\": \"geom\",\n" +
                "\t\t\"type\": \"Geometry\"\n" +
                "\t}],\n" +
                "\t\"root\": {\n" +
                "\t\t\"rootNS\": \"http://www.paikkatietopalvelu.fi/gml/asemakaava\",\n" +
                "\t\t\"name\": \"Rakennusala\"\n" +
                "\t},\n" +
                "\t\"scan\": {\n" +
                "\t\t\"scanNS\": \"http://www.opengis.net/gml\",\n" +
                "\t\t\"name\": \"featureMember\"\n" +
                "\t}\n" +
                "}";

        JSONObject conf = JSONHelper.createJSONObject(testConf);


        try {
            inputProcessor.setInput(inp);


            OutputStream fouts = WFSTestHelper.getTestOutputStream();
            try {
                outputProcessor.setOutput(fouts);

                ParserRecipe recipe = new RYSP_wfs_Parser();
                WFS11_path_parse_worker worker = new WFS11_path_parse_worker(conf);
                recipe.setWFS11ParseWorker(worker);

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
    public void test_RYSP_Kiinteisto_PathParser_local_href_wfs_GMLtoJSON()
            throws InstantiationException, IllegalAccessException, IOException,
            XMLStreamException {

        BasicFeatureEngine engine = new BasicFeatureEngine();

        XMLInputProcessor inputProcessor = new StaxGMLInputProcessor();

        OutputStreamProcessor outputProcessor = new JsonOutputProcessor();

        InputStream inp = getClass().getResourceAsStream(
                "/fi/nls/oskari/fi/rysp/kanta_Kiinteisto.xml");

        String testConf = "{\n" +
                "  \"paths\": [{\n" +
                "    \"path\": \"/kanta:Kiinteisto/@gml:id\",\n" +
                "    \"label\": \"id\",\n" +
                "    \"type\": \"String\"\n" +
                "  }, {\n" +
                "    \"path\": \"/kanta:Kiinteisto/kanta:kiinteistotunnus\",\n" +
                "    \"label\": \"Kiinteistotunnus\",\n" +
                "    \"type\": \"String\"\n" +
                "  }, {\n" +
                "    \"path\": \"/kanta:Kiinteisto/kanta:rekisteriyksikkolaji\",\n" +
                "    \"label\": \"Laji\",\n" +
                "    \"type\": \"String\"\n" +
                "  }, {\n" +
                "    \"path\": \"/kanta:Kiinteisto/kanta:kplkoodi\",\n" +
                "    \"label\": \"Kplkoodi\",\n" +
                "    \"type\": \"String\"\n" +
                "  },  {\n" +
                "    \"path\": \"/kanta:Kiinteisto/kanta:alkuPvm\",\n" +
                "    \"label\": \"LuontiPvm\",\n" +
                "    \"type\": \"String\"\n" +
                "  }, {\n" +
                "    \"path\": \"/kanta:Kiinteisto/kanta:luontitapa\",\n" +
                "    \"label\": \"Luontitapa\",\n" +
                "    \"type\": \"String\"\n" +
                "  },  {\n" +
                "    \"path\": \"/kanta:Kiinteisto/kanta:sijaintiepavarmuus\",\n" +
                "    \"label\": \"Sijaintiepävarmuus\",\n" +
                "    \"type\": \"String\"\n" +
                "  },  {\n" +
                "    \"path\": \"/kanta:Kiinteisto/kanta:sijainnit/kanta:Sijainti/kanta:alue\",\n" +
                "    \"label\": \"geom\",\n" +
                "    \"type\": \"Geometry\"\n" +
                "  }],\n" +
                "  \"root\": {\n" +
                "    \"rootNS\": \"http://www.paikkatietopalvelu.fi/gml/kantakartta\",\n" +
                "    \"name\": \"Kiinteisto\"\n" +
                "  },\n" +
                "  \"scan\": {\n" +
                "    \"scanNS\": \"http://www.opengis.net/gml\",\n" +
                "    \"name\": \"featureMember\"\n" +
                "  }\n" +
                "}";

        JSONObject conf = JSONHelper.createJSONObject(testConf);


        try {
            inputProcessor.setInput(inp);


            OutputStream fouts = WFSTestHelper.getTestOutputStream();
            try {
                outputProcessor.setOutput(fouts);

                ParserRecipe recipe = new RYSP_wfs_Parser();
                WFS11_path_parse_worker worker = new WFS11_path_parse_worker(conf);
                recipe.setWFS11ParseWorker(worker);

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
