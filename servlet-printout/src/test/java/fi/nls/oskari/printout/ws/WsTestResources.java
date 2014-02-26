package fi.nls.oskari.printout.ws;

import java.net.URI;
import java.net.URISyntaxException;

public class WsTestResources {

	static int port = 2374;

	public static URI getTestResource(String uri) throws URISyntaxException {
		return new URI("http", null, "localhost", port,

		"/oskari-printout-backend-1.0.3-SNAPSHOT" + uri, null, null);
	}
}
