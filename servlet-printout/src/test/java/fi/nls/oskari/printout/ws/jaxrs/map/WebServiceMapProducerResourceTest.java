package fi.nls.oskari.printout.ws.jaxrs.map;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import org.geowebcache.GeoWebCacheException;
import org.geowebcache.filter.request.RequestFilterException;
import org.junit.Before;
import org.junit.Test;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.operation.TransformException;

import fi.nls.oskari.printout.config.ConfigValue;
import fi.nls.oskari.printout.ws.ClientInfoSetup;
import fi.nls.oskari.printout.ws.ProxySetup;
import fi.nls.oskari.printout.ws.jaxrs.format.StreamingPDFImpl;
import fi.nls.oskari.printout.ws.jaxrs.format.StreamingPNGImpl;
import fi.nls.oskari.printout.ws.jaxrs.resource.MapResource;

/* 2nd generation tests - still valid */
public class WebServiceMapProducerResourceTest {

    final WebServiceMapProducerResourceTestRunner runner = new WebServiceMapProducerResourceTestRunner();
    final ClientInfoSetup clientInfo = new ClientInfoSetup(
            "XX"
            //"oskaristate=%7B%22currentViewId%22%3A%221%22%2C%22viewData%22%3A%7B%22mapfull%22%3A%7B%22state%22%3A%7B%22north%22%3A6885544%2C%22east%22%3A523072%2C%22zoom%22%3A1%2C%22srs%22%3A%22EPSG%3A3067%22%2C%22selectedLayers%22%3A%5B%7B%22id%22%3A%22base_35%22%2C%22opacity%22%3A100%2C%22style%22%3A%22default%22%7D%5D%2C%22plugins%22%3A%7B%22MainMapModuleMarkersPlugin%22%3A%7B%22markers%22%3A%5B%5D%7D%2C%22MainMapModuleManageStatsPlugin%22%3A%7B%22indicators%22%3A%5B%5D%2C%22layerId%22%3Anull%2C%22filterMethod%22%3A%22%22%2C%22cmode%22%3A%22%22%2C%22numberOfClasses%22%3A5%2C%22methodId%22%3A%221%22%2C%22filterInput%22%3A%5B%5D%2C%22filterRegion%22%3A%5B%5D%2C%22municipalities%22%3A%5B%5D%7D%7D%7D%7D%2C%22toolbar%22%3A%7B%22state%22%3A%7B%22selected%22%3A%7B%22id%22%3A%22select%22%2C%22group%22%3A%22default-basictools%22%7D%7D%7D%2C%22search%22%3A%7B%22state%22%3A%7B%7D%7D%2C%22layerselector2%22%3A%7B%22state%22%3A%7B%22tab%22%3A%22Aiheittain%22%2C%22filter%22%3A%22pelto%22%2C%22groups%22%3A%5B%5D%7D%7D%2C%22maplegend%22%3A%7B%22state%22%3A%7B%7D%7D%2C%22statsgrid%22%3A%7B%22state%22%3A%7B%22indicators%22%3A%5B%5D%2C%22layerId%22%3Anull%2C%22filterMethod%22%3A%22%22%2C%22cmode%22%3A%22%22%2C%22numberOfClasses%22%3A5%2C%22methodId%22%3A%221%22%2C%22filterInput%22%3A%5B%5D%2C%22filterRegion%22%3A%5B%5D%2C%22municipalities%22%3A%5B%5D%7D%7D%2C%22metadataflyout%22%3A%7B%7D%2C%22printout%22%3A%7B%22state%22%3A%7B%7D%7D%2C%22admin-layerrights%22%3A%7B%22state%22%3A%7B%7D%7D%7D%7D; LFR_SESSION_STATE_25529=1418029068812; JSESSIONID=D0FF2B40DE38339C6E48E271556BB284; COOKIE_SUPPORT=true; __utmt=1; GUEST_LANGUAGE_ID=fi_FI; __utmt=1; COMPANY_ID=10108; ID=6473694d302b2b72594f6b3d; PASSWORD=5971506b454f54324a727150417a66572f53516955773d3d; LOGIN=6a616e6e652e6b6f72686f6e656e406d61616e6d6974746175736c6169746f732e6669; SCREEN_NAME=51762f3749734c37446f413d; __utma=147817270.914057211.1418029040.1418029040.1418029040.1; __utmb=147817270.6.10.1418029040; __utmc=147817270; __utmz=147817270.1418029040.1.1.utmcsr=(direct)|utmccn=(direct)|utmcmd=(none); __utma=239348246.1787856735.1418028960.1418028960.1418028960.1; __utmb=239348246.5.10.1418028960; __utmc=239348246; __utmz=239348246.1418028960.1.1.utmcsr=(direct)|utmccn=(direct)|utmcmd=(none); Snoop_testi=1; Snoobisession_paikkatietoikkuna_fi=749749; Snoobi30minute_paikkatietoikkuna_fi=749749; SnoobiID=1350733379"
    );

