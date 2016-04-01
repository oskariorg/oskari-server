package fi.nls.oskari.wfs;

import fi.nls.oskari.cache.JedisManager;
import fi.nls.test.util.TestHelper;
import org.eclipse.xsd.XSDSchema;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

public class CachingSchemaLocatorTest {
	private static CachingSchemaLocator locator;
	
    @BeforeClass
    public static void setUp() {

        assumeTrue(TestHelper.redisAvailable());
		JedisManager.connect(10, "localhost", 6379);
		locator = new CachingSchemaLocator("", "");
    }
    
	@Test
	public void testHttp() {
        assumeTrue(TestHelper.redisAvailable());
		// TODO: find http url
		String namespaceURI = ""; // http
		String schemaLocation = "";
		XSDSchema schema = locator.locateSchema(null, namespaceURI, schemaLocation, null);
		//assertTrue("Should get valid schema", schema != null);
	}
	
	@Test
	public void testHttps() {
        assumeTrue(TestHelper.redisAvailable());
		// TODO: https
		String namespaceURI = ""; // https
		String schemaLocation = "";
		XSDSchema schema = locator.locateSchema(null, namespaceURI, schemaLocation, null);
		//assertTrue("Should get valid schema", schema != null);
	}
	
	@Test
	public void test() {
        assumeTrue(TestHelper.redisAvailable());
		// broken
		String namespaceURI = ""; 
		String schemaLocation = "";
		XSDSchema schema = locator.locateSchema(null, namespaceURI, schemaLocation, null);
		assertTrue("Should get invalid schema", schema == null);
	}

}
