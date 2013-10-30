package fi.nls.oskari.wfs.extension;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import fi.nls.oskari.work.WFSMapLayerJob;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.junit.Before;
import org.junit.Test;

import com.vividsolutions.jts.geom.Coordinate;

import fi.nls.oskari.pojo.GeoJSONFilter;
import fi.nls.oskari.pojo.SessionStore;
import fi.nls.oskari.pojo.WFSLayerStore;
import fi.nls.oskari.utils.XMLHelper;

public class AnalysisFilterTest {
    private SessionStore session;
    private WFSLayerStore layer;
    private WFSMapLayerJob.Type type;
    private GeoJSONFilter geojsonFilter;
    private GeoJSONFilter geojsonComplexFilter;
    private List<Double> emptyBounds;
    private List<Double> bounds;

    private String sessionJSON = "{\"client\":\"ryayjt49k377p1ks4afxsr5oki\",\"session\":\"473F9168ACE614E746B039896DEE5D41\",\"route\":\"\",\"language\":\"fi\",\"browser\":\"safari\",\"browserVersion\":537,\"location\":{\"srs\":\"EPSG:3067\",\"bbox\":[425724.45809412,6695489.1337378,427451.45809412,6696484.1337378],\"zoom\":10},\"grid\":{\"rows\":5,\"columns\":8,\"bounds\":[[425472.0,6696448.0,425728.0,6696704.0],[425728.0,6696448.0,425984.0,6696704.0],[425984.0,6696448.0,426240.0,6696704.0],[426240.0,6696448.0,426496.0,6696704.0],[426496.0,6696448.0,426752.0,6696704.0],[426752.0,6696448.0,427008.0,6696704.0],[427008.0,6696448.0,427264.0,6696704.0],[427264.0,6696448.0,427520.0,6696704.0],[425472.0,6696192.0,425728.0,6696448.0],[425728.0,6696192.0,425984.0,6696448.0],[425984.0,6696192.0,426240.0,6696448.0],[426240.0,6696192.0,426496.0,6696448.0],[426496.0,6696192.0,426752.0,6696448.0],[426752.0,6696192.0,427008.0,6696448.0],[427008.0,6696192.0,427264.0,6696448.0],[427264.0,6696192.0,427520.0,6696448.0],[425472.0,6695936.0,425728.0,6696192.0],[425728.0,6695936.0,425984.0,6696192.0],[425984.0,6695936.0,426240.0,6696192.0],[426240.0,6695936.0,426496.0,6696192.0],[426496.0,6695936.0,426752.0,6696192.0],[426752.0,6695936.0,427008.0,6696192.0],[427008.0,6695936.0,427264.0,6696192.0],[427264.0,6695936.0,427520.0,6696192.0],[425472.0,6695680.0,425728.0,6695936.0],[425728.0,6695680.0,425984.0,6695936.0],[425984.0,6695680.0,426240.0,6695936.0],[426240.0,6695680.0,426496.0,6695936.0],[426496.0,6695680.0,426752.0,6695936.0],[426752.0,6695680.0,427008.0,6695936.0],[427008.0,6695680.0,427264.0,6695936.0],[427264.0,6695680.0,427520.0,6695936.0],[425472.0,6695424.0,425728.0,6695680.0],[425728.0,6695424.0,425984.0,6695680.0],[425984.0,6695424.0,426240.0,6695680.0],[426240.0,6695424.0,426496.0,6695680.0],[426496.0,6695424.0,426752.0,6695680.0],[426752.0,6695424.0,427008.0,6695680.0],[427008.0,6695424.0,427264.0,6695680.0],[427264.0,6695424.0,427520.0,6695680.0]]},\"tileSize\":{\"width\":256,\"height\":256},\"mapSize\":{\"width\":1727,\"height\":995},\"mapScales\":[5669294.4,2834647.2,1417323.6,566929.44,283464.72,141732.36,56692.944,28346.472,11338.5888,5669.2944,2834.6472,1417.3236,708.6618],\"layers\":{\"analysis_216_710\":{\"id\":\"analysis_216_710\",\"styleName\":\"default\",\"visible\":true}}}";
    private String layerJSON = "{\"selectedFeatureParams\":{\"default\":[\"n1\",\"t1\",\"t2\",\"t3\",\"t4\",\"t5\",\"t6\",\"t7\",\"n2\",\"n3\"]},\"getMapTiles\":\"false\",\"layerName\":\"ana:analysis_data\",\"featureElement\":\"analysis_data\",\"password\":\"pationus\",\"username\":\"admin\",\"GMLGeometryProperty\":\"geometry\",\"nameLocales\":{\"fi\":{\"name\":\"Analyysitaso\",\"subtitle\":\"\"},\"sv\":{\"name\":\"Analyysitaso\",\"subtitle\":\"\"},\"en\":{\"name\":\"Analyysitaso\",\"subtitle\":\"\"}},\"featureType\":{},\"maxFeatures\":100,\"URL\":\"http://localhost:8080/geoserver/wfs\",\"maxScale\":1,\"featureParamsLocales\":{},\"getFeatureInfo\":\"true\",\"tileRequest\":\"false\",\"styles\":{},\"layerId\":\"analysis_216_710\",\"WFSVersion\":\"1.1.0\",\"GML2Separator\":\"false\",\"featureNamespace\":\"ana\",\"SRSName\":\"EPSG:3067\",\"minScale\":800000,\"GMLVersion\":\"3.1.1\",\"featureNamespaceURI\":\"http://nls.paikkatietoikkuna.fi/analysis\",\"geometryType\":\"2d\"}";
    private String geojson = "{\"data\":{\"filter\":{\"geojson\":{\"type\":\"FeatureCollection\",\"features\":[{\"type\":\"Feature\",\"properties\":{},\"geometry\":{\"type\":\"Polygon\",\"coordinates\":[[[394081,6691734],[394361,6692574],[393521,6692854],[393241,6692014],[394081,6691734]]]}}],\"crs\":{\"type\":\"EPSG\",\"properties\":{\"code\":3067}}}}} }";
    private String geojsonComplex = "{\"data\":{\"filter\":{\"geojson\":{\"type\":\"FeatureCollection\",\"features\":[{\"type\":\"Feature\",\"properties\":{},\"geometry\":{\"type\":\"Polygon\",\"coordinates\":[[[426125.95809412,6695752.6337378],[426535.95809412,6696262.6337378],[426025.95809412,6696672.6337378],[425615.95809412,6696162.6337378],[426125.95809412,6695752.6337378]]]}},{\"type\":\"Feature\",\"properties\":{},\"geometry\":{\"type\":\"Polygon\",\"coordinates\":[[[426843.70809412,6696053.8837378],[427215.70809412,6696609.8837378],[426659.70809412,6696981.8837378],[426287.70809412,6696425.8837378],[426843.70809412,6696053.8837378]]]}},{\"type\":\"Feature\",\"properties\":{},\"geometry\":{\"type\":\"Polygon\",\"coordinates\":[[[426595.70809412,6695941.8837378],[426195.70809412,6696245.8837378],[425891.70809412,6695845.8837378],[426291.70809412,6695541.8837378],[426595.70809412,6695941.8837378]]]}}],\"crs\":{\"type\":\"EPSG\",\"properties\":{\"code\":3067}}}}}}";

