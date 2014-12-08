package fi.nls.oskari.printout.ws.jaxrs.map;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
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

import fi.nls.oskari.printout.ws.ClientInfoSetup;
import fi.nls.oskari.printout.ws.ProxySetup;
import fi.nls.oskari.printout.ws.jaxrs.format.StreamingPDFImpl;
import fi.nls.oskari.printout.ws.jaxrs.format.StreamingPNGImpl;
import fi.nls.oskari.printout.ws.jaxrs.resource.MapResource;

/* 2nd generation tests - still valid */
public class WebServiceMapProducerResourceTest {

    final WebServiceMapProducerResourceTestRunner runner = new WebServiceMapProducerResourceTestRunner();
    final ClientInfoSetup clientInfo = new ClientInfoSetup("XX");

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
