package fi.nls.oskari.wfs;

import com.vividsolutions.jts.geom.Geometry;
import fi.nls.oskari.pojo.SessionStore;
import fi.nls.test.util.TestHelper;
import fi.nls.oskari.wfs.pojo.WFSLayerStore;
import fi.nls.oskari.wfs.util.HttpHelper;
import fi.nls.oskari.work.JobType;
import org.geotools.feature.FeatureCollection;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

public class WFSParserTest {
	private static SessionStore session;
	private static WFSLayerStore layer;
    private static JobType type;
	private static List<Double> bounds;

	private static String sessionJSON = "{\"client\":\"71k229bstn5ub1fbv1xzwnlnmz\",\"session\":\"49E8CFEF9A310C76438952F8FCD9FF2D\",\"language\":\"fi\",\"browser\":\"mozilla\",\"browserVersion\":20,\"location\":{\"srs\":\"EPSG:3067\",\"bbox\":[509058.0,6858054.0,513578.0,6860174.0],\"zoom\":8},\"grid\":{\"rows\":4,\"columns\":6,\"bounds\":[[508928.0,6859776.0,509952.0,6860800.0],[509952.0,6859776.0,510976.0,6860800.0],[510976.0,6859776.0,512000.0,6860800.0],[512000.0,6859776.0,513024.0,6860800.0],[513024.0,6859776.0,514048.0,6860800.0],[514048.0,6859776.0,515072.0,6860800.0],[508928.0,6858752.0,509952.0,6859776.0],[509952.0,6858752.0,510976.0,6859776.0],[510976.0,6858752.0,512000.0,6859776.0],[512000.0,6858752.0,513024.0,6859776.0],[513024.0,6858752.0,514048.0,6859776.0],[514048.0,6858752.0,515072.0,6859776.0],[508928.0,6857728.0,509952.0,6858752.0],[509952.0,6857728.0,510976.0,6858752.0],[510976.0,6857728.0,512000.0,6858752.0],[512000.0,6857728.0,513024.0,6858752.0],[513024.0,6857728.0,514048.0,6858752.0],[514048.0,6857728.0,515072.0,6858752.0],[508928.0,6856704.0,509952.0,6857728.0],[509952.0,6856704.0,510976.0,6857728.0],[510976.0,6856704.0,512000.0,6857728.0],[512000.0,6856704.0,513024.0,6857728.0],[513024.0,6856704.0,514048.0,6857728.0],[514048.0,6856704.0,515072.0,6857728.0]]},\"tileSize\":{\"width\":256,\"height\":256},\"mapSize\":{\"width\":1130,\"height\":530},\"mapScales\":[5669294.4,2834647.2,1417323.6,566929.44,283464.72,141732.36,56692.944,28346.472,11338.5888,5669.2944,2834.6472,1417.3236,708.6618],\"layers\":{\"216\":{\"id\":216,\"styleName\":\"default\",\"visible\":true},\"134\":{\"id\":134,\"styleName\":\"default\",\"visible\":true}}}";
	private static String layerJSON = "{\"layerId\":216,\"username\":\"\",\"password\":\"\",\"maxFeatures\":100,\"featureNamespace\":\"pkartta\",\"featureNamespaceURI\":\"www.pkartta.fi\",\"featureElement\":\"toimipaikat\",\"featureType\":{},\"selectedFeatureParams\":{},\"featureParamsLocales\":{},\"geometryType\":\"2d\",\"getMapTiles\":true,\"getFeatureInfo\":true,\"tileRequest\":false,\"minScale\":50000.0,\"maxScale\":1.0,\"templateName\":null,\"templateDescription\":null,\"templateType\":null,\"requestTemplate\":null,\"responseTemplate\":null,\"selectionSLDStyle\":null,\"styles\":{\"default\":{\"id\":\"1\",\"name\":\"default\",\"SLDStyle\":\"<?xml version=\\\"1.0\\\" encoding=\\\"ISO-8859-1\\\"?><StyledLayerDescriptor version=\\\"1.0.0\\\" xmlns=\\\"http://www.opengis.net/sld\\\" xmlns:ogc=\\\"http://www.opengis.net/ogc\\\" xmlns:xlink=\\\"http://www.w3.org/1999/xlink\\\" xmlns:xsi=\\\"http://www.w3.org/2001/XMLSchema-instance\\\" xsi:schemaLocation=\\\"http://www.opengis.net/sld http://schemas.opengis.net/sld/1.0.0/StyledLayerDescriptor.xsd\\\"><NamedLayer><Name>Palvelupisteet</Name><UserStyle><Title>Palvelupisteiden tyyli</Title><Abstract/><FeatureTypeStyle><Rule><Title>Piste</Title><PointSymbolizer><Graphic><Mark><WellKnownName>circle</WellKnownName><Fill><CssParameter name=\\\"fill\\\">#FFFFFF</CssParameter></Fill><Stroke><CssParameter name=\\\"stroke\\\">#000000</CssParameter><CssParameter name=\\\"stroke-width\\\">2</CssParameter></Stroke></Mark><Size>12</Size></Graphic></PointSymbolizer></Rule></FeatureTypeStyle></UserStyle></NamedLayer></StyledLayerDescriptor>\"}},\"URL\":\"http://kartta.suomi.fi/geoserver/wfs\",\"GMLGeometryProperty\":\"the_geom\",\"SRSName\":\"EPSG:3067\",\"GMLVersion\":\"3.1.1\",\"WFSVersion\":\"1.1.0\",\"WMSLayerId\":null}";

