package fi.nls.oskari.search.channel;

import fi.mml.portti.service.search.ChannelSearchResult;
import fi.mml.portti.service.search.SearchCriteria;
import fi.mml.portti.service.search.SearchResultItem;
import fi.nls.oskari.util.PropertyUtil;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.*;

public class MetadataCatalogueChannelSearchServiceTest {
    private MetadataCatalogueChannelSearchService channel = null;

    private static String SERVER_URL = null;

    @BeforeClass
    public static void setup() {
        PropertyUtil.clearProperties();
        Properties properties = new Properties();
        try {
            properties.load(MetadataCatalogueChannelSearchServiceTest.class.getResourceAsStream("test.properties"));
            PropertyUtil.addProperties(properties);
        } catch (Exception e) {
            fail("Should not throw exception" + e.getStackTrace());
        }
        SERVER_URL = PropertyUtil.get("search.channel.METADATA_CATALOGUE_CHANNEL.metadata.catalogue.server");
    }

    @AfterClass
    public static void teardown() {
        PropertyUtil.clearProperties();
    }

    private MetadataCatalogueChannelSearchService getSearchChannel() {
        if(channel != null) {
            return channel;
        }
        channel = new MetadataCatalogueChannelSearchService();
        MetadataCatalogueChannelSearchService.resetProperties();
        channel.init();
        return channel;
    }


    @Test
    public void testSVServiceIdentificationParsing() throws Exception {

        StAXOMBuilder builder = getTestResponse("SV_ServiceIdentification.xml");
        assertTrue("We should have a results object", builder != null);

        ChannelSearchResult channelResult = getSearchChannel().parseResults(builder, getSearchCriteria("fi"));
        List<SearchResultItem> results = channelResult.getSearchResultItems();

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
            assertEquals("Resource Namespace mismatch", SERVER_URL, result.getResourceNameSpace());
        }
    }

    @Test
    public void testMDDataIdentification() throws Exception {
        StAXOMBuilder builder = getTestResponse("MD_DataIdentification.xml");
        assertTrue("We should have a results object", builder != null);

        final long start = System.currentTimeMillis();
        ChannelSearchResult channelResult =  getSearchChannel().parseResults(builder, getSearchCriteria("fi"));
        final long end = System.currentTimeMillis();
        System.out.println("Parse time:" + (end-start) + "ms");
        List<SearchResultItem> results = channelResult.getSearchResultItems();
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
            // FIXME: downloadable points to:
            // gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource/gmd:protocol
            // which was previously parsed as boolean but in test XML it's "OGC:WMS-1.1.1-http-get-map" so ignoring for now...
            //assertEquals("IsDownloadable mismatch", true, result.isDownloadable());
            assertEquals("GMD URL mismatch", "http://www.gee-em-dee.com", result.getGmdURL());
            assertEquals("Action URL mismatch", "fetchPageURL.fiUUIDUUID-UUID-UUID-UUID-UUIDUUIDUUID", result.getActionURL());
            assertEquals("Content URL mismatch", "imageURL.fiuuid=UUIDUUID-UUID-UUID-UUID-UUIDUUIDUUID&fname=taajama_vammala.png", result.getContentURL());
            assertEquals("UUID mismatch", "UUIDUUID-UUID-UUID-UUID-UUIDUUIDUUID", result.getResourceId());
            assertEquals("Resource Namespace mismatch", SERVER_URL, result.getResourceNameSpace());
        }
    }

    @Test
    public void testLocalizedTitle() throws Exception {

        StAXOMBuilder builderSV = getTestResponse("MD_DataIdentification.xml");
        StAXOMBuilder builderFI = getTestResponse("MD_DataIdentification.xml");

        ChannelSearchResult channelResultSV =  getSearchChannel().parseResults(builderSV, getSearchCriteria("sv"));
        ChannelSearchResult channelResultFI =  getSearchChannel().parseResults(builderFI, getSearchCriteria("fi"));
        List<SearchResultItem> resultsSV = channelResultSV.getSearchResultItems();
        List<SearchResultItem> resultsFI = channelResultFI.getSearchResultItems();

        assertEquals("Results size should be same:", resultsFI.size(), resultsSV.size());

        for (int i = 0, j = resultsFI.size(); i < j; i++) {

            SearchResultItem itemSV = resultsSV.get(i);
            SearchResultItem itemFI = resultsFI.get(i);
            assertTrue("Action url should be .fi", itemFI.getActionURL().startsWith("fetchPageURL.fi"));
            assertTrue("Action url should be .sv", itemSV.getActionURL().startsWith("fetchPageURL.sv"));

            assertEquals("Default language organization should match", "Title", itemFI.getTitle());
            assertEquals("SV language organization should match", "Title SV", itemSV.getTitle());
        }
    }

    @Test
    public void testSrvExtent() throws Exception {

        StAXOMBuilder builder = getTestResponse("SRV_extent.xml");
        assertTrue("We should have a results object", builder != null);

        ChannelSearchResult channelResult =  getSearchChannel().parseResults(builder, getSearchCriteria("fi"));
        List<SearchResultItem> results = channelResult.getSearchResultItems();

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
            assertEquals("Resource Namespace mismatch", SERVER_URL, result.getResourceNameSpace());
        }
    }

    private SearchCriteria getSearchCriteria(String locale) {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.setLocale(locale);
        return searchCriteria;
    }

    private StAXOMBuilder getTestResponse(String resource) throws Exception {
        InputStream inp = this.getClass().getResourceAsStream(resource);
        final StAXOMBuilder stAXOMBuilder = new StAXOMBuilder(inp);
        return stAXOMBuilder;
    }
}