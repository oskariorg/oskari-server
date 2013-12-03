package fi.nls.oskari.search.channel;

import fi.mml.portti.service.search.SearchCriteria;
import fi.mml.portti.service.search.SearchResultItem;
import fi.nls.oskari.util.PropertyUtil;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public class MetadataCatalogueChannelSearchServiceTest {

    @BeforeClass
    public static void setUp() {
        Properties properties = new Properties();
        try {
            properties.load(MetadataCatalogueChannelSearchServiceTest.class.getResourceAsStream("test.properties"));
            PropertyUtil.addProperties(properties);
        } catch (Exception e) {
            fail("Should not throw exception" + e.getStackTrace());
        }
    }

    @Test
    public void testSVServiceIdentificationParsing() throws IOException, SAXException, ParserConfigurationException, XPathExpressionException {

        Node rootNode = getRootNode("SV_ServiceIdentification.xml");

        assertTrue("We should have a results object", rootNode != null);

        String serverURL = "serverURL.com";

        List<SearchResultItem> results =  MetadataCatalogueChannelSearchService.parseSearchResultItems(rootNode, getSearchCriteria(), getFetchPageURLs(), getImageURLs(), serverURL, getLocales("fi"));
        // enable later...
        assertTrue("Result count should match: " + results.size() + "/" + 1, results.size() == 1);
        SearchResultItem oldItem, result;
        // change back to oldResults.size() once you get the same amount...
        for (int i = 0, j = results.size(); i < j; i++) {
            result = results.get(i);
            assertEquals("Title mismatch", "Title", result.getTitle());
            assertEquals("Description mismatch", "Abstract text.", result.getDescription());
            assertEquals("Westbound longitude mismatch", null, result.getWestBoundLongitude());
            assertEquals("Southbound latitude mismatch", null, result.getSouthBoundLatitude());
            assertEquals("Eastbound longitude mismatch", null, result.getEastBoundLongitude());
            assertEquals("Northbound latitude mismatch", null, result.getNorthBoundLatitude());
            assertEquals("IsDownloadable mismatch", false, result.isDownloadable());
            assertEquals("GMD URL mismatch", "http://www.gee-em-dee.com", result.getGmdURL());
            assertEquals("Action URL mismatch", "fetchPageURL.fiUUIDUUID-UUID-UUID-UUID-UUIDUUIDUUID", result.getActionURL());
            assertEquals("Content URL mismatch", null, result.getContentURL());
            assertEquals("UUID mismatch", "UUIDUUID-UUID-UUID-UUID-UUIDUUIDUUID", result.getResourceId());
            assertEquals("Resource Namespace mismatch", serverURL, result.getResourceNameSpace());
        }
    }

    @Test
    public void testMDDataIdentification() throws IOException, SAXException, ParserConfigurationException, XPathExpressionException {

        Node rootNode = getRootNode("MD_DataIdentification.xml");

        assertTrue("We should have a results object", rootNode != null);

        String serverURL = "serverURL.com";

        List<SearchResultItem> results =  MetadataCatalogueChannelSearchService.parseSearchResultItems(rootNode, getSearchCriteria(), getFetchPageURLs(), getImageURLs(), serverURL, getLocales("fi"));
        // enable later...
        assertTrue("Result count should match: " + results.size() + "/" + 1, results.size() == 1);
        SearchResultItem oldItem, result;
        // change back to oldResults.size() once you get the same amount...
        for (int i = 0, j = results.size(); i < j; i++) {
            result = results.get(i);
            assertEquals("Title mismatch", "Title", result.getTitle());
            assertEquals("Description mismatch", "Abstract text.", result.getDescription());
            assertEquals("Westbound longitude mismatch", "19.08317359", result.getWestBoundLongitude());
            assertEquals("Southbound latitude mismatch", "59.45414258", result.getSouthBoundLatitude());
            assertEquals("Eastbound longitude mismatch", "31.58672881", result.getEastBoundLongitude());
            assertEquals("Northbound latitude mismatch", "70.09229553", result.getNorthBoundLatitude());
            assertEquals("IsDownloadable mismatch", true, result.isDownloadable());
            assertEquals("GMD URL mismatch", "http://www.gee-em-dee.com", result.getGmdURL());
            assertEquals("Action URL mismatch", "fetchPageURL.fiUUIDUUID-UUID-UUID-UUID-UUIDUUIDUUID", result.getActionURL());
            assertEquals("Content URL mismatch", "imageURL.fiuuid=UUIDUUID-UUID-UUID-UUID-UUIDUUIDUUID&fname=taajama_vammala.png", result.getContentURL());
            assertEquals("UUID mismatch", "UUIDUUID-UUID-UUID-UUID-UUIDUUIDUUID", result.getResourceId());
            assertEquals("Resource Namespace mismatch", serverURL, result.getResourceNameSpace());
        }
    }

    @Test
    public void testSrvExtent() throws IOException, SAXException, ParserConfigurationException, XPathExpressionException {

        Node rootNode = getRootNode("SRV_extent.xml");

        assertTrue("We should have a results object", rootNode != null);

        String serverURL = "serverURL.com";

        List<SearchResultItem> results =  MetadataCatalogueChannelSearchService.parseSearchResultItems(rootNode, getSearchCriteria(), getFetchPageURLs(), getImageURLs(), serverURL, getLocales("fi"));
        // enable later...
        assertTrue("Result count should match: " + results.size() + "/" + 1, results.size() == 1);
        SearchResultItem oldItem, result;
        // change back to oldResults.size() once you get the same amount...
        for (int i = 0, j = results.size(); i < j; i++) {
            result = results.get(i);
            assertEquals("Title mismatch", "Title", result.getTitle());
            assertEquals("Description mismatch", "Abstract text.", result.getDescription());
            assertEquals("Westbound longitude mismatch", "19.08317359", result.getWestBoundLongitude());
            assertEquals("Southbound latitude mismatch", "59.45414258", result.getSouthBoundLatitude());
            assertEquals("Eastbound longitude mismatch", "31.58672881", result.getEastBoundLongitude());
            assertEquals("Northbound latitude mismatch", "70.09229553", result.getNorthBoundLatitude());
            assertEquals("IsDownloadable mismatch", false, result.isDownloadable());
            assertEquals("GMD URL mismatch", "http://www.gee-em-dee.com", result.getGmdURL());
            assertEquals("Action URL mismatch", "fetchPageURL.fiUUIDUUID-UUID-UUID-UUID-UUIDUUIDUUID", result.getActionURL());
            assertEquals("Content URL mismatch", null, result.getContentURL());
            assertEquals("UUID mismatch", "UUIDUUID-UUID-UUID-UUID-UUIDUUIDUUID", result.getResourceId());
            assertEquals("Resource Namespace mismatch", serverURL, result.getResourceNameSpace());
        }
    }

    private List<String> getLocales(String locale) {
        List<String> locales = new ArrayList<String>();
        locales.add(locale);
        if (!"fi".matches(locale)) {
            locales.add("fi");
        } else if (!"en".matches(locale)) {
            locales.add("en");
        } else if (!"sv".matches(locale)) {
            locales.add("sv");
        }
        return locales;
    }

    private SearchCriteria getSearchCriteria() {
        List<String> props = PropertyUtil.getMatchingPropertyNames(".*");
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.setLocale("fi");
        return searchCriteria;
    }

    private Map<String, String> getFetchPageURLs() {
        Map<String, String> fetchPageURLs = new HashMap<String, String>();
        fetchPageURLs.put("en", "fetchPageURL.en");
        fetchPageURLs.put("fi", "fetchPageURL.fi");
        fetchPageURLs.put("sv", "fetchPageURL.sv");
        return fetchPageURLs;
    }

    private Map<String, String> getImageURLs() {
        Map<String, String> imageURLs = new HashMap<String, String>();
        imageURLs.put("en", "imageURL.en");
        imageURLs.put("fi", "imageURL.fi");
        imageURLs.put("sv", "imageURL.sv");
        return imageURLs;
    }

    private Node getRootNode(String resource) throws ParserConfigurationException, IOException, SAXException {
        InputStream inp = this.getClass().getResourceAsStream(resource);

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setNamespaceAware(true);
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

        Document doc = dBuilder.parse(inp);
        NodeList rootNodes = doc.getDocumentElement().getChildNodes();
        Node rootNode = null, tempNode;

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < rootNodes.getLength(); i++) {
            tempNode = rootNodes.item(i);
            sb.append(tempNode.getLocalName() + ", ");
            if ("SearchResults".equals(tempNode.getLocalName())) {
                rootNode = tempNode;
                break;
            }
        }
        return rootNode;
    }
}