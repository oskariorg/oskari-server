package fi.nls.oskari.csw.helper;

import fi.nls.oskari.csw.domain.CSWIsoRecord;
import fi.nls.test.util.ResourceHelper;
import fi.nls.oskari.util.JSONHelper;
import org.oskari.xml.XmlHelper;
import org.geotools.referencing.CRS;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.api.referencing.operation.MathTransform;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import java.io.InputStream;
import java.util.Locale;

public class CSWISORecordParserTest {

    private String TARGET_CRS = "EPSG:4326";
    private String SOURCE_CRS = "EPSG:3067";
    private String CSW_INPUT_FILE_NAME = "/fi/nls/oskari/csw/helper/csw.xml";
    private String CSW_OTHER_CONSTRAINTS_INPUT_FILE_NAME = "/fi/nls/oskari/csw/helper/csw_otherConstraints.xml";
    private String METADATA_ID = "MD_Metadata";

    private MathTransform getMathTransform() {
        MathTransform transform = null;
        try {
            CoordinateReferenceSystem targetCRS = CRS.decode(TARGET_CRS, true);
            CoordinateReferenceSystem sourceCRS = CRS.decode(SOURCE_CRS);
            transform = CRS.findMathTransform(sourceCRS, targetCRS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return transform;
    }

    private Node getMetadataNode(String file) throws Exception {
        InputStream in = getClass().getResourceAsStream(file);
        Element ret = XmlHelper.parseXML(in, true);
        return XmlHelper.getFirstChild(ret, METADATA_ID);
    }

    @Test
    public void TestDateParsing() throws Exception {
        Node metaDataNode = getMetadataNode(CSW_INPUT_FILE_NAME);
        JSONObject expected = ResourceHelper.readJSONResource("/fi/nls/oskari/csw/helper/csw-response.json", this.getClass());
        MathTransform transform = getMathTransform();
        Locale locale = new Locale("EN");

        CSWISORecordParser parser;
        try {
            parser = new CSWISORecordParser();
            CSWIsoRecord metadata = parser.parse(metaDataNode, locale, transform);
            JSONObject json = metadata.toJSON();
            Assertions.assertTrue(JSONHelper.isEqual(expected, json), "JSON matches expected");
            //System.out.println(json.toString(2));
            Assertions.assertEquals("2017-04-21T11:24Z", json.get("metadataDateStamp"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testConstraintParsing() throws Exception {
        Node metaDataNode = getMetadataNode(CSW_OTHER_CONSTRAINTS_INPUT_FILE_NAME);
        JSONObject expected = ResourceHelper.readJSONResource("/fi/nls/oskari/csw/helper/csw_otherConstraints-response.json", this.getClass());
        MathTransform transform = getMathTransform();
        Locale locale = new Locale("EN");

        CSWISORecordParser parser;
        try {
            parser = new CSWISORecordParser();
            CSWIsoRecord metadata = parser.parse(metaDataNode, locale, transform);
            JSONObject json = metadata.toJSON();
            Assertions.assertTrue(JSONHelper.isEqual(expected, json), "JSON matches expected");
            //System.out.println(json.toString(2));
            // Assertions.assertEquals("2017-04-21T11:24Z", json.get("metadataDateStamp"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}