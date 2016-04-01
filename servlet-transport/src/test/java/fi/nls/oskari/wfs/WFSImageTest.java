package fi.nls.oskari.wfs;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import fi.nls.oskari.cache.JedisManager;
import fi.nls.oskari.pojo.SessionStore;
import fi.nls.oskari.pojo.WFSCustomStyleStore;
import fi.nls.test.util.TestHelper;
import fi.nls.oskari.wfs.pojo.WFSLayerStore;
import org.geotools.feature.FeatureCollection;
import org.geotools.geometry.jts.WKTReader2;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

public class WFSImageTest {
	private static SessionStore session;
	private static WFSLayerStore layer;
	private static List<Double> bounds;
	private static FeatureCollection<SimpleFeatureType, SimpleFeature> features;
	
	
	private static String sessionJSON = "{\"client\":\"test\",\"session\":\"49E8CFEF9A310C76438952F8FCD9FF2D\",\"language\":\"fi\",\"browser\":\"mozilla\",\"browserVersion\":20,\"location\":{\"srs\":\"EPSG:3067\",\"bbox\":[509058.0,6858054.0,513578.0,6860174.0],\"zoom\":8},\"grid\":{\"rows\":4,\"columns\":6,\"bounds\":[[508928.0,6859776.0,509952.0,6860800.0],[509952.0,6859776.0,510976.0,6860800.0],[510976.0,6859776.0,512000.0,6860800.0],[512000.0,6859776.0,513024.0,6860800.0],[513024.0,6859776.0,514048.0,6860800.0],[514048.0,6859776.0,515072.0,6860800.0],[508928.0,6858752.0,509952.0,6859776.0],[509952.0,6858752.0,510976.0,6859776.0],[510976.0,6858752.0,512000.0,6859776.0],[512000.0,6858752.0,513024.0,6859776.0],[513024.0,6858752.0,514048.0,6859776.0],[514048.0,6858752.0,515072.0,6859776.0],[508928.0,6857728.0,509952.0,6858752.0],[509952.0,6857728.0,510976.0,6858752.0],[510976.0,6857728.0,512000.0,6858752.0],[512000.0,6857728.0,513024.0,6858752.0],[513024.0,6857728.0,514048.0,6858752.0],[514048.0,6857728.0,515072.0,6858752.0],[508928.0,6856704.0,509952.0,6857728.0],[509952.0,6856704.0,510976.0,6857728.0],[510976.0,6856704.0,512000.0,6857728.0],[512000.0,6856704.0,513024.0,6857728.0],[513024.0,6856704.0,514048.0,6857728.0],[514048.0,6856704.0,515072.0,6857728.0]]},\"tileSize\":{\"width\":256,\"height\":256},\"mapSize\":{\"width\":1130,\"height\":530},\"mapScales\":[5669294.4,2834647.2,1417323.6,566929.44,283464.72,141732.36,56692.944,28346.472,11338.5888,5669.2944,2834.6472,1417.3236,708.6618],\"layers\":{\"216\":{\"id\":216,\"styleName\":\"default\",\"visible\":true},\"134\":{\"id\":134,\"styleName\":\"default\",\"visible\":true}}}";
    private static String layerJSON = "{\"layerId\":216,\"username\":\"\",\"password\":\"\",\"maxFeatures\":100,\"featureNamespace\":\"pkartta\",\"featureNamespaceURI\":\"www.pkartta.fi\",\"featureElement\":\"toimipaikat\",\"featureType\":\"\",\"selectedFeatureParams\":[],\"featureParamsLocales\":{},\"geometryType\":\"2d\",\"getMapTiles\":true,\"getFeatureInfo\":true,\"tileRequest\":false,\"minScale\":50000.0,\"maxScale\":1.0,\"templateName\":null,\"templateDescription\":null,\"templateType\":null,\"requestTemplate\":null,\"responseTemplate\":null,\"selectionSLDStyle\":null,\"styles\":{\"default\":{\"id\":\"1\",\"name\":\"default\",\"SLDStyle\":\"<?xml version=\\\"1.0\\\" encoding=\\\"ISO-8859-1\\\"?><StyledLayerDescriptor version=\\\"1.0.0\\\" xmlns=\\\"http://www.opengis.net/sld\\\" xmlns:ogc=\\\"http://www.opengis.net/ogc\\\" xmlns:xlink=\\\"http://www.w3.org/1999/xlink\\\" xmlns:xsi=\\\"http://www.w3.org/2001/XMLSchema-instance\\\" xsi:schemaLocation=\\\"http://www.opengis.net/sld http://schemas.opengis.net/sld/1.0.0/StyledLayerDescriptor.xsd\\\"><NamedLayer><Name>Palvelupisteet</Name><UserStyle><Title>Palvelupisteiden tyyli</Title><FeatureTypeStyle><Rule><Title>Piste</Title><PointSymbolizer><Graphic><Mark><WellKnownName>circle</WellKnownName><Fill><CssParameter name=\\\"fill\\\">#FFFFFF</CssParameter></Fill><Stroke><CssParameter name=\\\"stroke\\\">#000000</CssParameter><CssParameter name=\\\"stroke-width\\\">2</CssParameter></Stroke></Mark><Size>12</Size></Graphic></PointSymbolizer></Rule></FeatureTypeStyle></UserStyle></NamedLayer></StyledLayerDescriptor>\"}},\"URL\":\"http://kartta.suomi.fi/geoserver/wfs\",\"GMLGeometryProperty\":\"shape\",\"SRSName\":\"EPSG:3067\",\"GMLVersion\":\"3.1.1\",\"WFSVersion\":\"1.1.0\",\"WMSLayerId\":null}";
    private static String custom216JSON = "{\"client\":\"test\",\"layerId\":\"216\",\"fillColor\":\"#ffde00\",\"fillPattern\":-1,\"borderColor\":\"#000000\",\"borderLinejoin\":\"mitre\",\"borderDasharray\":\"\",\"borderWidth\":1,\"strokeLinecap\":\"butt\",\"strokeColor\":\"#3233ff\",\"strokeLinejoin\":\"mitre\",\"strokeDasharray\":\"\",\"strokeWidth\":1,\"dotColor\":\"#000000\",\"dotShape\":1,\"dotSize\":3,\"geometry\":\"shape\"}";

