package fi.nls.oskari.csw.helper;

import fi.nls.oskari.csw.domain.CSWIsoRecord;
import org.oskari.xml.XmlHelper;
import org.geotools.referencing.CRS;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.Locale;

public class CSWISORecordParserTest {

    private String TARGET_CRS = "EPSG:4326";
    private String SOURCE_CRS = "EPSG:3067";
    private String CSW_INPUT_FILE_NAME = "/fi/nls/oskari/csw/helper/csw.xml";
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

    private Node getMetadataNode() throws Exception {
        InputStream in = getClass().getResourceAsStream(CSW_INPUT_FILE_NAME);
        Element ret = XmlHelper.parseXML(in, true);
        return XmlHelper.getFirstChild(ret, METADATA_ID);
    }

    @Test
    public void TestDateParsing() throws Exception {
        Node metaDataNode = getMetadataNode();
        MathTransform transform = getMathTransform();
        Locale locale = new Locale("EN");

        CSWISORecordParser parser;
        try {
            parser = new CSWISORecordParser();
            CSWIsoRecord metadata = parser.parse(metaDataNode, locale, transform);
            JSONObject json = metadata.toJSON();
            Assertions.assertEquals("2017-04-21T11:24Z", json.get("metadataDateStamp"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}