	String geomPointXML = "<example><gml:Point xmlns:gml=\"http://www.opengis.net/gml\" srsDimension=\"2\" srsName=\"http://www.opengis.net/gml/srs/epsg.xml#3067\"><gml:pos>385877.0 6671637.0</gml:pos></gml:Point></example>";
    String multiSurfaceGeomXML = "<gml:MultiSurface xmlns:gml=\"http://www.opengis.net/gml\" srsName=\"http://www.opengis.net/gml/srs/epsg.xml#3067\" ><gml:surfaceMember><gml:Surface srsName=\"http://www.opengis.net/gml/srs/epsg.xml#3067\"><gml:patches><gml:PolygonPatch><gml:exterior><gml:LinearRing><gml:posList srsDimension='2' count='40'>329408.761 6822136.694 329578.724 6822150.521 329779.535 6822083.635 329832.268 6822037.864 329858.803 6822026.926 329876.053 6822014.648 329973.222 6821972.197 329982.507 6821992.433 330014.344 6821978.836 330018.327 6821967.888 330078.356 6821951.642 330069.401 6821931.735 330066.747 6821914.489 330078.356 6821895.923 330237.213 6821830.916 330256.45 6821840.204 330331.24 6821805.381 330350.304 6821788.135 330344.673 6821778.517 330419.957 6821667.089 330486.946 6821634.916 330607.962 6821523.738 330635.858 6821507.892 330677.31 6821441.565 330744.967 6821412.371 330771.502 6821377.548 330736.35 6821384.847 330681.623 6821378.608 330613.966 6821377.838 330526.081 6821387.496 330428.574 6821425.968 330333.063 6821483.017 330260.095 6821550.673 330155.299 6821664.1 330170.059 6821708.871 329914.025 6821905.211 329828.128 6821967.558 329666.279 6822050.802 329547.052 6822105.85 329408.761 6822136.694</gml:posList></gml:LinearRing></gml:exterior></gml:PolygonPatch></gml:patches></gml:Surface></gml:surfaceMember></gml:MultiSurface>";
    String surfaceGeomXML = "<gml:Surface xmlns:gml=\"http://www.opengis.net/gml\" srsName=\"http://www.opengis.net/gml/srs/epsg.xml#3067\"><gml:patches><gml:PolygonPatch><gml:exterior><gml:LinearRing><gml:posList srsDimension='2' count='40'>329408.761 6822136.694 329578.724 6822150.521 329779.535 6822083.635 329832.268 6822037.864 329858.803 6822026.926 329876.053 6822014.648 329973.222 6821972.197 329982.507 6821992.433 330014.344 6821978.836 330018.327 6821967.888 330078.356 6821951.642 330069.401 6821931.735 330066.747 6821914.489 330078.356 6821895.923 330237.213 6821830.916 330256.45 6821840.204 330331.24 6821805.381 330350.304 6821788.135 330344.673 6821778.517 330419.957 6821667.089 330486.946 6821634.916 330607.962 6821523.738 330635.858 6821507.892 330677.31 6821441.565 330744.967 6821412.371 330771.502 6821377.548 330736.35 6821384.847 330681.623 6821378.608 330613.966 6821377.838 330526.081 6821387.496 330428.574 6821425.968 330333.063 6821483.017 330260.095 6821550.673 330155.299 6821664.1 330170.059 6821708.871 329914.025 6821905.211 329828.128 6821967.558 329666.279 6822050.802 329547.052 6822105.85 329408.761 6822136.694</gml:posList></gml:LinearRing></gml:exterior></gml:PolygonPatch></gml:patches></gml:Surface>";

    @BeforeClass
    public static void setUp() {
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
	@Ignore("Ignored because service is down and build can't complete with http-request in performed against live site")
	public void testParser() {

        // check that we have http connectivity (correct proxy settings etc)
        assumeTrue(TestHelper.canDoHttp());

        // * in the config marks the default geometry
        // should contain whole schema or at least the selectedFeatureParams (+ GEOMETRY)
        layer.addFeatureType("default", "fi_nimi:String,fi_osoite:String,postinumero:String,*the_geom:Point");

		String payload = WFSCommunicator.createRequestPayload(type, layer, session, bounds, null);

		// request (maplayer_id 216)
		// FIXME: instead of making actual http request, record an expected response and parse it.
		Reader response = HttpHelper.postRequestReader(this.layer.getURL(), "", payload, this.layer.getUsername(), this.layer.getPassword());
		assertTrue("Should get valid response", response != null);

		// parse
		WFSParser parser = new WFSParser(response, layer);
		FeatureCollection<SimpleFeatureType, SimpleFeature> features = parser.parse();
		assertTrue("Should get valid features", features != null);
		assertTrue("Should get features", features.size() > 0);
	}

	@Test
	public void testParseGeometry() {
        WFSParser parser = new WFSParser(null, layer);
		Geometry geom1 = parser.parseGeometry(geomPointXML);
		assertTrue("Should get valid geometry", geom1 != null);
		assertTrue("Should get x = 385877.0", geom1.getCoordinate().x == 385877.0);
		assertTrue("Should get y = 6671637.0", geom1.getCoordinate().y ==  6671637.0);
        assertTrue("Should be NOT EMPTY", !geom1.isEmpty());

        // empty result - can't handle multi surface
        Geometry geom2 = parser.parseGeometry(multiSurfaceGeomXML);
        assertTrue("Should get valid geometry", geom2 != null);
        assertTrue("Should NOT be EMPTY", !geom2.isEmpty());

        // same geom than geom2 but in flat surface (no multi surface)
        Geometry geom3 = parser.parseGeometry(surfaceGeomXML);
        assertTrue("Should get valid geometry", geom3 != null);
        assertTrue("Should NOT be EMPTY", !geom3.isEmpty());
	}
}
