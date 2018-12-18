package fi.nls.oskari.wfs;

import fi.nls.oskari.pojo.SessionStore;
import fi.nls.oskari.pojo.SessionStoreTest;
import fi.nls.oskari.service.ServiceRuntimeException;
import fi.nls.oskari.transport.TransportService;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.test.util.TestHelper;
import fi.nls.oskari.wfs.extension.AnalysisFilter;
import fi.nls.oskari.wfs.pojo.WFSLayerStore;
import fi.nls.oskari.wfs.util.HttpHelper;
import fi.nls.oskari.work.JobType;
import fi.nls.test.util.ResourceHelper;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.geotools.feature.FeatureCollection;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

public class WFSCommunicatorTest {
	private static SessionStore session;
	private static WFSLayerStore layer;
    private static JobType type;
	private static List<Double> bounds;
	
	String sessionResult = ResourceHelper.readStringResource("WFSCommunicatorTest-result-session.xml", this);
    String boundsResult = ResourceHelper.readStringResource("WFSCommunicatorTest-result-bounds.xml", this);

    @BeforeClass
    public static void setUp() {
        // use relaxed comparison settings
        XMLUnit.setIgnoreComments(true);
        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setIgnoreDiffBetweenTextAndCDATA(true);
        XMLUnit.setIgnoreAttributeOrder(true);

        assumeTrue(TestHelper.redisAvailable());

        Properties properties = new Properties();
        try {
            properties.load(TransportService.class.getResourceAsStream("/transport.properties"));
            PropertyUtil.addProperties(properties, true);
        } catch (Exception e) {
            System.err.println("Configuration could not be loaded");
            e.printStackTrace();
        }

		try {
            String sessionJSON = IOHelper.readString(SessionStoreTest.class.getResourceAsStream("sessionstore-valid.json"));
			session = SessionStore.setJSON(sessionJSON);
            String layerJSON = IOHelper.readString(WFSCommunicatorTest.class.getResourceAsStream("WFSCommunicatorTest-layer.json"));

			layer = WFSLayerStore.setJSON(layerJSON);
            type = JobType.NORMAL;
		} catch (IOException e) {
			fail("Should not throw exception");
		}
		
		bounds = new ArrayList<>();
		bounds.add(385800.0);
		bounds.add(6690267.0);
		bounds.add(397380.0);
		bounds.add(6697397.0);
    }
    
	@Test
	public void testLocation() throws Exception {
		String payload = WFSCommunicator.createRequestPayload(type, layer, session, null, null);

        Diff xmlDiff = new Diff(sessionResult, payload);
        assertTrue("Should get expected location result " + xmlDiff, xmlDiff.similar());
	}
	
	@Test
    @Ignore("Service specified in layer json no longer exists")
	public void testBounds() throws Exception {
        // check that we have http connectivity (correct proxy settings etc)
        assumeTrue(TestHelper.canDoHttp());
        assumeTrue(TestHelper.redisAvailable());

		String payload = WFSCommunicator.createRequestPayload(type, layer, session, bounds, null);
        Diff xmlDiff = new Diff(boundsResult, payload);
        assertTrue("Should get expected bounds result " + xmlDiff, xmlDiff.similar());
		
		// request (maplayer_id 216)
        Reader response = HttpHelper.postRequestReader(layer.getURL(), "text/xml", payload, layer.getUsername(), layer.getPassword());
		assertTrue("Should get valid response", response != null);
		
		// parse
		FeatureCollection<SimpleFeatureType, SimpleFeature> features = WFSCommunicator.parseSimpleFeatures(response, layer);
		assertTrue("Should get valid features", features != null);
		assertTrue("Should get features", features.size() > 0);
	}

    @Test
    public void testUserlayerParsing() throws Exception {
        // get userlayer config
        String layerJSON = IOHelper.readString(getClass().getResourceAsStream("Userlayer.json"));
        WFSLayerStore userlayer = WFSLayerStore.setJSON(layerJSON);

        // get response xml
        Reader response = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("Userlayer-GetFeature-response.xml")));
        assertTrue("Should get valid response", response != null);

        // parse the xml
        FeatureCollection<SimpleFeatureType, SimpleFeature> features = WFSCommunicator.parseSimpleFeatures(response, userlayer);
        assertTrue("Should get valid features", features != null);
        assertEquals("Should parse 177 features", 177, features.size());
    }

    @Test
    public void testFilterConstruct() {
        String layerId = "216";
        WFSFilter filter = WFSCommunicator.constructFilter(layerId);
        assertTrue("Should be instance of default", (filter instanceof WFSFilter));

        layerId = "analysis_216_710";
        filter = WFSCommunicator.constructFilter(layerId);
        assertTrue("Should be instance of analysis", (filter instanceof AnalysisFilter));
    }

    @Test(expected = ServiceRuntimeException.class)
    public void testFailingFilterConstruct() throws NullPointerException {
        String layerId = "nonexistent_216";
        WFSFilter filter = WFSCommunicator.constructFilter(layerId);
        assertTrue("Should be null", filter == null);
    }
}