    {
        runner.setClientInfo(clientInfo);
    }

    @Before
    public void setupProxy() throws IOException {

        new ProxySetup();

    }

    /*
     * ?ver=1.17&zoomLevel=2&coord=
     * 449600_7019344&mapLayers=base_35+100+default,519+79+&statsgrid=519+indicator52012total+1+7+++-NaN+2012+total,undefined+2012+total-seq,0,false&
     * showMarker
     * =false&forceCache=false&noSavedState=false&pageSize=A4&scaledWidth=200
     */

    @Test
    public void testMapLinkAsPNG() throws FactoryConfigurationError, Exception {

        Map<String, String> values = new HashMap<String, String>();
        values.put("VER", "1.17");
        values.put("ZOOMLEVEL", "2");
        values.put("COORD", "449600_7019344");
        values.put("MAPLAYERS", "base_35 100 default,519 79 ");
        values.put(
                "STATSGRID",
                "519+indicator52012total+1+7+++-NaN+2012+total,undefined+2012+total-seq,0,false");
        values.put("SHOWMARKER", "false");
        values.put("FORCECACHE", "false");
        values.put("NOSAVEDSTATE", "false");
        values.put("PAGESIZE", "A4");
        values.put("SCALEDWIDTH", "200");

        Properties props = getFixedTestProperties("jhs.properties",
                "layers.json");
        props.store(System.out, "");

        WebServiceMapProducerResource resource = new WebServiceMapProducerResource(
                props);
        String layersUrlFromProps = props.getProperty("layersURL");
        resource.setLayerJSONurl(new URL(layersUrlFromProps));
        resource.setLayersDirty(false);

        StreamingPNGImpl stream = resource.getMapPNG(values,
                clientInfo.getXClientInfo(props));
        stream.underflow();

        FileOutputStream outs = new FileOutputStream(
                "test-output/maplink-test-output.png");
        try {
            stream.write(outs);
        } finally {
            outs.close();
        }

    }

    /* %C3%A5%C3%A4%C3%B6%C3%A5%C3%A4%C3%B6ABC/// */
    @Test
    public void testMapLinkAsPDF() throws FactoryConfigurationError, Exception {

        Map<String, String> values = new HashMap<String, String>();
        values.put("VER", "1.17");
        values.put("ZOOMLEVEL", "2");
        values.put("COORD", "449600_7019344");
        values.put("MAPLAYERS", "base_35 100 default,519 79 ");
        values.put(
                "STATSGRID",
                "519+indicator52012total+1+7+++-NaN+2012+total,undefined+2012+total-seq,0,false");
        values.put("SHOWMARKER", "false");
        values.put("FORCECACHE", "false");
        values.put("NOSAVEDSTATE", "false");
        values.put("PAGESIZE", "A4");
        values.put("SCALEDWIDTH", "200");
        values.put("PAGETITLE", URLDecoder.decode(
                "%C3%A5%C3%A4%C3%B6%C3%A5%C3%A4%C3%B6ABC///", "UTF-8"));

        Properties props = getFixedTestProperties("jhs.properties",
                "layers.json");
        props.store(System.out, "");

        WebServiceMapProducerResource resource = new WebServiceMapProducerResource(
                props);
        String layersUrlFromProps = props.getProperty("layersURL");
        resource.setLayerJSONurl(new URL(layersUrlFromProps));
        resource.setLayersDirty(false);

        StreamingPDFImpl stream = resource.getMapPDF(values,
                clientInfo.getXClientInfo(props));
        stream.underflow();

        FileOutputStream outs = new FileOutputStream(
                "test-output/maplink-test-output.pdf");
        try {
            stream.write(outs);
        } finally {
            outs.close();
        }

    }

