package fi.nls.oskari.wfs;

import fi.nls.oskari.cache.JedisManager;
import fi.nls.oskari.pojo.SessionStore;
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
	
	private static String sessionJSON = "{\"client\":\"71k229bstn5ub1fbv1xzwnlnmz\",\"session\":\"49E8CFEF9A310C76438952F8FCD9FF2D\",\"language\":\"fi\",\"browser\":\"mozilla\",\"browserVersion\":20,\"location\":{\"srs\":\"EPSG:3067\",\"bbox\":[509058.0,6858054.0,513578.0,6860174.0],\"zoom\":8},\"grid\":{\"rows\":4,\"columns\":6,\"bounds\":[[508928.0,6859776.0,509952.0,6860800.0],[509952.0,6859776.0,510976.0,6860800.0],[510976.0,6859776.0,512000.0,6860800.0],[512000.0,6859776.0,513024.0,6860800.0],[513024.0,6859776.0,514048.0,6860800.0],[514048.0,6859776.0,515072.0,6860800.0],[508928.0,6858752.0,509952.0,6859776.0],[509952.0,6858752.0,510976.0,6859776.0],[510976.0,6858752.0,512000.0,6859776.0],[512000.0,6858752.0,513024.0,6859776.0],[513024.0,6858752.0,514048.0,6859776.0],[514048.0,6858752.0,515072.0,6859776.0],[508928.0,6857728.0,509952.0,6858752.0],[509952.0,6857728.0,510976.0,6858752.0],[510976.0,6857728.0,512000.0,6858752.0],[512000.0,6857728.0,513024.0,6858752.0],[513024.0,6857728.0,514048.0,6858752.0],[514048.0,6857728.0,515072.0,6858752.0],[508928.0,6856704.0,509952.0,6857728.0],[509952.0,6856704.0,510976.0,6857728.0],[510976.0,6856704.0,512000.0,6857728.0],[512000.0,6856704.0,513024.0,6857728.0],[513024.0,6856704.0,514048.0,6857728.0],[514048.0,6856704.0,515072.0,6857728.0]]},\"tileSize\":{\"width\":256,\"height\":256},\"mapSize\":{\"width\":1130,\"height\":530},\"mapScales\":[5669294.4,2834647.2,1417323.6,566929.44,283464.72,141732.36,56692.944,28346.472,11338.5888,5669.2944,2834.6472,1417.3236,708.6618],\"layers\":{\"216\":{\"id\":216,\"styleName\":\"default\",\"visible\":true},\"134\":{\"id\":134,\"styleName\":\"default\",\"visible\":true}}}";
	private static String layerJSON = "{\"layerId\":216,\"username\":\"\",\"password\":\"\",\"maxFeatures\":100,\"featureNamespace\":\"pkartta\",\"featureNamespaceURI\":\"www.pkartta.fi\",\"featureElement\":\"toimipaikat\",\"featureType\":{},\"selectedFeatureParams\":{},\"featureParamsLocales\":{},\"geometryType\":\"2d\",\"getMapTiles\":true,\"getFeatureInfo\":true,\"tileRequest\":false,\"minScale\":50000.0,\"maxScale\":1.0,\"templateName\":null,\"templateDescription\":null,\"templateType\":null,\"requestTemplate\":null,\"responseTemplate\":null,\"selectionSLDStyle\":null,\"styles\":{\"default\":{\"id\":\"1\",\"name\":\"default\",\"SLDStyle\":\"<?xml version=\\\"1.0\\\" encoding=\\\"ISO-8859-1\\\"?><StyledLayerDescriptor version=\\\"1.0.0\\\" xmlns=\\\"http://www.opengis.net/sld\\\" xmlns:ogc=\\\"http://www.opengis.net/ogc\\\" xmlns:xlink=\\\"http://www.w3.org/1999/xlink\\\" xmlns:xsi=\\\"http://www.w3.org/2001/XMLSchema-instance\\\" xsi:schemaLocation=\\\"http://www.opengis.net/sld http://schemas.opengis.net/sld/1.0.0/StyledLayerDescriptor.xsd\\\"><NamedLayer><Name>Palvelupisteet</Name><UserStyle><Title>Palvelupisteiden tyyli</Title><Abstract/><FeatureTypeStyle><Rule><Title>Piste</Title><PointSymbolizer><Graphic><Mark><WellKnownName>circle</WellKnownName><Fill><CssParameter name=\\\"fill\\\">#FFFFFF</CssParameter></Fill><Stroke><CssParameter name=\\\"stroke\\\">#000000</CssParameter><CssParameter name=\\\"stroke-width\\\">2</CssParameter></Stroke></Mark><Size>12</Size></Graphic></PointSymbolizer></Rule></FeatureTypeStyle></UserStyle></NamedLayer></StyledLayerDescriptor>\"}},\"URL\":\"http://kartta.suomi.fi/geoserver/wfs\",\"GMLGeometryProperty\":\"the_geom\",\"SRSName\":\"EPSG:3067\",\"GMLVersion\":\"3.1.1\",\"WFSVersion\":\"1.1.0\",\"WMSLayerId\":null}";
	
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
		JedisManager.connect(10, "localhost", 6379);

        Properties properties = new Properties();
        try {
            properties.load(TransportService.class.getResourceAsStream("/transport.properties"));
            PropertyUtil.addProperties(properties, true);
        } catch (Exception e) {
            System.err.println("Configuration could not be loaded");
            e.printStackTrace();
        }

		try {
			session = SessionStore.setJSON(sessionJSON);
			layer = WFSLayerStore.setJSON(layerJSON);
            type = JobType.NORMAL;
		} catch (IOException e) {
			fail("Should not throw exception");
		}
		
		bounds = new ArrayList<Double>();
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

    @Test
    public void testFailingFilterConstruct() throws NullPointerException {
        String layerId = "nonexistent_216";
        WFSFilter filter = WFSCommunicator.constructFilter(layerId);
        assertTrue("Should be null", filter == null);
    }
}