    @BeforeClass
    public static void setUp() {
		JedisManager.connect(10, "localhost", 6379);
		
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
		
		
		String types = "location:String,symbol:Integer,shape:Point";
		List<List<Object>> values = new ArrayList<List<Object>>();
		WKTReader2 wkt = new WKTReader2();
		List<Object> item = new ArrayList<Object>();
		item.add("Place1");
		item.add(1);
        Geometry geom = null;
		try {
			geom = wkt.read("POINT(" + 
					388800 + " " + 
					6695267 + ")");
		} catch (ParseException e) {
			fail("Should not throw exception");
		}
		item.add(geom);
		values.add(item);
		item = new ArrayList<Object>();
		item.add("Place2");
		item.add(2);
		try {
			geom = wkt.read("POINT(" + 
					392380 + " " + 
					6696397 + ")");
		} catch (ParseException e) {
			fail("Should not throw exception");
		}
		item.add(geom);
		values.add(item);

		features = WFSParser.dataToSimpleFeatures(types, values);
    }
    
	@Test
	public void testMap() {
		WFSImage mapImage = new WFSImage(layer,
                "test",
	    		session.getLayers().get(layer.getLayerId()).getStyleName(),
                null);
		
		BufferedImage bufferedMapImage = mapImage.draw(session.getTileSize(),
                session.getLocation(),
                features);
		assertTrue("Should get image", bufferedMapImage != null);
	}
  
	@Test
	public void testTile() {
        // check that we have redis connectivity (redis server running)
        assumeTrue(TestHelper.redisAvailable());
		WFSImage tileImage = new WFSImage(layer,
                "test",
	    		session.getLayers().get(layer.getLayerId()).getStyleName(),
                null);
		
		BufferedImage bufferedTileImage = tileImage.draw(session.getTileSize(),
                session.getLocation(),
                bounds,
                features);
		assertTrue("Should get image", bufferedTileImage != null);
		
 		Double[] bbox = new Double[4];
 		for (int i = 0; i < bbox.length; i++) {
 			bbox[i] = bounds.get(i);
 		}
		WFSImage.setCache(
        		bufferedTileImage, 
        		layer.getLayerId(),
                session.getLayers().get(layer.getLayerId()).getStyleName(),
	    		layer.getSRSName(), 
	    		bbox,
	    		session.getLocation().getZoom(),
	    		false
		);
		BufferedImage cachedImage = WFSImage.getCache(
                layer.getLayerId(),
                session.getLayers().get(layer.getLayerId()).getStyleName(),
                layer.getSRSName(),
                bbox,
                session.getLocation().getZoom(),
                false
        );
		byte[] byteTileImage = WFSImage.imageToBytes(bufferedTileImage);
		byte[] byteCachedImage = WFSImage.imageToBytes(cachedImage);
		
		assertTrue("Should get same image data", Arrays.equals(byteTileImage, byteCachedImage));
	}

    @Test
    public void testCustomTile() {
        // check that we have redis connectivity (redis server running)
        assumeTrue(TestHelper.redisAvailable());
        WFSCustomStyleStore customStyle = null;

        try {
            customStyle = WFSCustomStyleStore.setJSON(custom216JSON);
            customStyle.save();
        } catch (IOException e) {
            fail("Should not throw exception");
        }

        WFSImage tileImage = new WFSImage(layer,
                session.getClient(),
                WFSImage.PREFIX_CUSTOM_STYLE,
                null);

        BufferedImage bufferedTileImage = tileImage.draw(session.getTileSize(),
                session.getLocation(),
                bounds,
                features);
        assertTrue("Should get image", bufferedTileImage != null);
    }
}
