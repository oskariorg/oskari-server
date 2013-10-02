package fi.nls.oskari.wfs;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geotools.feature.FeatureCollection;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jts.geom.Geometry;

import fi.nls.oskari.pojo.SessionStore;
import fi.nls.oskari.pojo.WFSLayerStore;
import fi.nls.oskari.utils.HttpHelper;

public class WFSParserTest {
	private static SessionStore session;
	private static WFSLayerStore layer;
	private static List<Double> bounds;

	private static String sessionJSON = "{\"client\":\"71k229bstn5ub1fbv1xzwnlnmz\",\"session\":\"49E8CFEF9A310C76438952F8FCD9FF2D\",\"language\":\"fi\",\"browser\":\"mozilla\",\"browserVersion\":20,\"location\":{\"srs\":\"EPSG:3067\",\"bbox\":[509058.0,6858054.0,513578.0,6860174.0],\"zoom\":8},\"grid\":{\"rows\":4,\"columns\":6,\"bounds\":[[508928.0,6859776.0,509952.0,6860800.0],[509952.0,6859776.0,510976.0,6860800.0],[510976.0,6859776.0,512000.0,6860800.0],[512000.0,6859776.0,513024.0,6860800.0],[513024.0,6859776.0,514048.0,6860800.0],[514048.0,6859776.0,515072.0,6860800.0],[508928.0,6858752.0,509952.0,6859776.0],[509952.0,6858752.0,510976.0,6859776.0],[510976.0,6858752.0,512000.0,6859776.0],[512000.0,6858752.0,513024.0,6859776.0],[513024.0,6858752.0,514048.0,6859776.0],[514048.0,6858752.0,515072.0,6859776.0],[508928.0,6857728.0,509952.0,6858752.0],[509952.0,6857728.0,510976.0,6858752.0],[510976.0,6857728.0,512000.0,6858752.0],[512000.0,6857728.0,513024.0,6858752.0],[513024.0,6857728.0,514048.0,6858752.0],[514048.0,6857728.0,515072.0,6858752.0],[508928.0,6856704.0,509952.0,6857728.0],[509952.0,6856704.0,510976.0,6857728.0],[510976.0,6856704.0,512000.0,6857728.0],[512000.0,6856704.0,513024.0,6857728.0],[513024.0,6856704.0,514048.0,6857728.0],[514048.0,6856704.0,515072.0,6857728.0]]},\"tileSize\":{\"width\":256,\"height\":256},\"mapSize\":{\"width\":1130,\"height\":530},\"mapScales\":[5669294.4,2834647.2,1417323.6,566929.44,283464.72,141732.36,56692.944,28346.472,11338.5888,5669.2944,2834.6472,1417.3236,708.6618],\"layers\":{\"216\":{\"id\":216,\"styleName\":\"default\",\"visible\":true},\"134\":{\"id\":134,\"styleName\":\"default\",\"visible\":true}}}";
	private static String layerJSON = "{\"layerId\":216,\"nameLocales\":{\"fi\":{\"name\":\"Palvelupisteiden kyselypalvelu\",\"subtitle\":\"\"},\"sv\":{\"name\":\"Söktjänst för serviceställen\",\"subtitle\":\"\"},\"en\":{\"name\":\"Public services query service\",\"subtitle\":\"\"}},\"username\":\"\",\"password\":\"\",\"maxFeatures\":100,\"featureNamespace\":\"pkartta\",\"featureNamespaceURI\":\"www.pkartta.fi\",\"featureElement\":\"toimipaikat\",\"featureType\":{},\"selectedFeatureParams\":[],\"featureParamsLocales\":{},\"geometryType\":\"2d\",\"getMapTiles\":true,\"getFeatureInfo\":true,\"tileRequest\":false,\"minScale\":50000.0,\"maxScale\":1.0,\"templateName\":null,\"templateDescription\":null,\"templateType\":null,\"requestTemplate\":null,\"responseTemplate\":null,\"selectionSLDStyle\":null,\"styles\":{\"default\":{\"id\":\"1\",\"name\":\"default\",\"SLDStyle\":\"<?xml version=\\\"1.0\\\" encoding=\\\"ISO-8859-1\\\"?><StyledLayerDescriptor version=\\\"1.0.0\\\" xmlns=\\\"http://www.opengis.net/sld\\\" xmlns:ogc=\\\"http://www.opengis.net/ogc\\\" xmlns:xlink=\\\"http://www.w3.org/1999/xlink\\\" xmlns:xsi=\\\"http://www.w3.org/2001/XMLSchema-instance\\\" xsi:schemaLocation=\\\"http://www.opengis.net/sld http://schemas.opengis.net/sld/1.0.0/StyledLayerDescriptor.xsd\\\"><NamedLayer><Name>Palvelupisteet</Name><UserStyle><Title>Palvelupisteiden tyyli</Title><Abstract/><FeatureTypeStyle><Rule><Title>Piste</Title><PointSymbolizer><Graphic><Mark><WellKnownName>circle</WellKnownName><Fill><CssParameter name=\\\"fill\\\">#FFFFFF</CssParameter></Fill><Stroke><CssParameter name=\\\"stroke\\\">#000000</CssParameter><CssParameter name=\\\"stroke-width\\\">2</CssParameter></Stroke></Mark><Size>12</Size></Graphic></PointSymbolizer></Rule></FeatureTypeStyle></UserStyle></NamedLayer></StyledLayerDescriptor>\"}},\"URL\":\"http://kartta.suomi.fi/geoserver/wfs\",\"GMLGeometryProperty\":\"the_geom\",\"SRSName\":\"EPSG:3067\",\"GMLVersion\":\"3.1.1\",\"WFSVersion\":\"1.1.0\",\"WMSLayerId\":null}";

	String geomPointXML = "<example><gml:Point xmlns:gml=\"http://www.opengis.net/gml\" srsDimension=\"2\" srsName=\"http://www.opengis.net/gml/srs/epsg.xml#3067\"><gml:pos>385877.0 6671637.0</gml:pos></gml:Point></example>";

	@BeforeClass
    public static void setUp() {
		try {
			session = SessionStore.setJSON(sessionJSON);
			layer = WFSLayerStore.setJSON(layerJSON);
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
	public void testParser() {
        Map<String, String> inputFeatureTypes = new HashMap<String, String>();
        // * in the config marks the default geometry
        // should contain whole schema or at least the selectedFeatureParams (+ GEOMETRY)
        inputFeatureTypes.put("default", "fi_nimi:String,fi_osoite:String,postinumero:String,*the_geom:Point");
        layer.setFeatureType(inputFeatureTypes);

		String payload = WFSCommunicator.createRequestPayload(layer, session, bounds, null);

		// request (maplayer_id 216)
        BufferedReader response = HttpHelper.postRequestReader(this.layer.getURL(), "", payload, this.layer.getUsername(), this.layer.getPassword());
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
	}
}
