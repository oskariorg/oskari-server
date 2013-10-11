package fi.nls.oskari.map.myplaces.service;

import fi.nls.oskari.domain.User;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.data.service.GetGeoPointDataService;
import fi.nls.oskari.map.myplaces.domain.ProxyRequest;
import fi.nls.oskari.permission.PermissionException;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.PropertyUtil;
import org.apache.xerces.parsers.DOMParser;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
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
import javax.xml.xpath.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
import java.util.zip.GZIPInputStream;

public class GeoServerProxyService {

    private final static Logger log = LogFactory.getLogger(GeoServerProxyService.class);
    private final static String WFS_INSERT = "wfs:Insert";
    private final static String WFS_DELETE = "wfs:Delete";
    private final static String WFS_UPDATE = "wfs:Update";
    
    private final static String FEATURE_UUID = "feature:uuid";
    private final static String OGC_FEATURE_ID = "ogc:FeatureId";
    private final static String FID = "fid";
    private final static String OGC_LITERAL = "ogc:Literal";
    
    private static final String MY_PLACE_FEATURE_FILTER_XML = "GetFeatureInfoMyPlaces.xml";
    private static final String MY_PLACE_FEATURE_FILTER_XSL = "GetFeatureInfoMyPlaces.xsl";
    

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
            

    public String proxy(final ProxyRequest request, final User user)
            throws MalformedURLException, IOException, PermissionException {

        // check that the users UUID matches the one in POST data XML
        if ("POST".equals(request.getMethod())) {
            final StringBuffer sb = new StringBuffer();
            sb.append("<?xml version=\"1.0\"?>\r\n");
            sb.append(request.getPostData());
            log.debug("Posted XML:", sb.toString());
            
            final String uuid = Jsoup.clean(getUUIDfromXml(sb), Whitelist.none());
            if (!uuid.equals(user.getUuid())) {
                throw new PermissionException("UUID didn't match with XML");
            }
        }

        final HttpURLConnection con = IOHelper.getConnection(request.getUrl() + request.getParamsAsQueryString(),
                request.getUserName(), request.getPassword());
        IOHelper.writeHeaders(con, request.getHeaders());

        try {
            if ("POST".equals(request.getMethod())) {
                /*
                con.setRequestMethod(request.getMethod());
                con.setDoOutput(true);
                con.setDoInput(true);
                */
                HttpURLConnection.setFollowRedirects(false);
                con.setUseCaches(false);
                con.setRequestProperty("Content-type", "text/xml; charset=UTF-8");
                //con.connect();
                IOHelper.writeToConnection(con, request.getPostData());

                GZIPInputStream gis = 
                        new GZIPInputStream(con.getInputStream());
                return IOHelper.readString(gis);
            } else {
                return IOHelper.readString(con.getInputStream());
            }
        } finally {
            con.disconnect();
        }
    }


    public String convertStreamToString(InputStream is) throws IOException {
        /*
         * To convert the InputStream to String we use the Reader.read(char[]
         * buffer) method. We iterate until the Reader return -1 which means
         * there's no more data to read. We use the StringWriter class to
         * produce the string.
         */
        if (is == null) {
            return "";
        }
        final Writer writer = new StringWriter();
        final char[] buffer = new char[1024];
        try {
            final Reader reader = new BufferedReader(new InputStreamReader(is,
                    "UTF-8"));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
        } finally {
            is.close();
        }
        return writer.toString();
    }


    private static String getUUIDfromXml(final StringBuffer sb)
            throws IOException {

        InputStream is = new ByteArrayInputStream(sb.toString().getBytes(
                "UTF-8"));
        InputSource inputSource = new InputSource(is);
        DOMParser p = new DOMParser();
        try {
            p.parse(inputSource);
        } catch (SAXException e) {
            throw new IOException("Error parsing XML", e);
        }
        Document doc = p.getDocument();

        String uuidInXml = "";

        NodeList insertCommandNode = doc.getElementsByTagName(WFS_INSERT);
        NodeList deleteCommandNode = doc.getElementsByTagName(WFS_DELETE);
        NodeList updateCommandNode = doc.getElementsByTagName(WFS_UPDATE);

        if (insertCommandNode.getLength() > 0) {
            NodeList uuidNode = doc.getElementsByTagName(FEATURE_UUID);
            uuidInXml = uuidNode.item(0).getTextContent();
            uuidInXml = Jsoup.clean(uuidInXml, Whitelist.none());
        } else if (deleteCommandNode.getLength() > 0
                || updateCommandNode.getLength() > 0) {
            NodeList featureIdNode = doc.getElementsByTagName(OGC_FEATURE_ID);

            String featureId = featureIdNode.item(0).getAttributes()
                    .getNamedItem(FID).getTextContent();
            featureId = Jsoup.clean(featureId, Whitelist.none());
            InputStream is2;
            try {
                is2 = getInputStreamFromGeoserver(featureId);
                uuidInXml = getUuidFromGeoserver(is2);
            } catch (SAXException e) {
                throw new IOException("Error parsing XML", e);
            }
            uuidInXml = Jsoup.clean(uuidInXml, Whitelist.none());

        } else if (doc.getElementsByTagName(OGC_LITERAL).getLength() > 0) {
            uuidInXml = doc.getElementsByTagName(OGC_LITERAL).item(0)
                    .getTextContent();
            uuidInXml = Jsoup.clean(uuidInXml, Whitelist.none());
        }

        return uuidInXml;
    }

