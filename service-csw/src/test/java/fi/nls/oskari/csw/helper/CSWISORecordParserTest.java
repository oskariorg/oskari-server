package fi.nls.oskari.csw.helper;

import org.geotools.referencing.CRS;
import org.junit.Test;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.w3c.dom.Document;
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

    private Node getMetadataNode() {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        InputStream xmlInputStream = getClass().getResourceAsStream(CSW_INPUT_FILE_NAME);

        NodeList children = null;
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(xmlInputStream);
            Node root = doc.getDocumentElement();
            children = root.getChildNodes();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Node metaDataNode = null;
        for (int i = 0; i < children.getLength(); i++) {
            if (METADATA_ID.equals(children.item(i).getLocalName())) {
                metaDataNode = children.item(i);
            }
        }
        return metaDataNode;
    }

    @Test
    public void TestDateParsing() {
        Node metaDataNode = getMetadataNode();
        MathTransform transform = getMathTransform();
        Locale locale = new Locale("EN");

        CSWISORecordParser parser;
        try {
            parser = new CSWISORecordParser();
            parser.parse(metaDataNode, locale, transform);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}