    String result = "<ogc:Filter xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:gml=\"http://www.opengis.net/gml\"><ogc:And><ogc:PropertyIsEqualTo matchCase=\"false\"><ogc:PropertyName>analysis_id</ogc:PropertyName><ogc:Literal>710</ogc:Literal></ogc:PropertyIsEqualTo><ogc:BBOX><ogc:PropertyName>geometry</ogc:PropertyName><gml:Envelope srsDimension=\"2\" srsName=\"EPSG:3067\"><gml:lowerCorner>425724.45809412 6695489.1337378</gml:lowerCorner><gml:upperCorner>427451.45809412 6696484.1337378</gml:upperCorner></gml:Envelope></ogc:BBOX></ogc:And></ogc:Filter>";
    String resultBounds = "<ogc:Filter xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:gml=\"http://www.opengis.net/gml\"><ogc:And><ogc:PropertyIsEqualTo matchCase=\"false\"><ogc:PropertyName>analysis_id</ogc:PropertyName><ogc:Literal>710</ogc:Literal></ogc:PropertyIsEqualTo><ogc:BBOX><ogc:PropertyName>geometry</ogc:PropertyName><gml:Envelope srsDimension=\"2\" srsName=\"EPSG:3067\"><gml:lowerCorner>385800.0 6690267.0</gml:lowerCorner><gml:upperCorner>397380.0 6697397.0</gml:upperCorner></gml:Envelope></ogc:BBOX></ogc:And></ogc:Filter>";
    String resultMapClick = "<ogc:Filter xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:gml=\"http://www.opengis.net/gml\"><ogc:And><ogc:PropertyIsEqualTo matchCase=\"false\"><ogc:PropertyName>analysis_id</ogc:PropertyName><ogc:Literal>710</ogc:Literal></ogc:PropertyIsEqualTo><ogc:Intersects><ogc:PropertyName>geometry</ogc:PropertyName><gml:Polygon srsDimension=\"2\"><gml:exterior><gml:LinearRing srsDimension=\"2\"><gml:posList>393896.00000162 6692163.0 393895.42705229373 6692164.763356709 393893.9270514837 6692165.853171089 393892.0729485163 6692165.853171089 393890.57294770627 6692164.763356709 393889.99999838 6692163.0 393890.57294770627 6692161.236643291 393892.0729485163 6692160.146828911 393893.9270514837 6692160.146828911 393895.42705229373 6692161.236643291 393896.00000162 6692163.0</gml:posList></gml:LinearRing></gml:exterior></gml:Polygon></ogc:Intersects></ogc:And></ogc:Filter>";
    String resultHighlightFeatures = "<ogc:Filter xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:gml=\"http://www.opengis.net/gml\"><ogc:FeatureId fid=\"toimipaikat.6398\"/></ogc:Filter>";
    String resultGeoJson = "<ogc:Filter xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:gml=\"http://www.opengis.net/gml\"><ogc:And><ogc:PropertyIsEqualTo matchCase=\"false\"><ogc:PropertyName>analysis_id</ogc:PropertyName><ogc:Literal>710</ogc:Literal></ogc:PropertyIsEqualTo><ogc:Intersects><ogc:PropertyName>geometry</ogc:PropertyName><gml:Polygon srsDimension=\"2\"><gml:exterior><gml:LinearRing srsDimension=\"2\"><gml:posList>394081.0 6691734.0 394361.0 6692574.0 393521.0 6692854.0 393241.0 6692014.0 394081.0 6691734.0</gml:posList></gml:LinearRing></gml:exterior></gml:Polygon></ogc:Intersects></ogc:And></ogc:Filter>";
    String resultGeoJsonComplex = "<ogc:Filter xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:gml=\"http://www.opengis.net/gml\"><ogc:And><ogc:PropertyIsEqualTo matchCase=\"false\"><ogc:PropertyName>analysis_id</ogc:PropertyName><ogc:Literal>710</ogc:Literal></ogc:PropertyIsEqualTo><ogc:Or><ogc:Intersects><ogc:PropertyName>geometry</ogc:PropertyName><gml:Polygon srsDimension=\"2\"><gml:exterior><gml:LinearRing srsDimension=\"2\"><gml:posList>426125.95809412 6695752.6337378 426535.95809412 6696262.6337378 426025.95809412 6696672.6337378 425615.95809412 6696162.6337378 426125.95809412 6695752.6337378</gml:posList></gml:LinearRing></gml:exterior></gml:Polygon></ogc:Intersects><ogc:Intersects><ogc:PropertyName>geometry</ogc:PropertyName><gml:Polygon srsDimension=\"2\"><gml:exterior><gml:LinearRing srsDimension=\"2\"><gml:posList>426843.70809412 6696053.8837378 427215.70809412 6696609.8837378 426659.70809412 6696981.8837378 426287.70809412 6696425.8837378 426843.70809412 6696053.8837378</gml:posList></gml:LinearRing></gml:exterior></gml:Polygon></ogc:Intersects><ogc:Intersects><ogc:PropertyName>geometry</ogc:PropertyName><gml:Polygon srsDimension=\"2\"><gml:exterior><gml:LinearRing srsDimension=\"2\"><gml:posList>426595.70809412 6695941.8837378 426195.70809412 6696245.8837378 425891.70809412 6695845.8837378 426291.70809412 6695541.8837378 426595.70809412 6695941.8837378</gml:posList></gml:LinearRing></gml:exterior></gml:Polygon></ogc:Intersects></ogc:Or></ogc:And></ogc:Filter>";

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
    public void testLocation() {
        type = WFSMapLayerJob.Type.NORMAL;
        AnalysisFilter analysisFilter = new AnalysisFilter();
        String filterStr = analysisFilter.create(type, layer, session, emptyBounds, null);
        OMElement filter = null;
        if(filterStr != null) {
            StAXOMBuilder staxOMBuilder = XMLHelper.createBuilder(filterStr);
            filter = staxOMBuilder.getDocumentElement();
        }
        assertTrue("Should get expected result", filter.toString().equals(result));
    }

