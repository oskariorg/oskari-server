package fi.nls.oskari.wfs.extension;

/*
    This test verifies, how geotools mock schema is generated when schema locations are not available
     - Schema is generated using structure of 1st feature in xml response
     - so, parsing result is invalid, if feature properties count is not equal for all features and 1st feature has less properties than the other ones
 */

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import fi.nls.oskari.cache.JedisManager;
import fi.nls.oskari.pojo.SessionStore;
import fi.nls.oskari.transport.TransportService;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.wfs.WFSCommunicator;
import fi.nls.oskari.wfs.WFSFilter;
import fi.nls.oskari.wfs.WFSParser;
import fi.nls.oskari.wfs.pojo.WFSLayerStore;
import fi.nls.oskari.work.JobType;
import fi.nls.test.util.ResourceHelper;
import fi.nls.test.util.TestHelper;
import org.custommonkey.xmlunit.XMLUnit;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.referencing.operation.MathTransform;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

public class WFSAnalysisResponseParseTest {
	private static SessionStore session;
	private static WFSLayerStore layer;
    private static JobType type;
	private static List<Double> bounds;
	
	private static String sessionJSON = "{\"client\":\"2a1pfp35mdm5fdkl9xipgrfth6s\",\"session\":\"D6FD2035677FA26163732781C04C8D98\",\"route\":\"\",\"uuid\":\"938446bf-b1b5-4b27-b51a-9230cf839e8a\",\"language\":\"fi\",\"browser\":\"safari\",\"browserVersion\":537,\"location\":{\"srs\":\"EPSG:3067\",\"bbox\":[357952.0,6759760.0,383344.0,6773392.0],\"zoom\":7},\"grid\":{\"rows\":5,\"columns\":8,\"bounds\":[[356640.0,6770688.0,360736.0,6774784.0],[360736.0,6770688.0,364832.0,6774784.0],[364832.0,6770688.0,368928.0,6774784.0],[368928.0,6770688.0,373024.0,6774784.0],[373024.0,6770688.0,377120.0,6774784.0],[377120.0,6770688.0,381216.0,6774784.0],[381216.0,6770688.0,385312.0,6774784.0],[385312.0,6770688.0,389408.0,6774784.0],[356640.0,6766592.0,360736.0,6770688.0],[360736.0,6766592.0,364832.0,6770688.0],[364832.0,6766592.0,368928.0,6770688.0],[368928.0,6766592.0,373024.0,6770688.0],[373024.0,6766592.0,377120.0,6770688.0],[377120.0,6766592.0,381216.0,6770688.0],[381216.0,6766592.0,385312.0,6770688.0],[385312.0,6766592.0,389408.0,6770688.0],[356640.0,6762496.0,360736.0,6766592.0],[360736.0,6762496.0,364832.0,6766592.0],[364832.0,6762496.0,368928.0,6766592.0],[368928.0,6762496.0,373024.0,6766592.0],[373024.0,6762496.0,377120.0,6766592.0],[377120.0,6762496.0,381216.0,6766592.0],[381216.0,6762496.0,385312.0,6766592.0],[385312.0,6762496.0,389408.0,6766592.0],[356640.0,6758400.0,360736.0,6762496.0],[360736.0,6758400.0,364832.0,6762496.0],[364832.0,6758400.0,368928.0,6762496.0],[368928.0,6758400.0,373024.0,6762496.0],[373024.0,6758400.0,377120.0,6762496.0],[377120.0,6758400.0,381216.0,6762496.0],[381216.0,6758400.0,385312.0,6762496.0],[385312.0,6758400.0,389408.0,6762496.0],[356640.0,6754304.0,360736.0,6758400.0],[360736.0,6754304.0,364832.0,6758400.0],[364832.0,6754304.0,368928.0,6758400.0],[368928.0,6754304.0,373024.0,6758400.0],[373024.0,6754304.0,377120.0,6758400.0],[377120.0,6754304.0,381216.0,6758400.0],[381216.0,6754304.0,385312.0,6758400.0],[385312.0,6754304.0,389408.0,6758400.0]]},\"tileSize\":{\"width\":256,\"height\":256},\"mapSize\":{\"width\":1587,\"height\":852},\"mapScales\":[5805342.72,2902671.36,1451335.68,725667.84,362833.92,181416.96,90708.48,45354.24,22677.12,11338.56,5669.28,2834.64,1417.32,708.66],\"layers\":{\"analysis_1018_5673\":{\"id\":\"analysis_1018_5673\",\"styleName\":\"default\",\"visible\":true}}}";

	private static String layerJSON = "{\"selectedFeatureParams\":{\"default\":[\"t1\",\"n1\",\"n2\",\"n3\",\"n4\",\"n5\",\"n6\"]},\"getMapTiles\":false,\"layerName\":\"oskari:analysis_data\",\"featureElement\":\"analysis_data\",\"password\":\"pationus\",\"getHighlightImage\":true,\"username\":\"admin\",\"GMLGeometryProperty\":\"geometry\",\"tileBuffer\":{},\"geometryNamespaceURI\":\"\",\"featureType\":{},\"maxFeatures\":2000,\"maxScale\":1,\"URL\":\"http://demo.paikkatietoikkuna.fi/dataset/analysis/service/ows\",\"isPublished\":false,\"featureParamsLocales\":{},\"getFeatureInfo\":true,\"tileRequest\":false,\"styles\":{},\"layerId\":\"analysis_1018_5673\",\"WFSVersion\":\"1.1.0\",\"GML2Separator\":false,\"minScale\":1.5E7,\"SRSName\":\"EPSG:3067\",\"featureNamespace\":\"oskari\",\"GMLVersion\":\"3.1.1\",\"attributes\":\"{}\",\"featureNamespaceURI\":\"http://www.oskari.org\",\"uiName\":\"Analyysitaso\",\"geometryType\":\"2d\"}";


