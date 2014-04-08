package fi.nls.oskari.search.channel;

import fi.mml.portti.service.search.ChannelSearchResult;
import fi.mml.portti.service.search.SearchCriteria;
import fi.mml.portti.service.search.SearchResultItem;
import fi.nls.oskari.util.PropertyUtil;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.junit.BeforeClass;
import org.junit.Test;
import java.io.InputStream;
import java.util.*;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public class MetadataCatalogueChannelSearchServiceTest {
    private MetadataCatalogueChannelSearchService channel = null;

    private static String SERVER_URL = null;

    @BeforeClass
    public static void setUp() {
        Properties properties = new Properties();
        try {
            properties.load(MetadataCatalogueChannelSearchServiceTest.class.getResourceAsStream("test.properties"));
            PropertyUtil.addProperties(properties);
        } catch (Exception e) {
            fail("Should not throw exception" + e.getStackTrace());
        }
        SERVER_URL = PropertyUtil.get("search.channel.METADATA_CATALOGUE_CHANNEL.metadata.catalogue.server");
    }

    private MetadataCatalogueChannelSearchService getSearchChannel() {
        if(channel != null) {
            return channel;
        }
        channel = new MetadataCatalogueChannelSearchService();
        channel.setProperty("fetchpage.url.fi", "fetchPageURL.fi");
        channel.setProperty("fetchpage.url.en", "fetchPageURL.en");
        channel.setProperty("fetchpage.url.sv", "fetchPageURL.sv");

        channel.setProperty("image.url.fi", "imageURL.fi");
        channel.setProperty("image.url.en", "imageURL.en");
        channel.setProperty("image.url.sv", "imageURL.sv");
        return channel;
    }


    @Test
    public void testSVServiceIdentificationParsing() throws Exception {

        StAXOMBuilder builder = getTestResponse("SV_ServiceIdentification.xml");
        assertTrue("We should have a results object", builder != null);

        ChannelSearchResult channelResult = getSearchChannel().parseResults(builder, getSearchCriteria().getLocale());
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

        ChannelSearchResult channelResult =  getSearchChannel().parseResults(builder, getSearchCriteria().getLocale());
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
            // which should is used as boolean but in test XML it's "OGC:WMS-1.1.1-http-get-map" so ignoring for now...
            //assertEquals("IsDownloadable mismatch", true, result.isDownloadable());
            assertEquals("GMD URL mismatch", "http://www.gee-em-dee.com", result.getGmdURL());
            assertEquals("Action URL mismatch", "fetchPageURL.fiUUIDUUID-UUID-UUID-UUID-UUIDUUIDUUID", result.getActionURL());
            assertEquals("Content URL mismatch", "imageURL.fiuuid=UUIDUUID-UUID-UUID-UUID-UUIDUUIDUUID&fname=taajama_vammala.png", result.getContentURL());
            assertEquals("UUID mismatch", "UUIDUUID-UUID-UUID-UUID-UUIDUUIDUUID", result.getResourceId());
            assertEquals("Resource Namespace mismatch", SERVER_URL, result.getResourceNameSpace());
        }
    }

    @Test
    public void testSrvExtent() throws Exception {

        StAXOMBuilder builder = getTestResponse("SRV_extent.xml");
        assertTrue("We should have a results object", builder != null);

        ChannelSearchResult channelResult =  getSearchChannel().parseResults(builder, getSearchCriteria().getLocale());
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

    private SearchCriteria getSearchCriteria() {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.setLocale("fi");
        return searchCriteria;
    }

    private StAXOMBuilder getTestResponse(String resource) throws Exception {
        InputStream inp = this.getClass().getResourceAsStream(resource);
        final StAXOMBuilder stAXOMBuilder = new StAXOMBuilder(inp);
        return stAXOMBuilder;
    }
}