package fi.nls.oskari.utils;

import fi.nls.oskari.wfs.util.XMLHelper;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.eclipse.xsd.XSDSchema;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class XMLHelperTest {

	String xml = "<test><res>lol</res></test>";
	String xml2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><test><res>lol</res></test>";
	
	@Test
	public void testBuilder() {
        StAXOMBuilder staxOMBuilder = XMLHelper.createBuilder(xml);
        OMElement elements = staxOMBuilder.getDocumentElement();
    	assertTrue("Should get same xml as elements", elements.toString().equals(xml));
        
    	XSDSchema schema = XMLHelper.StringToXSDSchema(xml2);
    	String result = XMLHelper.XSDSchemaToString(schema);
    	assertTrue("Should get same xml as result", result.equals(xml2));
	}
	
	@Test
	public void testConversion() {
    	XSDSchema schema = XMLHelper.StringToXSDSchema(xml2);
    	String result = XMLHelper.XSDSchemaToString(schema);
    	assertTrue("Should get same xml as result", result.equals(xml2));
	}
}