	//String sessionResult = ResourceHelper.readStringResource("WFSCommunicatorTest-result-session.xml", this);
    String sresponse = ResourceHelper.readStringResource("WFSAnalysisResponseParseTest-GetFeature-response.xml", this);

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
		// 362532.0 6758528.0</gml:lowerCorner><gml:upperCorner>375228.0 6764992.0
		bounds = new ArrayList<Double>();
		bounds.add(362532.0);
        bounds.add(6758528.0);
		bounds.add(375228.0);
		bounds.add(6764992.0);
    }

	
	@Test
	public void testAnalysisResponse() throws Exception {

        BufferedReader response = new BufferedReader(new StringReader(sresponse));
                assertTrue("Should get valid response", response != null);
		
		// parse
		FeatureCollection<SimpleFeatureType, SimpleFeature> features = WFSCommunicator.parseSimpleFeatures(response, layer);
		assertTrue("Should get valid features", features != null);
		assertTrue("Should get features", features.size() > 0);

        // create filter of screen area
        Filter screenBBOXFilter = WFSFilter.initBBOXFilter(this.session.getLocation(), this.layer, false);

        // send feature info
        FeatureIterator<SimpleFeature> featuresIter =  features.features();

        List<List<Object>> featureValuesList = new ArrayList<List<Object>>();
        List<List<Object>> geomValuesList = new ArrayList<List<Object>>();
        Boolean sendFeatures = true;
        MathTransform transformClient = this.session.getLocation().getTransformForClient(this.layer.getCrs(), true);

        final List<String> selectedProperties = getPropertiesToInclude();

        while(featuresIter.hasNext()) {
            SimpleFeature feature = featuresIter.next();
            String fid = feature.getIdentifier().getID();
            System.out.println("Processing properties of feature:" + fid);

            // if is not in shown area -> skip
            if(!screenBBOXFilter.evaluate(feature)) {
                System.out.println("Feature not on screen, skipping" + fid);
                continue;
            }

            List<Object> values = new ArrayList<Object>();



            // get feature geometry (transform if needed) and get geometry center
            Geometry geometry = WFSParser.getFeatureGeometry(feature, this.layer.getGMLGeometryProperty(), transformClient);

            // Add geometry property, if requested  in hili
            if (this.session.isGeomRequest())
            {
                System.out.println("Requested geometry" + fid);
                List<Object> gvalues = new ArrayList<Object>();
                gvalues.add(fid);
                if( geometry != null ) {
                    gvalues.add(geometry.toText()); //feature.getAttribute(this.layer.getGMLGeometryProperty()));
                } else {
                    gvalues.add(null);
                }
                geomValuesList.add(gvalues);
            }

            // send values
            if(!sendFeatures) {
                System.out.println("Didn't request properties - skipping" + fid);
                continue;
            }
            Point centerPoint = WFSParser.getGeometryCenter(geometry);

            for (String attr : selectedProperties) {
                values.add(getFeaturePropertyValueForResponse(feature.getAttribute(attr)));
            }

            // center position (must be in properties also)
            if(centerPoint != null) {
                values.add(centerPoint.getX());
                values.add(centerPoint.getY());
            } else {
                values.add(null);
                values.add(null);
            }

            System.out.println("Got property values:" + values.toString());
            WFSParser.parseValuesForJSON(values);
            System.out.println("Transformed property values:" + values.toString());
            // 2nd feature output:
            // Transformed property values:[naiset, 144.0, null, null, null, null, null, 368864.9289099526, 6760158.767772512]
            // Should be: Transformed property values:[naiset, 144.0, 27559.0, 3.0, 1690.0, 191.38194444444446, 356.3104139130455, 368864.9289099526, 6760158.767772512]


                featureValuesList.add(values);

        }

        // Check values

	}
    private List<String> getPropertiesToInclude() {

        final List<String> selectedProperties = layer.getSelectedFeatureParams(session.getLanguage());
        if (selectedProperties != null && selectedProperties.size() != 0) {
            System.out.println("Using selected properties:" + selectedProperties.toString());
            return selectedProperties;
        }
        return selectedProperties;
    }

    private Object getFeaturePropertyValueForResponse(final Object input) {
        if(input == null) {
            return null;
        }
        final String value = input.toString();
        if(value == null) {
            return null;
        }
        if(value.isEmpty()) {
            System.out.println("Value is empty");
            return "";
        }
        try {
            HashMap<String, Object> propMap = new ObjectMapper().readValue(value, HashMap.class);
            if(propMap.isEmpty()) {
                System.out.println("Got empty map from value: '" + value + "' - Returning null. Input was" + input.getClass().getName());
                return null;
            }
            return propMap;
        } catch (Exception e) {
            return value;
        }

    }

    @After
    public void tearDown() {
        PropertyUtil.clearProperties();
    }
}