    @Test
    public void testGeojsPrintTestJsonAsPDF() throws FactoryConfigurationError,
            Exception {

        runner.setResource(WebServiceMapProducerResourceTest.acquire());
        runner.run("geojsPrintTest",
                WebServiceMapProducerResourceTestFileType.GEOJSON,
                WebServiceMapProducerResourceTestFileType.PDF);

    }

    @Test
    public void testGeojsPrintTestJsonAsPPTX()
            throws NoSuchAuthorityCodeException, IOException,
            GeoWebCacheException, FactoryException, ParseException,
            XMLStreamException, FactoryConfigurationError,
            RequestFilterException, TransformException,
            com.vividsolutions.jts.io.ParseException, InterruptedException,
            org.json.simple.parser.ParseException, URISyntaxException,
            Exception {
        runner.setResource(WebServiceMapProducerResourceTest.acquire());
        runner.run("geojsPrintTest-pptx",
                WebServiceMapProducerResourceTestFileType.GEOJSON,
                WebServiceMapProducerResourceTestFileType.PPTX);

    }

    @Test
    public void testGeojsPrintTestJsonAsPNG()
            throws NoSuchAuthorityCodeException, IOException,
            GeoWebCacheException, FactoryException, ParseException,
            XMLStreamException, FactoryConfigurationError,
            RequestFilterException, TransformException,
            com.vividsolutions.jts.io.ParseException,
            org.json.simple.parser.ParseException, URISyntaxException,
            InterruptedException, Exception {
        runner.setResource(WebServiceMapProducerResourceTest.acquire());

        runner.run("geojsPrintTest",
                WebServiceMapProducerResourceTestFileType.GEOJSON,
                WebServiceMapProducerResourceTestFileType.PNG);

    }

    @Test
    public void testGeojsPrintTest20130423JsonAsPDF()
            throws NoSuchAuthorityCodeException, IOException,
            GeoWebCacheException, FactoryException, ParseException,
            XMLStreamException, FactoryConfigurationError,
            RequestFilterException, TransformException,
            com.vividsolutions.jts.io.ParseException, InterruptedException,
            org.json.simple.parser.ParseException, URISyntaxException,
            Exception {
        runner.setResource(WebServiceMapProducerResourceTest.acquire());
        runner.run("geojsPrintTest20130423",
                WebServiceMapProducerResourceTestFileType.GEOJSON,
                WebServiceMapProducerResourceTestFileType.PDF);

    }

    @Test
    public void testGeojsPrintTest20130423JsonAsPNG()
            throws NoSuchAuthorityCodeException, IOException,
            GeoWebCacheException, FactoryException, ParseException,
            XMLStreamException, FactoryConfigurationError,
            RequestFilterException, TransformException,
            com.vividsolutions.jts.io.ParseException,
            org.json.simple.parser.ParseException, URISyntaxException,
            InterruptedException, Exception {
        runner.setResource(WebServiceMapProducerResourceTest.acquire());

        runner.run("geojsPrintTest20130423",
                WebServiceMapProducerResourceTestFileType.GEOJSON,
                WebServiceMapProducerResourceTestFileType.PNG);

    }

    @Test
    public void testStatjsPrintTestJsonAsPNG()
            throws NoSuchAuthorityCodeException, IOException,
            GeoWebCacheException, FactoryException, ParseException,
            XMLStreamException, FactoryConfigurationError,
            RequestFilterException, TransformException,
            com.vividsolutions.jts.io.ParseException,
            org.json.simple.parser.ParseException, URISyntaxException,
            InterruptedException, Exception {
        runner.setResource(WebServiceMapProducerResourceTest.acquire());

        runner.run("testStatLayerPrint",
                WebServiceMapProducerResourceTestFileType.GEOJSON,
                WebServiceMapProducerResourceTestFileType.PNG);

    }

    @Test
    public void testStatjsPrintTestJsonAsPDF()
            throws NoSuchAuthorityCodeException, IOException,
            GeoWebCacheException, FactoryException, ParseException,
            XMLStreamException, FactoryConfigurationError,
            RequestFilterException, TransformException,
            com.vividsolutions.jts.io.ParseException,
            org.json.simple.parser.ParseException, URISyntaxException,
            InterruptedException, Exception {
        runner.setResource(WebServiceMapProducerResourceTest.acquire());

        runner.run("testStatLayerPrint",
                WebServiceMapProducerResourceTestFileType.GEOJSON,
                WebServiceMapProducerResourceTestFileType.PDF);

    }

