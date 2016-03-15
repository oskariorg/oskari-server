package fi.nls.oskari.printout.ws.jaxrs.resource;

import fi.nls.oskari.printout.ws.WsTestResources;
import org.apache.commons.collections.map.MultiKeyMap;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class PostJsonTest {

	private static final MultiKeyMap uris = new MultiKeyMap();

	@BeforeClass
	public static void setupUris() throws URISyntaxException {
		uris.put(
				PostJsonTestFileType.JSON,
				PostJsonTestFileType.PNG,
				WsTestResources
						.getTestResource("/imaging/service/thumbnail/maplinkjson.png"));

		uris.put(
				PostJsonTestFileType.GEOJSON,
				PostJsonTestFileType.PNG,
				WsTestResources
						.getTestResource("/imaging/service/thumbnail/maplinkgeojson.png"));

		uris.put(
				PostJsonTestFileType.JSON,
				PostJsonTestFileType.PDF,
				WsTestResources
						.getTestResource("/imaging/service/thumbnail/maplinkjson.pdf"));

		uris.put(
				PostJsonTestFileType.GEOJSON,
				PostJsonTestFileType.PDF,
				WsTestResources
						.getTestResource("/imaging/service/thumbnail/maplinkgeojson.pdf"));

	}

	PostJsonTestRunner runner = new PostJsonTestRunner();

	@Test
	public void testPostActionRouteJSONForPDF() throws IOException {
		String testname = "action_route_liferay";

		PostJsonTestFileType input = PostJsonTestFileType.JSON;
		PostJsonTestFileType output = PostJsonTestFileType.PDF;

		runner.run(testname, (URI) uris.get(input, output), output);
	}

	@Test
	public void testPostActionRouteJSONForPNG() throws IOException {

		String testname = "action_route_liferay";

		PostJsonTestFileType input = PostJsonTestFileType.JSON;
		PostJsonTestFileType output = PostJsonTestFileType.PNG;

		runner.run(testname, (URI) uris.get(input, output), output);
	}

	@Test
	public void testPostGeojsPrintTest20130423JSONForPDF() throws IOException {
		String testname = "geojsPrintTest20130423";

		PostJsonTestFileType input = PostJsonTestFileType.GEOJSON;
		PostJsonTestFileType output = PostJsonTestFileType.PDF;

		runner.run(testname, (URI) uris.get(input, output), output);

	}

	@Test
	public void testPostGeojsPrintTest20130423JSONForPNG() throws IOException {
		String testname = "geojsPrintTest20130423";

		PostJsonTestFileType input = PostJsonTestFileType.GEOJSON;
		PostJsonTestFileType output = PostJsonTestFileType.PNG;

		runner.run(testname, (URI) uris.get(input, output), output);

	}

	@Test
	public void testPostGeojsPrintTestJSONForPDF() throws IOException {
		String testname = "geojsPrintTest";

		PostJsonTestFileType input = PostJsonTestFileType.GEOJSON;
		PostJsonTestFileType output = PostJsonTestFileType.PDF;

		runner.run(testname, (URI) uris.get(input, output), output);
	}

	@Test
	public void testPostGeojsPrintTestJSONForPNG() throws IOException {
		String testname = "geojsPrintTest";

		PostJsonTestFileType input = PostJsonTestFileType.GEOJSON;
		PostJsonTestFileType output = PostJsonTestFileType.PNG;

		runner.run(testname, (URI) uris.get(input, output), output);

	}

	@Test
	public void testPostGeojsPrintTestWithTilesJSONForPNG() throws IOException {
		String testname = "geojsPrintTestWithTiles";

		PostJsonTestFileType input = PostJsonTestFileType.GEOJSON;
		PostJsonTestFileType output = PostJsonTestFileType.PNG;

		runner.run(testname, (URI) uris.get(input, output), output);

	}

	@Test
	public void testPostLiferayActionRouteJSONForPNG() throws IOException {
		String testname = "action_route_liferay";

		PostJsonTestFileType input = PostJsonTestFileType.JSON;
		PostJsonTestFileType output = PostJsonTestFileType.PNG;

		runner.run(testname, (URI) uris.get(input, output), output);
	}

	@Test
	public void testPostLiferayActionRouteJSONForPNGToGeoJSONEndpoint()
			throws IOException {
		String testname = "action_route_liferay";

		PostJsonTestFileType input = PostJsonTestFileType.GEOJSON;
		PostJsonTestFileType output = PostJsonTestFileType.PNG;

		runner.run(testname, (URI) uris.get(input, output), output);

	}

	@Test
	public void testPostLiferayActionRouteTestJSONForPNG() throws IOException {
		String testname = "action_route_test";

		PostJsonTestFileType input = PostJsonTestFileType.JSON;
		PostJsonTestFileType output = PostJsonTestFileType.PNG;

		runner.run(testname, (URI) uris.get(input, output), output);

	}

	@Test
	public void testPostStatsPrintTestJSONForPDF() throws IOException {
		String testname = "testStatLayerPrint";

		PostJsonTestFileType input = PostJsonTestFileType.GEOJSON;
		PostJsonTestFileType output = PostJsonTestFileType.PDF;

		runner.run(testname, (URI) uris.get(input, output), output);

	}

	@Test
	public void testPostStatsPrintTestJSONForPNG() throws IOException {
		String testname = "testStatLayerPrint";

		PostJsonTestFileType input = PostJsonTestFileType.GEOJSON;
		PostJsonTestFileType output = PostJsonTestFileType.PNG;

		runner.run(testname, (URI) uris.get(input, output), output);

	}

}
