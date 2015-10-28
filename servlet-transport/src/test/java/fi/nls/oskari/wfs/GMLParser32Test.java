package fi.nls.oskari.wfs;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import fi.nls.oskari.cache.JedisManager;
import fi.nls.oskari.wfs.pojo.WFSLayerStore;
import org.geotools.xml.Parser;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class GMLParser32Test {
	private static WFSLayerStore layer;
	
	private static String json = "{\"layerId\":216,\"username\":\"\",\"password\":\"\",\"maxFeatures\":100,\"featureNamespace\":\"pkartta\",\"featureNamespaceURI\":\"www.pkartta.fi\",\"featureElement\":\"toimipaikat\",\"featureType\":\"\",\"selectedFeatureParams\":[],\"featureParamsLocales\":{},\"geometryType\":\"2d\",\"getMapTiles\":true,\"getFeatureInfo\":true,\"tileRequest\":false,\"minScale\":50000.0,\"maxScale\":1.0,\"templateName\":null,\"templateDescription\":null,\"templateType\":null,\"requestTemplate\":null,\"responseTemplate\":null,\"selectionSLDStyle\":null,\"styles\":{\"default\":{\"id\":\"1\",\"name\":\"default\",\"SLDStyle\":\"<?xml version=\\\"1.0\\\" encoding=\\\"ISO-8859-1\\\"?><StyledLayerDescriptor version=\\\"1.0.0\\\" xmlns=\\\"http://www.opengis.net/sld\\\" xmlns:ogc=\\\"http://www.opengis.net/ogc\\\" xmlns:xlink=\\\"http://www.w3.org/1999/xlink\\\" xmlns:xsi=\\\"http://www.w3.org/2001/XMLSchema-instance\\\" xsi:schemaLocation=\\\"http://www.opengis.net/sld http://schemas.opengis.net/sld/1.0.0/StyledLayerDescriptor.xsd\\\"><NamedLayer><Name>Palvelupisteet</Name><UserStyle><Title>Palvelupisteiden tyyli</Title><Abstract/><FeatureTypeStyle><Rule><Title>Piste</Title><PointSymbolizer><Graphic><Mark><WellKnownName>circle</WellKnownName><Fill><CssParameter name=\\\"fill\\\">#FFFFFF</CssParameter></Fill><Stroke><CssParameter name=\\\"stroke\\\">#000000</CssParameter><CssParameter name=\\\"stroke-width\\\">2</CssParameter></Stroke></Mark><Size>12</Size></Graphic></PointSymbolizer></Rule></FeatureTypeStyle></UserStyle></NamedLayer></StyledLayerDescriptor>\"}},\"URL\":\"http://kartta.suomi.fi/geoserver/wfs\",\"GMLGeometryProperty\":\"the_geom\",\"SRSName\":\"EPSG:3067\",\"GMLVersion\":\"3.1.1\",\"WFSVersion\":\"1.1.0\",\"WMSLayerId\":null}";

    String paikkaSijainti = "<pnr:paikkaSijainti  xmlns:pnr=\"http://xml.nls.fi/Nimisto/Nimistorekisteri/2009/02\">"
            + "<gml:Point srsName=\"EPSG:3067\""
            + " xmlns:gml=\"http://www.opengis.net/gml\">"
            + " <gml:pos>384237.315 6682633.799</gml:pos> "
            + "</gml:Point>" + "</pnr:paikkaSijainti>";

    @BeforeClass
    public static void setUp() {
		JedisManager.connect(10, "localhost", 6379);
		layer = null;
		try {
			layer = WFSLayerStore.setJSON(json);
		} catch (IOException e) {
			fail("Should not throw exception");
		}
    }
    
	@Test
	public void test() {
		Parser parser = GMLParser32.getParser(layer);
		assertTrue("Should get valid parser", parser != null);
	}

    @Test
    public void testParserWithoutSchemaLocator() throws ParserConfigurationException, SAXException, IOException {
        Parser parser = GMLParser3.getParserWithoutSchemaLocator();
        StringReader r;
        Object obj;

        for (int n = 0; n < 1000; n++) {
            r = new StringReader(paikkaSijainti);
            obj = parser.parse(r);
            assertTrue(obj instanceof Geometry);
            assertTrue(obj instanceof Point);

            Point pt = (Point) obj;
            assertTrue(new Double(pt.getX()).equals(384237.315));
            assertTrue(new Double(pt.getY()).equals(6682633.799));
        }
    }
}
