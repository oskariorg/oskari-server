package downloadbasket.data;

import org.junit.Test;

import static org.junit.Assert.*;

public class ZipDownloadDetailsTest {

	final String TESTING = "Hello world";

	@Test
	public void testSetFileName() throws Exception {
		ZipDownloadDetails details = new ZipDownloadDetails();
		details.setFileName(TESTING);
		assertEquals(details.getFileName(), TESTING);

	}

	@Test
	public void testSetLayerName() throws Exception {
		ZipDownloadDetails details = new ZipDownloadDetails();
		details.setLayerName(TESTING);
		assertEquals(details.getLayerName(), TESTING);

	}
}