package fi.nls.oskari.utils;

import static org.junit.Assert.*;

import java.io.BufferedInputStream;
import java.io.BufferedReader;

import org.junit.Test;

public class HttpHelperTest {

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
		BufferedReader responseReader = HttpHelper.getRequestReader("http://httpbin.org/ip", "text/html", null, null);
		assertTrue("Should get buffer", responseReader != null);
	}
	
	@Test
	public void testReaderGetRequestFail() {
		BufferedReader responseReader = HttpHelper.getRequestReader("http://httpbin.org/asda", "text/html", null, null);
		assertTrue("Shouldn't get buffer", responseReader == null);
	}
	
	@Test
	public void testPostRequestSuccess() {
		BufferedReader responseReader = HttpHelper.postRequestReader("http://httpbin.org/post", "text/html", "test", null, null);
		assertTrue("Should get buffer", responseReader != null);
	}
	
	@Test
	public void testPostRequestFail() {
		BufferedReader responseReader = HttpHelper.postRequestReader("http://httpbin.org/asda", "text/html", "test", null, null);
		assertTrue("Shouldn't get buffer", responseReader == null);
	}
}
