package fi.nls.oskari.util;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import org.apache.axiom.om.OMElement;
import org.junit.Test;

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
        assertNotNull("Should have element", elem);

        OMElement caseElem = XmlHelper.getChild(elem, "case");
        assertNotNull("Should have case element", caseElem);

        final Map<String, String> attrs = XmlHelper.getAttributesAsMap(caseElem);
        assertEquals("Should have one attribute", 1, attrs.size());
        assertEquals("Should have attribute with name 'name'", "name", attrs.keySet().iterator().next());

        assertEquals("Name attribute should have correct value", "my test", XmlHelper.getAttributeValue(caseElem, "name"));
    }
}
