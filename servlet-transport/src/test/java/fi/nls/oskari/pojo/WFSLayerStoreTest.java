package fi.nls.oskari.pojo;

import fi.nls.oskari.wfs.pojo.WFSLayerStore;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertTrue;

public class WFSLayerStoreTest {

	String json = "{\"layerId\":216,\"uiName\":\"Palvelupisteiden kyselypalvelu\",\"username\":\"\",\"password\":\"\",\"maxFeatures\":100,\"featureNamespace\":\"pkartta\",\"featureNamespaceURI\":\"www.pkartta.fi\",\"featureElement\":\"toimipaikat\",\"featureType\":{},\"selectedFeatureParams\":{\"default\": [\"nimi\", \"osoite\"]},\"featureParamsLocales\":{\"fi\": [\"nimi\", \"osoite\"]},\"geometryType\":\"2d\",\"getMapTiles\":true,\"getFeatureInfo\":true,\"tileRequest\":false,\"minScale\":50000.0,\"maxScale\":1.0,\"templateName\":null,\"templateDescription\":null,\"templateType\":null,\"requestTemplate\":null,\"responseTemplate\":null,\"selectionSLDStyle\":null,\"styles\":{\"default\":{\"id\":\"1\",\"name\":\"default\",\"SLDStyle\":\"<?xml version=\\\"1.0\\\" encoding=\\\"ISO-8859-1\\\"?><StyledLayerDescriptor version=\\\"1.0.0\\\" xmlns=\\\"http://www.opengis.net/sld\\\" xmlns:ogc=\\\"http://www.opengis.net/ogc\\\" xmlns:xlink=\\\"http://www.w3.org/1999/xlink\\\" xmlns:xsi=\\\"http://www.w3.org/2001/XMLSchema-instance\\\" xsi:schemaLocation=\\\"http://www.opengis.net/sld http://schemas.opengis.net/sld/1.0.0/StyledLayerDescriptor.xsd\\\"><NamedLayer><Name>Palvelupisteet</Name><UserStyle><Title>Palvelupisteiden tyyli</Title><Abstract/><FeatureTypeStyle><Rule><Title>Piste</Title><PointSymbolizer><Graphic><Mark><WellKnownName>circle</WellKnownName><Fill><CssParameter name=\\\"fill\\\">#FFFFFF</CssParameter></Fill><Stroke><CssParameter name=\\\"stroke\\\">#000000</CssParameter><CssParameter name=\\\"stroke-width\\\">2</CssParameter></Stroke></Mark><Size>12</Size></Graphic></PointSymbolizer></Rule></FeatureTypeStyle></UserStyle></NamedLayer></StyledLayerDescriptor>\"}},\"URL\":\"http://kartta.suomi.fi/geoserver/wfs\",\"GMLGeometryProperty\":\"the_geom\",\"SRSName\":\"EPSG:3067\",\"GMLVersion\":\"3.1.1\",\"WFSVersion\":\"1.1.0\",\"WMSLayerId\":null}";
	String jsonFail = "{\"layerId:216,\"username\":\"\",\"password\":\"\",\"maxFeatures\":100,\"featureNamespace\":\"pkartta\",\"featureNamespaceURI\":\"www.pkartta.fi\",\"featureElement\":\"toimipaikat\",\"featureType\":\"\",\"selectedFeatureParams\":[],\"featureParamsLocales\":{},\"geometryType\":\"2d\",\"getMapTiles\":true,\"getFeatureInfo\":true,\"tileRequest\":false,\"minScale\":50000.0,\"maxScale\":1.0,\"templateName\":null,\"templateDescription\":null,\"templateType\":null,\"requestTemplate\":null,\"responseTemplate\":null,\"selectionSLDStyle\":null,\"styles\":{\"default\":{\"id\":\"1\",\"name\":\"default\",\"SLDStyle\":\"<?xml version=\\\"1.0\\\" encoding=\\\"ISO-8859-1\\\"?><StyledLayerDescriptor version=\\\"1.0.0\\\" xmlns=\\\"http://www.opengis.net/sld\\\" xmlns:ogc=\\\"http://www.opengis.net/ogc\\\" xmlns:xlink=\\\"http://www.w3.org/1999/xlink\\\" xmlns:xsi=\\\"http://www.w3.org/2001/XMLSchema-instance\\\" xsi:schemaLocation=\\\"http://www.opengis.net/sld http://schemas.opengis.net/sld/1.0.0/StyledLayerDescriptor.xsd\\\"><NamedLayer><Name>Palvelupisteet</Name><UserStyle><Title>Palvelupisteiden tyyli</Title><Abstract/><FeatureTypeStyle><Rule><Title>Piste</Title><PointSymbolizer><Graphic><Mark><WellKnownName>circle</WellKnownName><Fill><CssParameter name=\\\"fill\\\">#FFFFFF</CssParameter></Fill><Stroke><CssParameter name=\\\"stroke\\\">#000000</CssParameter><CssParameter name=\\\"stroke-width\\\">2</CssParameter></Stroke></Mark><Size>12</Size></Graphic></PointSymbolizer></Rule></FeatureTypeStyle></UserStyle></NamedLayer></StyledLayerDescriptor>\"}},\"URL\":\"http://kartta.suomi.fi/geoserver/wfs\",\"GMLGeometryProperty\":\"the_geom\",\"SRSName\":\"EPSG:3067\",\"GMLVersion\":\"3.1.1\",\"WFSVersion\":\"1.1.0\",\"WMSLayerId\":null}";

	@Test
	public void testJSON() throws Exception {
        WFSLayerStore store = null;
        try {
		    store = WFSLayerStore.setJSON(json);
        } catch (Exception e) {
            assertTrue("Exception!", false); // fail;
        }

        // check the content as it should be
        assertTrue("should get layerId as 216", store.getLayerId().equals("216"));
        /*
        // FIXME: name locales aren't really used so maybe remove handling?
        JSONObject names = JSONHelper.createJSONObject(store.getNameLocales());
        Map<String, String> namesMap = JSONHelper.getObjectAsMap(names);
        assertTrue("should get nameLocales for 3 language", namesMap.size() == 3);
        */

        assertTrue("should get  'nimi' as 1.", store.getSelectedFeatureParams("default").get(0).equals("nimi"));
        assertTrue("should get  'osoite' as 2.", store.getSelectedFeatureParams("default").get(1).equals("osoite"));

        // returns default cause key was not found
        assertTrue("should get  'nimi' as 1.", store.getSelectedFeatureParams("lol").get(0).equals("nimi"));
        assertTrue("should get  'osoite' as 2.", store.getSelectedFeatureParams("lol").get(1).equals("osoite"));

        assertTrue("should get  'nimi' as 1.", store.getFeatureParamsLocales("fi").get(0).equals("nimi"));
        assertTrue("should get  'osoite' as 2.", store.getFeatureParamsLocales("fi").get(1).equals("osoite"));
	}
	
	@Test(expected=IOException.class)
	public void testJSONIOException() throws Exception {
		WFSLayerStore.setJSON(jsonFail);
	}

}
