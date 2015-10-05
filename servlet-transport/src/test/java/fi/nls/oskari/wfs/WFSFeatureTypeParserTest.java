package fi.nls.oskari.wfs;

import fi.nls.oskari.wfs.pojo.WFSLayerStore;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opengis.feature.simple.SimpleFeatureType;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class WFSFeatureTypeParserTest {
	private static WFSLayerStore layer;
	
	private static String layerJSON = "{\"layerId\":216,\"username\":\"\",\"password\":\"\",\"maxFeatures\":100,\"featureNamespace\":\"pkartta\",\"featureNamespaceURI\":\"www.pkartta.fi\",\"featureElement\":\"toimipaikat\",\"featureType\":{},\"selectedFeatureParams\":{},\"featureParamsLocales\":{},\"geometryType\":\"2d\",\"getMapTiles\":true,\"getFeatureInfo\":true,\"tileRequest\":false,\"minScale\":50000.0,\"maxScale\":1.0,\"templateName\":null,\"templateDescription\":null,\"templateType\":null,\"requestTemplate\":null,\"responseTemplate\":null,\"selectionSLDStyle\":null,\"styles\":{\"default\":{\"id\":\"1\",\"name\":\"default\",\"SLDStyle\":\"<?xml version=\\\"1.0\\\" encoding=\\\"ISO-8859-1\\\"?><StyledLayerDescriptor version=\\\"1.0.0\\\" xmlns=\\\"http://www.opengis.net/sld\\\" xmlns:ogc=\\\"http://www.opengis.net/ogc\\\" xmlns:xlink=\\\"http://www.w3.org/1999/xlink\\\" xmlns:xsi=\\\"http://www.w3.org/2001/XMLSchema-instance\\\" xsi:schemaLocation=\\\"http://www.opengis.net/sld http://schemas.opengis.net/sld/1.0.0/StyledLayerDescriptor.xsd\\\"><NamedLayer><Name>Palvelupisteet</Name><UserStyle><Title>Palvelupisteiden tyyli</Title><Abstract/><FeatureTypeStyle><Rule><Title>Piste</Title><PointSymbolizer><Graphic><Mark><WellKnownName>circle</WellKnownName><Fill><CssParameter name=\\\"fill\\\">#FFFFFF</CssParameter></Fill><Stroke><CssParameter name=\\\"stroke\\\">#000000</CssParameter><CssParameter name=\\\"stroke-width\\\">2</CssParameter></Stroke></Mark><Size>12</Size></Graphic></PointSymbolizer></Rule></FeatureTypeStyle></UserStyle></NamedLayer></StyledLayerDescriptor>\"}},\"URL\":\"http://kartta.suomi.fi/geoserver/wfs\",\"GMLGeometryProperty\":\"the_geom\",\"SRSName\":\"EPSG:3067\",\"GMLVersion\":\"3.1.1\",\"WFSVersion\":\"1.1.0\",\"WMSLayerId\":null}";
    
	@BeforeClass
    public static void setUp() {		
		try {
			layer = WFSLayerStore.setJSON(layerJSON);
		} catch (IOException e) {
			fail("Should not throw exception");
		}
    }
    
	@Test
	public void testParser() {
        // * in the config marks the default geometry
        // should contain whole schema or at least the selectedFeatureParams (+ GEOMETRY)
        layer.addFeatureType("default", "fi_nimi:String,fi_osoite:String,postinumero:String,*the_geom:Point");

        WFSFeatureTypeParser featureTypeParser = new WFSFeatureTypeParser(layer.getFeatureType());
        
        Map<String, SimpleFeatureType> featureTypes = featureTypeParser.parse();
		assertTrue("Should get valid default type", featureTypes.get("default") != null);
		
        Map<String, List<String>> propertyNames = featureTypeParser.getProperties();
		assertTrue("Should get valid default type's property names", propertyNames.get("default") != null);
	}
}