    @Test
    public void testGeojsPrintTestWithTilesJsonAsPNG()
            throws NoSuchAuthorityCodeException, IOException,
            GeoWebCacheException, FactoryException, ParseException,
            XMLStreamException, FactoryConfigurationError,
            RequestFilterException, TransformException,
            com.vividsolutions.jts.io.ParseException,
            org.json.simple.parser.ParseException, URISyntaxException,
            InterruptedException, Exception {
        runner.setResource(WebServiceMapProducerResourceTest.acquire());

        runner.run("geojsPrintTestWithTiles",
                WebServiceMapProducerResourceTestFileType.GEOJSON,
                WebServiceMapProducerResourceTestFileType.PNG);

    }

    @Test
    public void testGeojsLegendTestJsonAsPNG()
            throws NoSuchAuthorityCodeException, IOException,
            GeoWebCacheException, FactoryException, ParseException,
            XMLStreamException, FactoryConfigurationError,
            RequestFilterException, TransformException,
            com.vividsolutions.jts.io.ParseException,
            org.json.simple.parser.ParseException, URISyntaxException,
            InterruptedException, Exception {
        runner.setResource(WebServiceMapProducerResourceTest.acquire());

        runner.run("legend_test",
                WebServiceMapProducerResourceTestFileType.GEOJSON,
                WebServiceMapProducerResourceTestFileType.PNG);
    }

    @Test
    public void testWmtsPrintJsonAsPNG() throws NoSuchAuthorityCodeException,
            IOException, GeoWebCacheException, FactoryException,
            ParseException, XMLStreamException, FactoryConfigurationError,
            RequestFilterException, TransformException,
            com.vividsolutions.jts.io.ParseException, InterruptedException,
            URISyntaxException, org.json.simple.parser.ParseException {

        Properties props = getFixedTestProperties("jhs.properties",
                "layers.json");
        props.store(System.out, "");

        WebServiceMapProducerResource resource = new WebServiceMapProducerResource(
                props);
        String layersUrlFromProps = props.getProperty("layersURL");
        resource.setLayerJSONurl(new URL(layersUrlFromProps));
        resource.setLayersDirty(false);
        runner.setResource(resource);

        runner.run("testWmtsLayerPrint",
                WebServiceMapProducerResourceTestFileType.JSON,
                WebServiceMapProducerResourceTestFileType.PNG);

    }

    @Test
    public void testWmtsPrintZoom3JsonAsPNG()
            throws NoSuchAuthorityCodeException, IOException,
            GeoWebCacheException, FactoryException, ParseException,
            XMLStreamException, FactoryConfigurationError,
            RequestFilterException, TransformException,
            com.vividsolutions.jts.io.ParseException, InterruptedException,
            URISyntaxException, org.json.simple.parser.ParseException {

        Properties props = getFixedTestProperties("jhs.properties",
                "layers.json");
        props.store(System.out, "");

        WebServiceMapProducerResource resource = new WebServiceMapProducerResource(
                props);
        String layersUrlFromProps = props.getProperty("layersURL");
        resource.setLayerJSONurl(new URL(layersUrlFromProps));
        resource.setLayersDirty(false);
        runner.setResource(resource);

        runner.run("testWmtsLayerPrintZoom3",
                WebServiceMapProducerResourceTestFileType.JSON,
                WebServiceMapProducerResourceTestFileType.PNG);

    }

    @Test
    public void testWmtsPrintZoom7JsonAsPNG()
            throws NoSuchAuthorityCodeException, IOException,
            GeoWebCacheException, FactoryException, ParseException,
            XMLStreamException, FactoryConfigurationError,
            RequestFilterException, TransformException,
            com.vividsolutions.jts.io.ParseException, InterruptedException,
            URISyntaxException, org.json.simple.parser.ParseException {

        Properties props = getFixedTestProperties("jhs.properties",
                "layers.json");
        props.store(System.out, "");

        WebServiceMapProducerResource resource = new WebServiceMapProducerResource(
                props);
        String layersUrlFromProps = props.getProperty("layersURL");
        resource.setLayerJSONurl(new URL(layersUrlFromProps));
        resource.setLayersDirty(false);
        runner.setResource(resource);

        runner.run("testWmtsLayerPrintZoom7",
                WebServiceMapProducerResourceTestFileType.JSON,
                WebServiceMapProducerResourceTestFileType.PNG);

    }

