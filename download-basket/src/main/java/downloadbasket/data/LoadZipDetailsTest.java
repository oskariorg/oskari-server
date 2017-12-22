package downloadbasket.data;

import org.junit.Test;

import static org.junit.Assert.*;

public class LoadZipDetailsTest {

	final String TESTING = "hello world";

	@Test
	public void testSetWFSUrl() throws Exception {
		LoadZipDetails details = new LoadZipDetails();
		details.setWFSUrl(TESTING);
		assertEquals(details.getWFSUrl(), TESTING);
	}

	@Test
	public void testSetUserEmail() throws Exception {
		LoadZipDetails details = new LoadZipDetails();
		details.setUserEmail(TESTING);
		assertEquals(details.getUserEmail(), TESTING);
	}

	@Test
	public void getGetFeatureInfoRequest() throws Exception {
		LoadZipDetails details = new LoadZipDetails();
		details.setUserEmail(TESTING);
		assertEquals(details.getUserEmail(), TESTING);
	}

	@Test
	public void setTemporaryDirectory() throws Exception {
		LoadZipDetails details = new LoadZipDetails();
		details.setUserEmail(TESTING);
		assertEquals(details.getUserEmail(), TESTING);
	}
}