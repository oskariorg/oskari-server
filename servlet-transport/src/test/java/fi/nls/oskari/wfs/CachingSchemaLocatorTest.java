package fi.nls.oskari.wfs;

import static org.junit.Assert.*;

import fi.nls.oskari.cache.JedisManager;
import org.eclipse.xsd.XSDSchema;
import org.junit.BeforeClass;
import org.junit.Test;

public class CachingSchemaLocatorTest {
	private static CachingSchemaLocator locator;
	
    @BeforeClass
    public static void setUp() {
		JedisManager.connect(10, "localhost", 6379);
		locator = new CachingSchemaLocator("", "");
    }
    
	@Test
	public void testHttp() {
		// TODO: find http url
		String namespaceURI = ""; // http
		String schemaLocation = "";
		XSDSchema schema = locator.locateSchema(null, namespaceURI, schemaLocation, null);
		//assertTrue("Should get valid schema", schema != null);
	}
	
	@Test
	public void testHttps() {
		// TODO: https
		String namespaceURI = ""; // https
		String schemaLocation = "";
		XSDSchema schema = locator.locateSchema(null, namespaceURI, schemaLocation, null);
		//assertTrue("Should get valid schema", schema != null);
	}
	
	@Test
	public void test() {
		// broken
		String namespaceURI = ""; 
		String schemaLocation = "";
		XSDSchema schema = locator.locateSchema(null, namespaceURI, schemaLocation, null);
		assertTrue("Should get invalid schema", schema == null);
	}

}