    @Test
    public void testWmtsPrintZoom7png8opacityJsonAsPNG()
            throws NoSuchAuthorityCodeException, IOException,
            GeoWebCacheException, FactoryException, ParseException,
            XMLStreamException, FactoryConfigurationError,
            RequestFilterException, TransformException,
            com.vividsolutions.jts.io.ParseException, InterruptedException,
            URISyntaxException, org.json.simple.parser.ParseException {

        Properties props = getFixedTestProperties("jhs.properties",
                "layers.json");
        props.store(System.out, "");

        WebServiceMapProducerResource resource = new WebServiceMapProducerResource(
                props);
        String layersUrlFromProps = props.getProperty("layersURL");
        resource.setLayerJSONurl(new URL(layersUrlFromProps));
        resource.setLayersDirty(false);
        runner.setResource(resource);

        runner.run("testWmtsLayerPrintZoom7png8opacity",
                WebServiceMapProducerResourceTestFileType.GEOJSON,
                WebServiceMapProducerResourceTestFileType.PNG);

    }

    @Test
    public void testWmtsPrintZoom7jpegopacityJsonAsPNG()
            throws NoSuchAuthorityCodeException, IOException,
            GeoWebCacheException, FactoryException, ParseException,
            XMLStreamException, FactoryConfigurationError,
            RequestFilterException, TransformException,
            com.vividsolutions.jts.io.ParseException, InterruptedException,
            URISyntaxException, org.json.simple.parser.ParseException {

        Properties props = getFixedTestProperties("jhs.properties",
                "layers.json");
        props.store(System.out, "");

        WebServiceMapProducerResource resource = new WebServiceMapProducerResource(
                props);
        String layersUrlFromProps = props.getProperty("layersURL");
        resource.setLayerJSONurl(new URL(layersUrlFromProps));
        resource.setLayersDirty(false);
        runner.setResource(resource);

        runner.run("testWmtsLayerPrintZoom7jpegopacity",
                WebServiceMapProducerResourceTestFileType.GEOJSON,
                WebServiceMapProducerResourceTestFileType.PNG);

    }

    @Test
    public void testWmtsPrintZoom7png8opacityJsonAsPDF()
            throws NoSuchAuthorityCodeException, IOException,
            GeoWebCacheException, FactoryException, ParseException,
            XMLStreamException, FactoryConfigurationError,
            RequestFilterException, TransformException,
            com.vividsolutions.jts.io.ParseException, InterruptedException,
            URISyntaxException, org.json.simple.parser.ParseException {

        Properties props = getFixedTestProperties("jhs.properties",
                "layers.json");
        props.store(System.out, "");

        WebServiceMapProducerResource resource = new WebServiceMapProducerResource(
                props);
        String layersUrlFromProps = props.getProperty("layersURL");
        resource.setLayerJSONurl(new URL(layersUrlFromProps));
        resource.setLayersDirty(false);
        runner.setResource(resource);

        runner.run("testWmtsLayerPrintZoom7png8opacity",
                WebServiceMapProducerResourceTestFileType.GEOJSON,
                WebServiceMapProducerResourceTestFileType.PDF);

    }

    @Test
    public void testWmtsPrintZoom7png8opacityJsonAndTemplateAsPDF()
            throws NoSuchAuthorityCodeException, IOException,
            GeoWebCacheException, FactoryException, ParseException,
            XMLStreamException, FactoryConfigurationError,
            RequestFilterException, TransformException,
            com.vividsolutions.jts.io.ParseException, InterruptedException,
            URISyntaxException, org.json.simple.parser.ParseException {

        Properties props = getFixedTestProperties("jhs.properties",
                "layers.json");
        props.store(System.out, "");

        WebServiceMapProducerResource resource = new WebServiceMapProducerResource(
                props);
        String layersUrlFromProps = props.getProperty("layersURL");
        resource.setLayerJSONurl(new URL(layersUrlFromProps));
        resource.setLayersDirty(false);
        runner.setResource(resource);

        runner.run("testWmtsLayerPrintZoom7png8opacityAndTemplate",
                WebServiceMapProducerResourceTestFileType.GEOJSON,
                WebServiceMapProducerResourceTestFileType.PDF);

    }