    @Test
    public void testBounds() {
        type = WFSMapLayerJob.Type.NORMAL;
        AnalysisFilter analysisFilter = new AnalysisFilter();
        String filterStr = analysisFilter.create(type, layer, session, bounds, null);
        OMElement filter = null;
        if(filterStr != null) {
            StAXOMBuilder staxOMBuilder = XMLHelper.createBuilder(filterStr);
            filter = staxOMBuilder.getDocumentElement();
        }
        assertTrue("Should get expected resultBounds", filter.toString().equals(resultBounds));
    }

    @Test
    public void testMapClick() {
        type = WFSMapLayerJob.Type.MAP_CLICK;
        session.setMapClick(new Coordinate(393893.0, 6692163.0));

        AnalysisFilter analysisFilter = new AnalysisFilter();
        String filterStr = analysisFilter.create(type, layer, session, emptyBounds, null);
        OMElement filter = null;
        if(filterStr != null) {
            StAXOMBuilder staxOMBuilder = XMLHelper.createBuilder(filterStr);
            filter = staxOMBuilder.getDocumentElement();
        }
        assertTrue("Should get expected resultMapClick", filter.toString().equals(resultMapClick));
    }

    @Test
    public void testGeoJson() {
        type = WFSMapLayerJob.Type.GEOJSON;
        session.setFilter(geojsonFilter);

        AnalysisFilter analysisFilter = new AnalysisFilter();
        String filterStr = analysisFilter.create(type, layer, session, emptyBounds, null);
        OMElement filter = null;
        if(filterStr != null) {
            StAXOMBuilder staxOMBuilder = XMLHelper.createBuilder(filterStr);
            filter = staxOMBuilder.getDocumentElement();
        }
        assertTrue("Should get expected resultGeoJson", filter.toString().equals(resultGeoJson));

        // multiple geometries
        session.setFilter(geojsonComplexFilter);
        filterStr = analysisFilter.create(type, layer, session, emptyBounds, null);
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
        type = WFSMapLayerJob.Type.HIGHLIGHT;
        List<String> featureIds = new ArrayList<String>();
        featureIds.add("toimipaikat.6398");
        session.getLayers().get("analysis_216_710").setHighlightedFeatureIds(featureIds);

        AnalysisFilter analysisFilter = new AnalysisFilter();
        String filterStr = analysisFilter.create(type, layer, session, emptyBounds, null);
        OMElement filter = null;
        if(filterStr != null) {
            StAXOMBuilder staxOMBuilder = XMLHelper.createBuilder(filterStr);
            filter = staxOMBuilder.getDocumentElement();
        }
        assertTrue("Should get expected resultHighlightFeatures", filter.toString().equals(resultHighlightFeatures));
    }

}
