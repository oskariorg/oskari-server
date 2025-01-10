package fi.nls.oskari.util;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import org.apache.axiom.om.OMElement;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created with IntelliJ IDEA.
 * User: SMAKINEN
 * Date: 21.5.2014
 * Time: 9:43
 * To change this template use File | Settings | File Templates.
 */
public class XmlHelperTest {
    private static final Logger log = LogFactory.getLogger(XmlHelperTest.class);

    @Test
    public void testXMLParsing() throws Exception {
        OMElement elem = XmlHelper.parseXML("<test><case name=\"my test\">testing</case></test>");
        Assertions.assertNotNull(elem, "Should have element");

        OMElement caseElem = XmlHelper.getChild(elem, "case");
        Assertions.assertNotNull(caseElem, "Should have case element");

        final Map<String, String> attrs = XmlHelper.getAttributesAsMap(caseElem);
        Assertions.assertEquals(1, attrs.size(), "Should have one attribute");
        Assertions.assertEquals("name", attrs.keySet().iterator().next(), "Should have attribute with name 'name'");

        Assertions.assertEquals("my test", XmlHelper.getAttributeValue(caseElem, "name"), "Name attribute should have correct value");
    }
}
