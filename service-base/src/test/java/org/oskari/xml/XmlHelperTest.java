package org.oskari.xml;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Element;

import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class XmlHelperTest {
    private static final Logger log = LogFactory.getLogger(XmlHelperTest.class);

    @Test
    public void testXMLParsing() throws Exception {
        Element elem = XmlHelper.parseXML("<test><case name=\"my test\">testing</case></test>");
        Assertions.assertNotNull(elem, "Should have element");

        Element caseElem = XmlHelper.getFirstChild(elem, "case");
        Assertions.assertNotNull(caseElem, "Should have case element");

        final Map<String, String> attrs = XmlHelper.getAttributesAsMap(caseElem);
        Assertions.assertEquals(1, attrs.size(), "Should have one attribute");
        Assertions.assertEquals("name", attrs.keySet().iterator().next(), "Should have attribute with name 'name'");

        Assertions.assertEquals("my test", XmlHelper.getAttributeValue(caseElem, "name"), "Name attribute should have correct value");
    }

    @Test
    public void testXMLParsing2() throws Exception {
        Element elem = XmlHelper.parseXML("<test><case name=\"my test 1\">testing</case><moi>jee</moi><case name=\"my test 2\">testing</case></test>");
        Assertions.assertNotNull(elem, "Should have element");

        String names = XmlHelper.getChildElements(elem, "case").map(caseEl -> {
            final Map<String, String> attrs = XmlHelper.getAttributesAsMap(caseEl);
            return attrs.get("name");
        }).collect(Collectors.joining(","));
        Assertions.assertEquals("my test 1,my test 2", names, "Names match");

        Element caseElem = XmlHelper.getFirstChild(elem, "case");
        Assertions.assertNotNull(caseElem, "Should have case element");

        final Map<String, String> attrs = XmlHelper.getAttributesAsMap(caseElem);
        Assertions.assertEquals(1, attrs.size(), "Should have one attribute");
        Assertions.assertEquals("name", attrs.keySet().iterator().next(), "Should have attribute with name 'name'");

        Assertions.assertEquals("my test 1", XmlHelper.getAttributeValue(caseElem, "name"), "Name attribute should have correct value");
    }
    @Test
    public void testXMLParsing3() throws Exception {
        Element elem = XmlHelper.parseXML("<mahroot:test><dummy:case name=\"my test 1\">testing</dummy:case><moi>jee</moi><dummy:case name=\"my test 2\">testing</dummy:case></mahroot:test>");
        Assertions.assertNotNull(elem, "Should have element");

        String names = XmlHelper.getChildElements(elem, "case").map(caseEl -> {
            final Map<String, String> attrs = XmlHelper.getAttributesAsMap(caseEl);
            return attrs.get("name");
        }).collect(Collectors.joining(","));
        Assertions.assertEquals("my test 1,my test 2", names, "Names match");

        Element caseElem = XmlHelper.getFirstChild(elem, "case");
        Assertions.assertNotNull(caseElem, "Should have case element");

        final Map<String, String> attrs = XmlHelper.getAttributesAsMap(caseElem);
        Assertions.assertEquals(1, attrs.size(), "Should have one attribute");
        Assertions.assertEquals("name", attrs.keySet().iterator().next(), "Should have attribute with name 'name'");

        Assertions.assertEquals("my test 1", XmlHelper.getAttributeValue(caseElem, "name"), "Name attribute should have correct value");
    }

    @Test
    public void testXMLDocTypeParsing() throws Exception {

        Element elem = XmlHelper.parseXML("<!DOCTYPE WMT_MS_Capabilities SYSTEM \"https://fake.address/inspire-wms/schemas/wms/1.1.1/WMS_MS_Capabilities.dtd\">" +
                "<test><case name=\"my test\">testing</case></test>");
        Assertions.assertNotNull(elem, "All good, didn't throw exception");
    }
    @Test
    public void testXMLDocTypeParsing2() throws Exception {
        String xml = "<!DOCTYPE WMT_MS_Capabilities SYSTEM\n" +
                "            \"http://schemas.opengis.net/wms/1.1.0/capabilities_1_1_0.dtd\"[ <!ELEMENT VendorSpecificCapabilities EMPTY>]>" +
                "<test><case name=\"my test\">testing</case></test>";
        Element elem = XmlHelper.parseXML(xml);
        Assertions.assertNotNull(elem, "All good, didn't throw exception");
    }
}