    public static String getUuidFromGeoserver(InputStream is2)
            throws IOException, SAXException {
        String uuidInXml = "";
        DOMParser p2 = new DOMParser();
        InputSource inputSource2 = new InputSource(is2);
        p2.parse(inputSource2);
        Document docu = p2.getDocument();
        NodeList responseUUIDNode = docu.getElementsByTagName("ows:uuid");
        uuidInXml = responseUUIDNode.item(0).getTextContent();
        uuidInXml = Jsoup.clean(uuidInXml, Whitelist.none());
        return uuidInXml;
    }

    public static InputStream getInputStreamFromGeoserver(String featureId)
            throws IOException, SAXException {
        featureId = Jsoup.clean(featureId, Whitelist.none());
        int dotIdx = featureId.indexOf('.');
        if (dotIdx < 0)
            throw new RuntimeException("No type in '" + featureId + "'");
        String typeName = featureId.substring(0, dotIdx);
        String myPlacesUrl = PropertyUtil.get("myPlacesUrl");
        myPlacesUrl = Jsoup.clean(myPlacesUrl, Whitelist.none());
        String geoserverAddress = myPlacesUrl + "service=WFS&version=1.0.0"
                + "&request=GetFeature" + "&maxFeatures=50"
                + "&outputFormat=text/xml;%20subtype=gml/3.1.1"
                + "&typeName=ows:" + typeName + "&FEATUREID=" + featureId;

        URLConnection geoserverCon = getConnection(geoserverAddress);
        if (geoserverCon == null)
            throw new RuntimeException("Could not get connection"
                    + " to GeoServer");
        InputStream is2 = geoserverCon.getInputStream();

        return is2;
    }
    
    public JSONObject getFeatureInfo(final double lat, final double lon, final int zoom, final String id, final String uuid) {

        try {
            final String categoryId = id.substring(id.indexOf("_")+1);
            URLConnection connection = getConnection();
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-type", "application/xml");
            
            OutputStream outs = connection.getOutputStream();
            buildQueryToStream(MY_PLACE_FEATURE_FILTER_XML, lon +" " +lat, zoom, categoryId, uuid, outs);
            outs.flush();
            outs.close();
            
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(false);
            factory.setNamespaceAware(true);

            DocumentBuilder builder = factory.newDocumentBuilder();
            InputStream respInStream = connection.getInputStream();
            org.w3c.dom.Document document = builder.parse(respInStream);
            InputStream xsltInStream = this.getClass().getResourceAsStream(MY_PLACE_FEATURE_FILTER_XSL);
            StreamSource stylesource = new StreamSource(xsltInStream);
            
            String nof = document.getElementsByTagName("wfs:FeatureCollection").item(0).getAttributes().getNamedItem("numberOfFeatures").getTextContent(); 
            
            if (!"0".equals(nof)) {
                String transformedResponse = GetGeoPointDataService.getFormatedJSONString(document, stylesource);
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
        }
        return null;
    }
    
    private String buildQueryToStream(String resourceName,
            String lon_lat, int zoomLevel, String categoryId, String uuid, OutputStream outs)
            throws ParserConfigurationException, SAXException, IOException,
            XPathExpressionException, TransformerException {

        
        MyPlacesNamespaceContext nscontext = new MyPlacesNamespaceContext();
        /**
         * 1) Read Query Template
         */
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder;
        Document doc = null;
        builder = factory.newDocumentBuilder();

        InputStream inp = this.getClass().getResourceAsStream(resourceName);

        doc = builder.parse(inp);
        inp.close();

        // Create a XPathFactory
        XPathFactory xFactory = XPathFactory.newInstance();

        // Create a XPath object
        XPath xpath = xFactory.newXPath();
        xpath.setNamespaceContext(nscontext);
        XPath xpath2 = xFactory.newXPath();
        xpath2.setNamespaceContext(nscontext);
        XPath xpath3 = xFactory.newXPath();
        xpath3.setNamespaceContext(nscontext);
        XPath xpath4 = xFactory.newXPath();
        xpath4.setNamespaceContext(nscontext);
        
        // Compile the XPath expression
        XPathExpression expr = xpath
                .compile("//gml:pos[.='{LON_LAT}']");
        Node nd = (Node) expr.evaluate(doc, XPathConstants.NODE);
        nd.setTextContent(lon_lat);
        
        XPathExpression expr2 = xpath2
                .compile("//ogc:Literal[.='{CATEGORY_ID}']");
        Node nd2 = (Node) expr2.evaluate(doc, XPathConstants.NODE);
        nd2.setTextContent(categoryId);
        
        XPathExpression expr3 = xpath3
                .compile("//ogc:Literal[.='{UUID}']");
        Node nd3 = (Node) expr3.evaluate(doc, XPathConstants.NODE);
        nd3.setTextContent(uuid);
        
        XPathExpression expr4 = xpath4
                .compile("//ogc:Distance[.='{DISTANCE}']");
        Node nd4 = (Node) expr4.evaluate(doc, XPathConstants.NODE);
        nd4.setTextContent(String.valueOf(Math.pow(2, (12-zoomLevel))));
        
        
        // Use a Transformer for output
        TransformerFactory tFactory = TransformerFactory.newInstance();
        Transformer transformer = tFactory.newTransformer();

        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(outs);
        transformer.transform(source, result);
        
        return outs.toString();
    }
}