    @Test
    public void testWmtsLayerPrintZoom7png8opacityAndTemplateWithTableContentAsPDF()
            throws NoSuchAuthorityCodeException, IOException,
            GeoWebCacheException, FactoryException, ParseException,
            XMLStreamException, FactoryConfigurationError,
            RequestFilterException, TransformException,
            com.vividsolutions.jts.io.ParseException, InterruptedException,
            URISyntaxException, org.json.simple.parser.ParseException {

        Properties props = getFixedTestProperties("jhs.properties",
                "layers.json");
        props.store(System.out, "");

        WebServiceMapProducerResource resource = new WebServiceMapProducerResource(
                props);
        String layersUrlFromProps = props.getProperty("layersURL");
        resource.setLayerJSONurl(new URL(layersUrlFromProps));
        resource.setLayersDirty(false);
        runner.setResource(resource);

        runner.run(
                "testWmtsLayerPrintZoom7png8opacityAndTemplateWithTableContent",
                WebServiceMapProducerResourceTestFileType.GEOJSON,
                WebServiceMapProducerResourceTestFileType.PDF);

    }

    @Test
    public void testLocalResourcePDF() throws NoSuchAuthorityCodeException,
            IOException, GeoWebCacheException, FactoryException,
            ParseException, XMLStreamException, FactoryConfigurationError,
            RequestFilterException, TransformException,
            com.vividsolutions.jts.io.ParseException, InterruptedException,
            URISyntaxException, org.json.simple.parser.ParseException {

        Properties props = getFixedTestProperties("jhs.properties",
                "layers.json");
        props.store(System.out, "");

        WebServiceMapProducerResource resource = new WebServiceMapProducerResource(
                props);
        String layersUrlFromProps = props.getProperty("layersURL");
        resource.setLayerJSONurl(new URL(layersUrlFromProps));
        resource.setLayersDirty(false);
        runner.setResource(resource);

        runner.run("testLocalResource",
                WebServiceMapProducerResourceTestFileType.GEOJSON,
                WebServiceMapProducerResourceTestFileType.PDF);

    }

    @Test
    public void testWmtsLayerPrintZoom7png8opacityAndTemplateWithPagedTableContentAsPDF()
            throws NoSuchAuthorityCodeException, IOException,
            GeoWebCacheException, FactoryException, ParseException,
            XMLStreamException, FactoryConfigurationError,
            RequestFilterException, TransformException,
            com.vividsolutions.jts.io.ParseException, InterruptedException,
            URISyntaxException, org.json.simple.parser.ParseException {

        Properties props = getFixedTestProperties("jhs.properties",
                "layers.json");
        props.store(System.out, "");

        WebServiceMapProducerResource resource = new WebServiceMapProducerResource(
                props);
        String layersUrlFromProps = props.getProperty("layersURL");
        resource.setLayerJSONurl(new URL(layersUrlFromProps));
        resource.setLayersDirty(false);
        runner.setResource(resource);

        runner.run(
                "testWmtsLayerPrintZoom7png8opacityAndTemplateWithPagedTableContent",
                WebServiceMapProducerResourceTestFileType.GEOJSON,
                WebServiceMapProducerResourceTestFileType.PDF);

    }

    /* FOR TESTING ONLY */
    /* synchronized for create on call only */
    static Object getmapResourceLock = new Object();
    static WebServiceMapProducerResource shared;

    public static WebServiceMapProducerResource acquire() throws Exception {
        synchronized (getmapResourceLock) {

            if (shared != null) {
                return shared;
            }

            Properties props = getFixedTestProperties("default.properties",
                    "layers.json");
            props.store(System.out, "");

            shared = new WebServiceMapProducerResource(props);

            String layersUrlFromProps = props.getProperty("layersURL");

            URL layerJSONurl = null;

            if (layersUrlFromProps != null) {
                layerJSONurl = new URL(layersUrlFromProps);
            } else {
                layerJSONurl = new URL("http://n.a");
                shared.setLayersDirty(false);
            }

            shared.setLayerJSONurl(layerJSONurl);

            try {
                shared.loadLayerJson();
            } catch (IOException ioe) {
                if (shared.getLayerJson() != null) {
                    /* we'll use the old one */
                } else {
                    throw ioe;
                }
            }

        }

        return shared;
    }

    public static Properties getFixedTestProperties(String propsName,
            String layersUrl) throws IOException {
        Properties props = new Properties();
        Reader r = new InputStreamReader(
                MapResource.class.getResourceAsStream(propsName));
        try {
            props.load(r);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            r.close();
        }

        String layersPropValue = WebServiceMapProducerResourceTest.class
                .getResource(layersUrl).toString();
        props.put("layersURL", layersPropValue);

        return props;
    }

}
