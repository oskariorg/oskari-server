package fi.nls.oskari.csw.helper;

import fi.nls.oskari.csw.domain.CSWIsoRecord;
import fi.nls.test.util.ResourceHelper;
import fi.nls.oskari.util.JSONHelper;
import org.oskari.xml.XmlHelper;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Element;
import java.io.InputStream;
import java.util.Locale;

public class CSWISORecordParserTest {

    private String CSW_INPUT_FILE_NAME = "/fi/nls/oskari/csw/helper/csw.xml";
    private String CSW_OTHER_CONSTRAINTS_INPUT_FILE_NAME = "/fi/nls/oskari/csw/helper/csw_otherConstraints.xml";
    private String METADATA_ID = "MD_Metadata";

    private Element getMetadataNode(String file) throws Exception {
        InputStream in = getClass().getResourceAsStream(file);
        Element ret = XmlHelper.parseXML(in, true);
        return XmlHelper.getFirstChild(ret, METADATA_ID);
    }

    @Test
    public void TestDateParsing() throws Exception {
        Element metaDataNode = getMetadataNode(CSW_INPUT_FILE_NAME);
        Locale locale = new Locale("EN");
        CSWIsoRecord metadata = CSWISORecordParser.parse(metaDataNode, locale);
        JSONObject json = metadata.toJSON();
        JSONObject expected = ResourceHelper.readJSONResource("/fi/nls/oskari/csw/helper/csw-response.json", this.getClass());
        Assertions.assertTrue(JSONHelper.isEqual(expected, json), "JSON matches expected");
        Assertions.assertEquals("2017-04-21T11:24Z", json.get("metadataDateStamp"));
    }

    @Test
    public void testConstraintParsing() throws Exception {
        Element metaDataNode = getMetadataNode(CSW_OTHER_CONSTRAINTS_INPUT_FILE_NAME);
        Locale locale = new Locale("EN");
        CSWIsoRecord metadata = CSWISORecordParser.parse(metaDataNode, locale);
        JSONObject json = metadata.toJSON();
        JSONObject expected = ResourceHelper.readJSONResource("/fi/nls/oskari/csw/helper/csw_otherConstraints-response.json", this.getClass());
        Assertions.assertTrue(JSONHelper.isEqual(expected, json), "JSON matches expected");
    }
}