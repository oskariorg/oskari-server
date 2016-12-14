package fi.nls.oskari.wfs;

import com.vividsolutions.jts.geom.Coordinate;
import fi.nls.oskari.pojo.GeoJSONFilter;
import fi.nls.oskari.pojo.SessionStore;
import fi.nls.oskari.wfs.pojo.WFSLayerStore;
import fi.nls.oskari.wfs.util.XMLHelper;
import fi.nls.oskari.work.JobType;
import fi.nls.test.util.ResourceHelper;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.opengis.filter.Filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class WFSFilterTest {
	private SessionStore session;
	private WFSLayerStore layer;
    private JobType type;
	private GeoJSONFilter geojsonFilter;
    private GeoJSONFilter geojsonComplexFilter;
	private List<Double> emptyBounds;
	private List<Double> bounds;
	
	private String sessionJSON = "{\"client\":\"71k229bstn5ub1fbv1xzwnlnmz\",\"session\":\"49E8CFEF9A310C76438952F8FCD9FF2D\",\"language\":\"fi\",\"browser\":\"mozilla\",\"browserVersion\":20,\"location\":{\"srs\":\"EPSG:3067\",\"bbox\":[509058.0,6858054.0,513578.0,6860174.0],\"zoom\":8},\"grid\":{\"rows\":4,\"columns\":6,\"bounds\":[[508928.0,6859776.0,509952.0,6860800.0],[509952.0,6859776.0,510976.0,6860800.0],[510976.0,6859776.0,512000.0,6860800.0],[512000.0,6859776.0,513024.0,6860800.0],[513024.0,6859776.0,514048.0,6860800.0],[514048.0,6859776.0,515072.0,6860800.0],[508928.0,6858752.0,509952.0,6859776.0],[509952.0,6858752.0,510976.0,6859776.0],[510976.0,6858752.0,512000.0,6859776.0],[512000.0,6858752.0,513024.0,6859776.0],[513024.0,6858752.0,514048.0,6859776.0],[514048.0,6858752.0,515072.0,6859776.0],[508928.0,6857728.0,509952.0,6858752.0],[509952.0,6857728.0,510976.0,6858752.0],[510976.0,6857728.0,512000.0,6858752.0],[512000.0,6857728.0,513024.0,6858752.0],[513024.0,6857728.0,514048.0,6858752.0],[514048.0,6857728.0,515072.0,6858752.0],[508928.0,6856704.0,509952.0,6857728.0],[509952.0,6856704.0,510976.0,6857728.0],[510976.0,6856704.0,512000.0,6857728.0],[512000.0,6856704.0,513024.0,6857728.0],[513024.0,6856704.0,514048.0,6857728.0],[514048.0,6856704.0,515072.0,6857728.0]]},\"tileSize\":{\"width\":256,\"height\":256},\"mapSize\":{\"width\":1130,\"height\":530},\"mapScales\":[5669294.4,2834647.2,1417323.6,566929.44,283464.72,141732.36,56692.944,28346.472,11338.5888,5669.2944,2834.6472,1417.3236,708.6618],\"layers\":{\"216\":{\"id\":216,\"styleName\":\"default\",\"visible\":true},\"134\":{\"id\":134,\"styleName\":\"default\",\"visible\":true}}}";
	private String layerJSON = ResourceHelper.readStringResource("WFSFilterTest-layerJSON-input.json", this);
	private String geojson = "{\"data\":{\"filter\":{\"geojson\":{\"type\":\"FeatureCollection\",\"features\":[{\"type\":\"Feature\",\"properties\":{},\"geometry\":{\"type\":\"Polygon\",\"coordinates\":[[[394081,6691734],[394361,6692574],[393521,6692854],[393241,6692014],[394081,6691734]]]}}],\"crs\":{\"type\":\"EPSG\",\"properties\":{\"code\":3067}}}}} }";
    private String geojsonComplex = "{\"data\":{\"filter\":{\"geojson\":{\"type\":\"FeatureCollection\",\"features\":[{\"type\":\"Feature\",\"properties\":{},\"geometry\":{\"type\":\"Polygon\",\"coordinates\":[[[426125.95809412,6695752.6337378],[426535.95809412,6696262.6337378],[426025.95809412,6696672.6337378],[425615.95809412,6696162.6337378],[426125.95809412,6695752.6337378]]]}},{\"type\":\"Feature\",\"properties\":{},\"geometry\":{\"type\":\"Polygon\",\"coordinates\":[[[426843.70809412,6696053.8837378],[427215.70809412,6696609.8837378],[426659.70809412,6696981.8837378],[426287.70809412,6696425.8837378],[426843.70809412,6696053.8837378]]]}},{\"type\":\"Feature\",\"properties\":{},\"geometry\":{\"type\":\"Polygon\",\"coordinates\":[[[426595.70809412,6695941.8837378],[426195.70809412,6696245.8837378],[425891.70809412,6695845.8837378],[426291.70809412,6695541.8837378],[426595.70809412,6695941.8837378]]]}}],\"crs\":{\"type\":\"EPSG\",\"properties\":{\"code\":3067}}}}}}";

    // FIXME: move to files as in other test and use ResourceHelper to load them - Use XmlDiff to compare...
    String result = "<ogc:Filter xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:gml=\"http://www.opengis.net/gml\"><ogc:BBOX><ogc:PropertyName>the_geom</ogc:PropertyName><gml:Envelope srsDimension=\"2\" srsName=\"EPSG:3067\"><gml:lowerCorner>509058.0 6858054.0</gml:lowerCorner><gml:upperCorner>513578.0 6860174.0</gml:upperCorner></gml:Envelope></ogc:BBOX></ogc:Filter>";
	String resultBounds = "<ogc:Filter xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:gml=\"http://www.opengis.net/gml\"><ogc:BBOX><ogc:PropertyName>the_geom</ogc:PropertyName><gml:Envelope srsDimension=\"2\" srsName=\"EPSG:3067\"><gml:lowerCorner>385800.0 6690267.0</gml:lowerCorner><gml:upperCorner>397380.0 6697397.0</gml:upperCorner></gml:Envelope></ogc:BBOX></ogc:Filter>";
	String resultMapClick = "<ogc:Filter xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:gml=\"http://www.opengis.net/gml\"><ogc:Intersects>[ the_geom intersects POLYGON ((393905.00000648 6692163, 393902.708209175 6692170.053426837, 393896.70820593496 6692174.412684359, 393889.29179406504 6692174.412684359, 393883.291790825 6692170.053426837, 393880.99999352 6692163, 393883.291790825 6692155.946573163, 393889.29179406504 6692151.587315641, 393896.70820593496 6692151.587315641, 393902.708209175 6692155.946573163, 393905.00000648 6692163)) ]<ogc:PropertyName>the_geom</ogc:PropertyName><gml:Polygon srsDimension=\"2\"><gml:exterior><gml:LinearRing srsDimension=\"2\"><gml:posList>393905.00000648 6692163.0 393902.708209175 6692170.053426837 393896.70820593496 6692174.412684359 393889.29179406504 6692174.412684359 393883.291790825 6692170.053426837 393880.99999352 6692163.0 393883.291790825 6692155.946573163 393889.29179406504 6692151.587315641 393896.70820593496 6692151.587315641 393902.708209175 6692155.946573163 393905.00000648 6692163.0</gml:posList></gml:LinearRing></gml:exterior></gml:Polygon></ogc:Intersects></ogc:Filter>";
	String resultHighlightFeatures = "<ogc:Filter xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:gml=\"http://www.opengis.net/gml\"><ogc:FeatureId fid=\"toimipaikat.6398\"/></ogc:Filter>";
	String resultGeoJson ="<ogc:Filter xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:gml=\"http://www.opengis.net/gml\"><ogc:Intersects>[ the_geom intersects POLYGON ((394081 6691734, 394361 6692574, 393521 6692854, 393241 6692014, 394081 6691734)) ]<ogc:PropertyName>the_geom</ogc:PropertyName><gml:Polygon srsDimension=\"2\"><gml:exterior><gml:LinearRing srsDimension=\"2\"><gml:posList>394081.0 6691734.0 394361.0 6692574.0 393521.0 6692854.0 393241.0 6692014.0 394081.0 6691734.0</gml:posList></gml:LinearRing></gml:exterior></gml:Polygon></ogc:Intersects></ogc:Filter>";
    String resultGeoJsonComplex = "<ogc:Filter xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:gml=\"http://www.opengis.net/gml\"><ogc:Or><ogc:Intersects>[ the_geom intersects POLYGON ((426125.95809412 6695752.6337378, 426535.95809412 6696262.6337378, 426025.95809412 6696672.6337378, 425615.95809412 6696162.6337378, 426125.95809412 6695752.6337378)) ]<ogc:PropertyName>the_geom</ogc:PropertyName><gml:Polygon srsDimension=\"2\"><gml:exterior><gml:LinearRing srsDimension=\"2\"><gml:posList>426125.95809412 6695752.6337378 426535.95809412 6696262.6337378 426025.95809412 6696672.6337378 425615.95809412 6696162.6337378 426125.95809412 6695752.6337378</gml:posList></gml:LinearRing></gml:exterior></gml:Polygon></ogc:Intersects><ogc:Intersects>[ the_geom intersects POLYGON ((426843.70809412 6696053.8837378, 427215.70809412 6696609.8837378, 426659.70809412 6696981.8837378, 426287.70809412 6696425.8837378, 426843.70809412 6696053.8837378)) ]<ogc:PropertyName>the_geom</ogc:PropertyName><gml:Polygon srsDimension=\"2\"><gml:exterior><gml:LinearRing srsDimension=\"2\"><gml:posList>426843.70809412 6696053.8837378 427215.70809412 6696609.8837378 426659.70809412 6696981.8837378 426287.70809412 6696425.8837378 426843.70809412 6696053.8837378</gml:posList></gml:LinearRing></gml:exterior></gml:Polygon></ogc:Intersects><ogc:Intersects>[ the_geom intersects POLYGON ((426595.70809412 6695941.8837378, 426195.70809412 6696245.8837378, 425891.70809412 6695845.8837378, 426291.70809412 6695541.8837378, 426595.70809412 6695941.8837378)) ]<ogc:PropertyName>the_geom</ogc:PropertyName><gml:Polygon srsDimension=\"2\"><gml:exterior><gml:LinearRing srsDimension=\"2\"><gml:posList>426595.70809412 6695941.8837378 426195.70809412 6696245.8837378 425891.70809412 6695845.8837378 426291.70809412 6695541.8837378 426595.70809412 6695941.8837378</gml:posList></gml:LinearRing></gml:exterior></gml:Polygon></ogc:Intersects></ogc:Or></ogc:Filter>";

    @Before
    public void setUp() {
		try {
			session = SessionStore.setJSON(sessionJSON);
			layer = WFSLayerStore.setJSON(layerJSON);
		} catch (IOException e) {
			fail("Should not throw exception");
		}
		geojsonFilter = GeoJSONFilter.setParamsJSON(geojson);
        geojsonComplexFilter = GeoJSONFilter.setParamsJSON(geojsonComplex);
		
		emptyBounds = null;
		bounds = new ArrayList<Double>();
		bounds.add(385800.0);
		bounds.add(6690267.0);
		bounds.add(397380.0);
		bounds.add(6697397.0);
    }

    @Test
    public void testDefaultBuffer() {
        WFSFilter wfsFilter = new WFSFilter();
        double mapScale = session.getMapScales().get((int) session.getLocation().getZoom());
        wfsFilter.setDefaultBuffer(mapScale);
        double buffer = wfsFilter.getDefaultBuffer();
        assertTrue("Should get expected buffer size", buffer > 24.00d && buffer < 24.001d);
    }
    
	@Test
	public void testLocation() {
        type = JobType.NORMAL;
		WFSFilter wfsFilter = new WFSFilter();
        String filterStr = wfsFilter.create(type, layer, session, emptyBounds, null);
		OMElement filter = null;
		if(filterStr != null) {
	        StAXOMBuilder staxOMBuilder = XMLHelper.createBuilder(filterStr);
	        filter = staxOMBuilder.getDocumentElement();
		}

		assertEquals("Should get expected result", result, filter.toString());
	}

	@Test
	public void testBounds() {
        type = JobType.NORMAL;
		WFSFilter wfsFilter = new WFSFilter();
        String filterStr = wfsFilter.create(type, layer, session, bounds, null);
        OMElement filter = null;
		if(filterStr != null) {
	        StAXOMBuilder staxOMBuilder = XMLHelper.createBuilder(filterStr);
	        filter = staxOMBuilder.getDocumentElement();
		}
		assertTrue("Should get expected resultBounds", filter.toString().equals(resultBounds));
	}

	@Test
	public void testMapClick() {
        type = JobType.MAP_CLICK;
		session.setMapClick(new Coordinate(393893.0, 6692163.0));

		WFSFilter wfsFilter = new WFSFilter();
        String filterStr = wfsFilter.create(type, layer, session, emptyBounds, null);
		OMElement filter = null;
		if(filterStr != null) {
	        StAXOMBuilder staxOMBuilder = XMLHelper.createBuilder(filterStr);
	        filter = staxOMBuilder.getDocumentElement();
		}
		assertTrue("Should get expected resultMapClick", filter.toString().equals(resultMapClick));
	}
	
	@Test
	public void testGeoJson() {
        type = JobType.GEOJSON;
    	session.setFilter(geojsonFilter);

    	WFSFilter wfsFilter = new WFSFilter();
        String filterStr = wfsFilter.create(type, layer, session, emptyBounds, null);
    	OMElement filter = null;
		if(filterStr != null) {
	        StAXOMBuilder staxOMBuilder = XMLHelper.createBuilder(filterStr);
	        filter = staxOMBuilder.getDocumentElement();
		}
		assertTrue("Should get expected resultGeoJson", filter.toString().equals(resultGeoJson));

        // multiple geometries
        session.setFilter(geojsonComplexFilter);
        filterStr = wfsFilter.create(type, layer, session, emptyBounds, null);
        filter = null;
        if(filterStr != null) {
            StAXOMBuilder staxOMBuilder = XMLHelper.createBuilder(filterStr);
            filter = staxOMBuilder.getDocumentElement();
        }
        System.out.println(filter.toString());
        assertTrue("Should get expected resultGeoJson", filter.toString().equals(resultGeoJsonComplex));
	}
	
	@Test
	public void testHighlight() {
        type = JobType.HIGHLIGHT;
    	List<String> featureIds = new ArrayList<String>();
    	featureIds.add("toimipaikat.6398");
		session.getLayers().get("216").setHighlightedFeatureIds(featureIds);

		WFSFilter wfsFilter = new WFSFilter();
        String filterStr = wfsFilter.create(type, layer, session, emptyBounds, null);
		OMElement filter = null;
		if(filterStr != null) {
	        StAXOMBuilder staxOMBuilder = XMLHelper.createBuilder(filterStr);
	        filter = staxOMBuilder.getDocumentElement();
		}
		assertTrue("Should get expected resultHighlightFeatures", filter.toString().equals(resultHighlightFeatures));
	}

    @Test
	public void initGeoJson() {
		//initGeoJSONFilter
        String input = ResourceHelper.readStringResource("WFSFilterTest-geojson-point-input.json", this);
        GeoJSONFilter geoJson = GeoJSONFilter.setParamsJSON(input);
        WFSFilter wfsFilter = new WFSFilter();
        wfsFilter.setDefaultBuffer(59d);
		// NOTE!!! This fails with geotools 11.2/13.1 and Java 8!
		// With Java 7 it works ok (JSONObject seems to keep the order "correct" for Geotools with Java 7).
		// See breakGeotoolsGeoJSon() below
        WFSLayerStore layer = new WFSLayerStore();
        layer.setGMLGeometryProperty("the_geom");
        Filter f = wfsFilter.initGeoJSONFilter(geoJson, "EPSG:3067", layer);
        assertNotNull(f);
        System.out.println("F on " + f);
	}

	//@Test(expected = ClassCastException.class) - works in gt 14.2
	@Test
	public void breakGeotoolsGeoJSon() throws IOException {
		String inputInExpectedOrder = "{\n" +
				"  \"type\": \"Point\",\n" +
				"  \"coordinates\": [386260, 6676432]\n" +
				"}";
		String inputThatBreaksGeometryJSONparsing = "{\n" +
				"  \"coordinates\": [386260, 6676432],\n" +
				"  \"type\": \"Point\"\n" +
				"}";
		org.geotools.geojson.geom.GeometryJSON geometryJSON = new org.geotools.geojson.geom.GeometryJSON();
		Object worksOk = geometryJSON.readPoint(inputInExpectedOrder);
		System.out.println("F on " + worksOk);
		// works correctly: F on POINT (386260 6676432)

		Object thisFails = geometryJSON.readPoint(inputThatBreaksGeometryJSONparsing);
		// NOTE!!! This fails with geotools 11.2 and 13.1
        /*
java.lang.ClassCastException: java.lang.String cannot be cast to java.lang.Number
at org.geotools.geojson.GeoJSONUtil.createCoordinate(GeoJSONUtil.java:218)
at org.geotools.geojson.geom.GeometryHandlerBase.coordinate(GeometryHandlerBase.java:54)
at org.geotools.geojson.geom.PointHandler.endObject(PointHandler.java:50)
at org.json.simple.parser.JSONParser.parse(Unknown Source)
         */
		System.out.println("Failed on " + thisFails);
	}
}
