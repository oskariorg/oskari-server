package fi.nls.oskari.utils;

import fi.nls.oskari.wfs.util.HttpHelper;
import fi.nls.test.util.TestHelper;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.BufferedInputStream;
import java.io.Reader;

import static org.junit.Assert.assertTrue;


public class HttpHelperTest {

    @BeforeClass
    public static void beforeTest() {
        org.junit.Assume.assumeTrue(TestHelper.canDoHttp());
    }

	@Test
	public void testGetRequest() {
		String response = HttpHelper.getRequest("http://httpbin.org/ip", null);
		assertTrue("Should get response", response != null);
	}

	@Test
	public void testGetRequestFail() {
		String response = HttpHelper.getRequest("http://httpbin.org/asda", null);
		assertTrue("Shouldn't get response", response == null);
	}
	
	@Test
	public void testBufferGetRequest() {
		BufferedInputStream responseBuffer = HttpHelper.getRequestStream("http://httpbin.org/ip", "text/html", null, null);
		assertTrue("Should get buffer", responseBuffer != null);
	}
	
	@Test
	public void testBufferGetRequestFail() {
		BufferedInputStream responseBuffer = HttpHelper.getRequestStream("http://httpbin.org/asda", "text/html", null, null);
		assertTrue("Shouldn't get buffer", responseBuffer == null);
	}
	
	@Test
	public void testReaderGetRequest() {
	    Reader responseReader = HttpHelper.getRequestReader("http://httpbin.org/ip", "text/html", null, null);
		assertTrue("Should get buffer", responseReader != null);
	}
	
	@Test
	public void testReaderGetRequestFail() {
	    Reader responseReader = HttpHelper.getRequestReader("http://httpbin.org/asda", "text/html", null, null);
		assertTrue("Shouldn't get buffer", responseReader == null);
	}
	
	@Test
	public void testPostRequestSuccess() {
	    Reader responseReader = HttpHelper.postRequestReader("http://httpbin.org/post", "text/html", "test", null, null);
		assertTrue("Should get buffer", responseReader != null);
	}
	
	@Test
	public void testPostRequestFail() {
	    Reader responseReader = HttpHelper.postRequestReader("http://httpbin.org/asda", "text/html", "test", null, null);
		assertTrue("Shouldn't get buffer", responseReader == null);
	}
}
