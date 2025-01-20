package fi.nls.oskari.csw.service;

import fi.nls.oskari.csw.domain.CSWIsoRecord;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.JSONHelper;
import org.oskari.xml.XmlHelper;
import org.json.JSONObject;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.io.InputStream;

import static org.junit.Assert.*;

public class CSWServiceTest {

    private Node getMetadataElement(String file) throws Exception {
        InputStream in = getClass().getResourceAsStream(file);
        Element ret = XmlHelper.parseXML(in, true);
        return XmlHelper.getFirstChild(ret, "MD_Metadata");
    }

    @Test
    public void mapIsoRecordElementToObject() throws Exception {
        String testfile = "CSWService-Metadata";
        Node metadata = getMetadataElement(testfile + ".xml");
        CSWService service = new CSWService("http://for.testing.org");
        CSWIsoRecord rec = service.mapIsoRecordElementToObject(metadata, "fi");
        JSONObject actual = rec.toJSON();
        JSONObject expected = new JSONObject(IOHelper.readString(getClass().getResourceAsStream(testfile + "-expected.json")));
        assertTrue("JSON should match", JSONHelper.isEqual(actual, expected));
    }

    @Test
    public void mapIsoRecordElementToObjectMultiLang() throws Exception {
        String testfile = "CSWService-Metadata-multilang";
        Node metadata = getMetadataElement(testfile + ".xml");
        CSWService service = new CSWService("http://for.testing.org");
        CSWIsoRecord rec = service.mapIsoRecordElementToObject(metadata, "fi");
        JSONObject actual = rec.toJSON();
        JSONObject expected = new JSONObject(IOHelper.readString(getClass().getResourceAsStream(testfile + "-expected.json")));
        assertTrue("JSON should match", JSONHelper.isEqual(actual, expected));
    }
}