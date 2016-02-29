package fi.nls.oskari.wfs.extension;

import com.vividsolutions.jts.geom.Coordinate;
import fi.nls.oskari.pojo.GeoJSONFilter;
import fi.nls.oskari.pojo.SessionStore;
import fi.nls.oskari.wfs.pojo.WFSLayerStore;
import fi.nls.oskari.wfs.util.XMLHelper;
import fi.nls.oskari.work.JobType;
import fi.nls.test.util.ResourceHelper;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class AnalysisFilterTest {
    private SessionStore session;
    private WFSLayerStore layer;
    private JobType type;
    private GeoJSONFilter geojsonFilter;
    private GeoJSONFilter geojsonComplexFilter;
    private List<Double> emptyBounds;
    private List<Double> bounds;

    private String sessionJSON = "{\"client\":\"ryayjt49k377p1ks4afxsr5oki\",\"session\":\"473F9168ACE614E746B039896DEE5D41\",\"route\":\"\",\"language\":\"fi\",\"browser\":\"safari\",\"browserVersion\":537,\"location\":{\"srs\":\"EPSG:3067\",\"bbox\":[425724.45809412,6695489.1337378,427451.45809412,6696484.1337378],\"zoom\":10},\"grid\":{\"rows\":5,\"columns\":8,\"bounds\":[[425472.0,6696448.0,425728.0,6696704.0],[425728.0,6696448.0,425984.0,6696704.0],[425984.0,6696448.0,426240.0,6696704.0],[426240.0,6696448.0,426496.0,6696704.0],[426496.0,6696448.0,426752.0,6696704.0],[426752.0,6696448.0,427008.0,6696704.0],[427008.0,6696448.0,427264.0,6696704.0],[427264.0,6696448.0,427520.0,6696704.0],[425472.0,6696192.0,425728.0,6696448.0],[425728.0,6696192.0,425984.0,6696448.0],[425984.0,6696192.0,426240.0,6696448.0],[426240.0,6696192.0,426496.0,6696448.0],[426496.0,6696192.0,426752.0,6696448.0],[426752.0,6696192.0,427008.0,6696448.0],[427008.0,6696192.0,427264.0,6696448.0],[427264.0,6696192.0,427520.0,6696448.0],[425472.0,6695936.0,425728.0,6696192.0],[425728.0,6695936.0,425984.0,6696192.0],[425984.0,6695936.0,426240.0,6696192.0],[426240.0,6695936.0,426496.0,6696192.0],[426496.0,6695936.0,426752.0,6696192.0],[426752.0,6695936.0,427008.0,6696192.0],[427008.0,6695936.0,427264.0,6696192.0],[427264.0,6695936.0,427520.0,6696192.0],[425472.0,6695680.0,425728.0,6695936.0],[425728.0,6695680.0,425984.0,6695936.0],[425984.0,6695680.0,426240.0,6695936.0],[426240.0,6695680.0,426496.0,6695936.0],[426496.0,6695680.0,426752.0,6695936.0],[426752.0,6695680.0,427008.0,6695936.0],[427008.0,6695680.0,427264.0,6695936.0],[427264.0,6695680.0,427520.0,6695936.0],[425472.0,6695424.0,425728.0,6695680.0],[425728.0,6695424.0,425984.0,6695680.0],[425984.0,6695424.0,426240.0,6695680.0],[426240.0,6695424.0,426496.0,6695680.0],[426496.0,6695424.0,426752.0,6695680.0],[426752.0,6695424.0,427008.0,6695680.0],[427008.0,6695424.0,427264.0,6695680.0],[427264.0,6695424.0,427520.0,6695680.0]]},\"tileSize\":{\"width\":256,\"height\":256},\"mapSize\":{\"width\":1727,\"height\":995},\"mapScales\":[5669294.4,2834647.2,1417323.6,566929.44,283464.72,141732.36,56692.944,28346.472,11338.5888,5669.2944,2834.6472,1417.3236,708.6618],\"layers\":{\"analysis_216_710\":{\"id\":\"analysis_216_710\",\"styleName\":\"default\",\"visible\":true}}}";
    private String layerJSON = "{\"selectedFeatureParams\":{\"default\":[\"n1\",\"t1\",\"t2\",\"t3\",\"t4\",\"t5\",\"t6\",\"t7\",\"n2\",\"n3\"]},\"getMapTiles\":\"false\",\"layerName\":\"ana:analysis_data\",\"featureElement\":\"analysis_data\",\"password\":\"pationus\",\"username\":\"admin\",\"GMLGeometryProperty\":\"geometry\",\"featureType\":{},\"maxFeatures\":100,\"URL\":\"http://localhost:8080/geoserver/wfs\",\"maxScale\":1,\"featureParamsLocales\":{},\"getFeatureInfo\":\"true\",\"tileRequest\":\"false\",\"styles\":{},\"layerId\":\"analysis_216_710\",\"WFSVersion\":\"1.1.0\",\"GML2Separator\":\"false\",\"featureNamespace\":\"ana\",\"SRSName\":\"EPSG:3067\",\"minScale\":800000,\"GMLVersion\":\"3.1.1\",\"featureNamespaceURI\":\"http://nls.paikkatietoikkuna.fi/analysis\",\"geometryType\":\"2d\"}";
    private String geojson = "{\"data\":{\"filter\":{\"geojson\":{\"type\":\"FeatureCollection\",\"features\":[{\"type\":\"Feature\",\"properties\":{},\"geometry\":{\"type\":\"Polygon\",\"coordinates\":[[[394081,6691734],[394361,6692574],[393521,6692854],[393241,6692014],[394081,6691734]]]}}],\"crs\":{\"type\":\"EPSG\",\"properties\":{\"code\":3067}}}}} }";
    private String geojsonComplex = "{\"data\":{\"filter\":{\"geojson\":{\"type\":\"FeatureCollection\",\"features\":[{\"type\":\"Feature\",\"properties\":{},\"geometry\":{\"type\":\"Polygon\",\"coordinates\":[[[426125.95809412,6695752.6337378],[426535.95809412,6696262.6337378],[426025.95809412,6696672.6337378],[425615.95809412,6696162.6337378],[426125.95809412,6695752.6337378]]]}},{\"type\":\"Feature\",\"properties\":{},\"geometry\":{\"type\":\"Polygon\",\"coordinates\":[[[426843.70809412,6696053.8837378],[427215.70809412,6696609.8837378],[426659.70809412,6696981.8837378],[426287.70809412,6696425.8837378],[426843.70809412,6696053.8837378]]]}},{\"type\":\"Feature\",\"properties\":{},\"geometry\":{\"type\":\"Polygon\",\"coordinates\":[[[426595.70809412,6695941.8837378],[426195.70809412,6696245.8837378],[425891.70809412,6695845.8837378],[426291.70809412,6695541.8837378],[426595.70809412,6695941.8837378]]]}}],\"crs\":{\"type\":\"EPSG\",\"properties\":{\"code\":3067}}}}}}";

    String resultLocation = ResourceHelper.readStringResource("AnalysisFilter-result-testlocation.xml", this);
    String resultBounds = ResourceHelper.readStringResource("AnalysisFilter-result-bounds.xml", this);
    String resultMapClick = ResourceHelper.readStringResource("AnalysisFilter-result-mapclick.xml", this);
    String resultHighlightFeatures = ResourceHelper.readStringResource("AnalysisFilter-result-highlight-features.xml", this);
    String resultGeoJson = ResourceHelper.readStringResource("AnalysisFilter-result-geojson-simple.xml", this);
    String resultGeoJsonComplex = ResourceHelper.readStringResource("AnalysisFilter-result-geojson-complex.xml", this);


    @Before
    public void setUp() {
        // use relaxed comparison settings
        XMLUnit.setIgnoreComments(true);
        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setIgnoreDiffBetweenTextAndCDATA(true);
        XMLUnit.setIgnoreAttributeOrder(true);

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
    public void testLocation() throws Exception {
        type = JobType.NORMAL;
        AnalysisFilter analysisFilter = new AnalysisFilter();
        String filterStr = analysisFilter.create(type, layer, session, emptyBounds, null);
        OMElement filter = null;
        if(filterStr != null) {
            StAXOMBuilder staxOMBuilder = XMLHelper.createBuilder(filterStr);
            filter = staxOMBuilder.getDocumentElement();
        }
        Diff xmlDiff = new Diff(resultLocation, filter.toString());
        assertTrue("Should get expected location result " + xmlDiff, xmlDiff.similar());
    }

    @Test
    public void testBounds() throws Exception {
        type = JobType.NORMAL;
        AnalysisFilter analysisFilter = new AnalysisFilter();
        String filterStr = analysisFilter.create(type, layer, session, bounds, null);
        OMElement filter = null;
        if(filterStr != null) {
            StAXOMBuilder staxOMBuilder = XMLHelper.createBuilder(filterStr);
            filter = staxOMBuilder.getDocumentElement();
        }
        Diff xmlDiff = new Diff(resultBounds, filter.toString());
        assertTrue("Should get expected resultBounds " + xmlDiff, xmlDiff.similar());
    }

    @Test
    public void testMapClick() throws Exception {
        type = JobType.MAP_CLICK;
        session.setMapClick(new Coordinate(393893.0, 6692163.0));

        AnalysisFilter analysisFilter = new AnalysisFilter();
        String filterStr = analysisFilter.create(type, layer, session, emptyBounds, null);
        OMElement filter = null;
        if(filterStr != null) {
            StAXOMBuilder staxOMBuilder = XMLHelper.createBuilder(filterStr);
            filter = staxOMBuilder.getDocumentElement();
        }
        Diff xmlDiff = new Diff(resultMapClick, filter.toString());
        assertTrue("Should get expected resultMapClick " + xmlDiff, xmlDiff.similar());
    }

    @Test
    public void testGeoJson() throws Exception {
        type = JobType.GEOJSON;
        session.setFilter(geojsonFilter);

        AnalysisFilter analysisFilter = new AnalysisFilter();
        String filterStr = analysisFilter.create(type, layer, session, emptyBounds, null);
        OMElement filter = null;
        if(filterStr != null) {
            StAXOMBuilder staxOMBuilder = XMLHelper.createBuilder(filterStr);
            filter = staxOMBuilder.getDocumentElement();
        }
        Diff xmlDiff = new Diff(resultGeoJson, filter.toString());
        assertTrue("Should get expected resultGeoJsonSimple " + xmlDiff, xmlDiff.similar());
        //assertEquals("Should get expected resultGeoJson", resultGeoJson, filter.toString());

        // multiple geometries
        session.setFilter(geojsonComplexFilter);
        filterStr = analysisFilter.create(type, layer, session, emptyBounds, null);
        filter = null;
        if(filterStr != null) {
            StAXOMBuilder staxOMBuilder = XMLHelper.createBuilder(filterStr);
            filter = staxOMBuilder.getDocumentElement();
        }

        Diff xmlDiffComplex = new Diff(resultGeoJsonComplex, filter.toString());
        assertTrue("Should get expected resultGeoJsonComplex " + xmlDiffComplex, xmlDiffComplex.similar());
    }

    @Test
    public void testHighlight() throws Exception {
        type = JobType.HIGHLIGHT;
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
        Diff xmlDiff = new Diff(resultHighlightFeatures, filter.toString());
        assertTrue("Should get expected resultHighlightFeatures " + xmlDiff, xmlDiff.similar());
    }

}
