package fi.nls.oskari.eu.elf.addresses;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.stream.XMLStreamException;

import fi.nls.oskari.eu.elf.recipe.universal.ELF_path_parse_worker;
import fi.nls.oskari.eu.elf.recipe.universal.ELF_wfs_Parser;
import fi.nls.oskari.util.JSONHelper;
import org.json.JSONObject;
import org.junit.Test;

import fi.nls.oskari.eu.elf.recipe.addresses.ELF_MasterLoD0_Address_nls_fi_wfs_Parser;
import fi.nls.oskari.fe.engine.BasicFeatureEngine;
import fi.nls.oskari.fe.input.XMLInputProcessor;
import fi.nls.oskari.fe.input.format.gml.StaxGMLInputProcessor;
import fi.nls.oskari.fe.input.format.gml.recipe.ParserRecipe;
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
    //@Ignore("Not ready")
    @Test
    public void test_ELF_Master_LoD0_Address_ign_fr_wfs_GMLtoJSON()
            throws InstantiationException, IllegalAccessException, IOException,
            XMLStreamException {

        BasicFeatureEngine engine = new BasicFeatureEngine();

        XMLInputProcessor inputProcessor = new StaxGMLInputProcessor();

        OutputStreamProcessor outputProcessor = new JsonOutputProcessor();

        InputStream inp = getClass().getResourceAsStream(
                "/fi/nls/oskari/eu/elf/addresses/ign_fr-ELF-AD-wfs.xml");

        try {
            inputProcessor.setInput(inp);

            OutputStream fouts = System.out;
            try {
                outputProcessor.setOutput(fouts);

                ParserRecipe recipe = new ELF_MasterLoD0_Address_nls_fi_wfs_Parser();

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
    public void test_ELF_Master_LoD0_Address_ign_fi_local_href_wfs_GMLtoJSON()
            throws InstantiationException, IllegalAccessException, IOException,
            XMLStreamException {

        BasicFeatureEngine engine = new BasicFeatureEngine();

        XMLInputProcessor inputProcessor = new StaxGMLInputProcessor();

        OutputStreamProcessor outputProcessor = new JsonOutputProcessor();

        InputStream inp = getClass().getResourceAsStream(
                "/fi/nls/oskari/eu/elf/addresses/ign_fi_local-href-ELF-AD-wfs.xml");

        String testConf =" {\"scan\":{\"scanNS\":\"http://www.opengis.net/wfs/2.0\",\"name\":\"member\"}," +
                "\"root\":{\"rootNS\":\"http://www.locationframework.eu/schemas/Addresses/MasterLoD0/1.0\",\"name\":\"Address\"}," +
                "\"paths\":[" +
                "{\"path\":\"/elf-lod0ad:Address/@gml:id\",\"type\":\"String\",\"label\":\"id\"}," +
                "{\"path\":\"/elf-lod0ad:Address/ad:inspireId/base:Identifier/base:localId\",\"type\":\"String\",\"label\":\"InspireLocalId\"}," +
                "{\"path\":\"/elf-lod0ad:Address/ad:inspireId/base:Identifier/base:versionId\",\"type\":\"String\",\"label\":\"InspireVersionId\"}," +
                "{\"path\":\"/elf-lod0ad:Address/ad:position/ad:GeographicPosition/ad:geometry\",\"type\":\"Geometry\",\"label\":\"geom\"}," +
                "{\"path\":\"/elf-lod0ad:Address/ad:locator/ad:AddressLocator/ad:designator/ad:LocatorDesignator\",\"type\":\"Object\",\"label\":\"addressLocatorDesignators\"}," +
                "{\"path\":\"/elf-lod0ad:Address/ad:validFrom\",\"type\":\"String\",\"label\":\"validFrom\"}," +
                "{\"path\":\"/elf-lod0ad:Address/ad:validTo\",\"type\":\"String\",\"label\":\"validTo\"}," +
                "{\"path\":\"/elf-lod0ad:Address/ad:beginLifespanVersion\",\"type\":\"String\",\"label\":\"beginLifespanVersion\"}," +
                "{\"path\":\"/elf-lod0ad:Address/ad:endLifespanVersion\",\"type\":\"String\",\"label\":\"endLifespanVersion\"}," +
                "{\"path\":\"/elf-lod0ad:Address/ad:component/@xlink:href\",\"type\":\"Href\",\"label\":\"components\",\"hrefPath\":[" +
                "{\"path\":\"/wfs:SimpleFeatureCollection/wfs:member/elf-lod0ad:AdminUnitName/@gml:id\",\"type\":\"String\",\"label\":\"id\"}," +
                "{\"path\":\"/wfs:SimpleFeatureCollection/wfs:member/elf-lod0ad:AdminUnitName/ad:inspireId/base:Identifier/base:namespace\",\"type\":\"String\",\"label\":\"type\"},"  +
                "{\"path\":\"/wfs:SimpleFeatureCollection/wfs:member/elf-lod0ad:AdminUnitName/ad:name/gn:GeographicalName/gn:language\",\"type\":\"String\",\"label\":\"language\"},"  +
                "{\"path\":\"/wfs:SimpleFeatureCollection/wfs:member/elf-lod0ad:AdminUnitName/ad:name/gn:GeographicalName/gn:spelling/gn:SpellingOfName/gn:text\",\"type\":\"String\",\"label\":\"name\"},"  +
                "{\"path\":\"/wfs:SimpleFeatureCollection/wfs:member/elf-lod0ad:ThoroughfareName/@gml:id\",\"type\":\"String\",\"label\":\"id\"}," +
                "{\"path\":\"/wfs:SimpleFeatureCollection/wfs:member/elf-lod0ad:ThoroughfareName/ad:inspireId/base:Identifier/base:namespace\",\"type\":\"String\",\"label\":\"type\"},"  +
                "{\"path\":\"/wfs:SimpleFeatureCollection/wfs:member/elf-lod0ad:ThoroughfareName/ad:name/gn:GeographicalName/gn:language\",\"type\":\"String\",\"label\":\"language\"},"  +
                "{\"path\":\"/wfs:SimpleFeatureCollection/wfs:member/elf-lod0ad:ThoroughfareName/ad:name/gn:GeographicalName/gn:spelling/gn:SpellingOfName/gn:text\",\"type\":\"String\",\"label\":\"name\"}"  +
                "]}" +
                "]}";

        JSONObject conf = JSONHelper.createJSONObject(testConf);


        try {
            inputProcessor.setInput(inp);


            OutputStream fouts = System.out;
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
