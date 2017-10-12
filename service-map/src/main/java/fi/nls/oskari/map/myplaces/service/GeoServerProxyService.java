package fi.nls.oskari.map.myplaces.service;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.data.service.GetGeoPointDataService;
import fi.nls.oskari.map.myplaces.domain.ProxyRequest;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.util.XmlHelper;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;

public class GeoServerProxyService {

    private final static Logger log = LogFactory.getLogger(GeoServerProxyService.class);

    private final static String WFS_FEATURECOLLECTION = "wfs:FeatureCollection";

    protected static final String MY_PLACE_FEATURE_FILTER_XML = "GetFeatureInfoMyPlaces.xml";
    private static final String MY_PLACE_FEATURE_FILTER_XSL = "GetFeatureInfoMyPlaces.xsl";
    private static final String POST_REQUEST = "POST";

    private static final int DISTANCE_FACTOR = 5;
    private static final int MAX_ZOOM_LEVEL = 12;

    private static HttpURLConnection getConnection() throws IOException {
        final String myPlacesUrl = PropertyUtil.get("myplaces.ows.url");
        return getConnection(myPlacesUrl);
    }

    private static HttpURLConnection getConnection(final String url) throws IOException {
        // myplaces needs geoserver auth
        final String myplacesUser = PropertyUtil.get("myplaces.user");
        final String myplacesUserPass = PropertyUtil.get("myplaces.password");
        return IOHelper.getConnection(url, myplacesUser, myplacesUserPass);
    }

    /**
     * Proxy without user check
     *
     * @param request
     * @return
     * @throws IOException
     */
    public String proxy(final ProxyRequest request)
            throws IOException {
        final HttpURLConnection con = IOHelper.getConnection(request.getUrl() + request.getParamsAsQueryString(),
                request.getUserName(), request.getPassword());
        IOHelper.writeHeaders(con, request.getHeaders());

        try {
            if (POST_REQUEST.equals(request.getMethod())) {
                HttpURLConnection.setFollowRedirects(false);
                con.setUseCaches(false);
                con.setRequestProperty("Content-type", "text/xml; charset=UTF-8");
                log.debug("Posted XML:", request.getPostData());
                IOHelper.writeToConnection(con, request.getPostData());
            }
            return IOHelper.readString(con);
        } finally {
            con.disconnect();
        }
    }

    public JSONObject getFeatureInfo(final double lat, final double lon, final int zoom, final String id, final String uuid, final String srs) {

        HttpURLConnection connection = null;
        InputStream respInStream = null;
        InputStream xsltInStream = null;
        try {
            final String categoryId = id.substring(id.indexOf("_") + 1);
            connection = getConnection();
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-type", "application/xml");

            OutputStream outs = connection.getOutputStream();
            buildQueryToStream(MY_PLACE_FEATURE_FILTER_XML, lon + " " + lat, zoom, categoryId, uuid, outs, srs);
            outs.flush();
            outs.close();

            DocumentBuilderFactory factory = XmlHelper.newDocumentBuilderFactory();
            factory.setNamespaceAware(true);

            DocumentBuilder builder = factory.newDocumentBuilder();
            log.debug("Got response for myplaces GFI:");
            respInStream = IOHelper.debugResponse(connection.getInputStream());

            org.w3c.dom.Document document = builder.parse(respInStream);
            xsltInStream = this.getClass().getResourceAsStream(MY_PLACE_FEATURE_FILTER_XSL);
            StreamSource stylesource = new StreamSource(xsltInStream);

            String nof = document.getElementsByTagName(WFS_FEATURECOLLECTION).item(0).getAttributes().getNamedItem("numberOfFeatures").getTextContent();

            if (!"0".equals(nof)) {
                String transformedResponse = GetGeoPointDataService.getFormattedJSONString(document, stylesource);
                JSONObject response = new JSONObject();
                response.put(GetGeoPointDataService.TYPE, "wmslayer");
                response.put(GetGeoPointDataService.LAYER_ID, id);
                response.put(GetGeoPointDataService.PRESENTATION_TYPE, "JSON");
                response.put(GetGeoPointDataService.CONTENT, new JSONObject(transformedResponse));
                return response;
            }

        } catch (IOException e) {
            log.error("IOException when trying do wfs query for my places", e);
        } catch (XPathExpressionException e) {
            log.error("XPathExpressionException when trying do wfs query for my places", e);
        } catch (ParserConfigurationException e) {
            log.error("ParserConfigurationException when trying do wfs query for my places", e);
        } catch (SAXException e) {
            log.error("SAXException when trying do wfs query for my places", e);
        } catch (TransformerException e) {
            log.error("TransformerException when trying do wfs query for my places", e);
        } catch (JSONException e) {
            log.error("JSONException when trying do wfs query for my places", e);
        } finally {
            IOHelper.close(respInStream);
            IOHelper.close(xsltInStream);
            if (connection != null) {
                connection.disconnect();
            }
        }
        return null;
    }

    protected String buildQueryToStream(String resourceName,
                                      String lon_lat, int zoomLevel, String categoryId, String uuid, OutputStream outs, final String srs)
            throws ParserConfigurationException, SAXException, IOException,
            XPathExpressionException, TransformerException {

        /**
         * 1) Read Query Template
         */
        DocumentBuilderFactory factory = XmlHelper.newDocumentBuilderFactory();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();

        InputStream inp = this.getClass().getResourceAsStream(resourceName);

        Document doc = builder.parse(inp);
        inp.close();

        // Create a XPathFactory
        XPathFactory xFactory = XPathFactory.newInstance();

        // Create a XPath object
        XPath xpath = xFactory.newXPath();
        xpath.setNamespaceContext(new MyPlacesNamespaceContext());

        // replace template placeholders with values
        setupTemplateValue(doc, xpath, "//gml:pos[.='{LON_LAT}']", lon_lat);
        setupTemplateValue(doc, xpath, "//ogc:Literal[.='{CATEGORY_ID}']", categoryId);
        setupTemplateValue(doc, xpath, "//ogc:Literal[.='{UUID}']", uuid);

        final String distanceForZoom = String.valueOf(DISTANCE_FACTOR * Math.pow(2, (MAX_ZOOM_LEVEL - zoomLevel)));
        setupTemplateValue(doc, xpath, "//ogc:Distance[.='{DISTANCE}']", distanceForZoom);

        setupSrsAttribute(doc, xpath, "//wfs:Query", srs);
        setupSrsAttribute(doc, xpath, "//gml:Point", srs);

        // gml:Point srsName="{SRSNAME}"


        // Use a Transformer for output
        TransformerFactory tFactory = XmlHelper.newTransformerFactory();
        Transformer transformer = tFactory.newTransformer();

        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(outs);
        transformer.transform(source, result);
        return outs.toString();
    }

    private void setupTemplateValue(Document doc, XPath xpath, String selector, String value) throws XPathExpressionException {
        XPathExpression expr = xpath.compile(selector);
        Node nd = (Node) expr.evaluate(doc, XPathConstants.NODE);
        nd.setTextContent(value);
    }
    private void setupSrsAttribute(Document doc, XPath xpath, String selector, String srs) throws XPathExpressionException {
        XPathExpression expr = xpath.compile(selector);
        Node nd = (Node) expr.evaluate(doc, XPathConstants.NODE);
        Node querySRS = nd.getAttributes().getNamedItem("srsName");
        querySRS.setNodeValue(srs);
    }
}