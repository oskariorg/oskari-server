package fi.nls.oskari.csw.service;

import fi.nls.oskari.csw.domain.CSWIsoRecord;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.JSONHelper;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import java.io.InputStream;

public class CSWServiceTest {

    // Parsing with xpaths don't work with the "future" parser so we can't use it yet
    // but this is the usage of revised version of XMLHelper
    private Node getMetadataElementFuture(String file) throws Exception {
        InputStream in = getClass().getResourceAsStream(file);
        Element ret = org.oskari.xml.XmlHelper.parseXML(in, true);
        Element metadata = org.oskari.xml.XmlHelper.getFirstChild(ret, "MD_Metadata");
        return metadata;
    }

    private Node getMetadataElementCurrent(String file) throws Exception {
        InputStream in = getClass().getResourceAsStream(file);

        DocumentBuilderFactory dbf = fi.nls.oskari.util.XmlHelper.newDocumentBuilderFactory();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(in);
        Node root = doc.getDocumentElement();

        NodeList children = root.getChildNodes();
        Node metadata = null;
        for (int i = 0; i < children.getLength(); i++) {
            if ("MD_Metadata".equals(children.item(i).getLocalName())) {
                metadata = children.item(i);
            }
        }
        return metadata;
    }

    @Test
    public void mapIsoRecordElementToObject() throws Exception {
        String testfile = "CSWService-Metadata";
        Node metadata = getMetadataElementCurrent(testfile + ".xml");
        CSWService service = new CSWService("http://for.testing.org");
        CSWIsoRecord rec = service.mapIsoRecordElementToObject(metadata, "fi");
        JSONObject actual = rec.toJSON();
        JSONObject expected = new JSONObject(IOHelper.readString(getClass().getResourceAsStream(testfile + "-expected.json")));
        Assertions.assertTrue(JSONHelper.isEqual(actual, expected), "JSON should match");
    }

    @Test
    public void mapIsoRecordElementToObjectMultiLang() throws Exception {
        String testfile = "CSWService-Metadata-multilang";
        Node metadata = getMetadataElementCurrent(testfile + ".xml");
        CSWService service = new CSWService("http://for.testing.org");
        CSWIsoRecord rec = service.mapIsoRecordElementToObject(metadata, "fi");
        JSONObject actual = rec.toJSON();
        JSONObject expected = new JSONObject(IOHelper.readString(getClass().getResourceAsStream(testfile + "-expected.json")));
        Assertions.assertTrue(JSONHelper.isEqual(actual, expected), "JSON should match");
    }
}