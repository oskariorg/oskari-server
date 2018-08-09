package fi.nls.oskari.search.channel;

import fi.mml.portti.service.search.ChannelSearchResult;
import fi.mml.portti.service.search.SearchCriteria;
import fi.mml.portti.service.search.SearchResultItem;
import org.junit.Test;
import static org.junit.Assert.*;

public class TM35LehtijakoSearchChannelTest {

    @Test
    public void testDoSearch() {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.setSearchString("U52");
        searchCriteria.setSRS("EPSG:3067");

        TM35LehtijakoSearchChannel instance = new TM35LehtijakoSearchChannel();
        ChannelSearchResult result = instance.doSearch(searchCriteria);
        SearchResultItem item = result.getSearchResultItems().get(0);

        assertEquals(548000.0d, Double.parseDouble(item.getLon()), 0.0d);
        assertEquals(7506000.0d, Double.parseDouble(item.getLat()), 0.0d);
    }
    @Test
    public void testDoSearchInvalidKeyword() {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.setSearchString("This is not a karttalehti");
        searchCriteria.setSRS("EPSG:3067");

        TM35LehtijakoSearchChannel instance = new TM35LehtijakoSearchChannel();
        ChannelSearchResult result = instance.doSearch(searchCriteria);
        assertTrue("Should not find anything", result.getSearchResultItems().isEmpty());

    }

    @Test
    public void testReverseGeocode() throws Exception {
        SearchCriteria searchCriteria = new SearchCriteria();
        //searchCriteria.setSRS("EPSG:3067");
        searchCriteria.setSRS("EPSG:4326");
        // Lat,Lon order
        //searchCriteria.setReverseGeocode(7506000.0d,548000.0d);
        searchCriteria.setReverseGeocode(67.66541141214373,28.131921318047343);
        searchCriteria.addParam("scale", "100000");

        TM35LehtijakoSearchChannel instance = new TM35LehtijakoSearchChannel();
        ChannelSearchResult result = instance.reverseGeocode(searchCriteria);
        SearchResultItem item = result.getSearchResultItems().get(0);

        assertEquals("source EPSG:4326", "U52", item.getTitle());

        searchCriteria.setSRS("EPSG:3857");
        // Lat,Lon order
        searchCriteria.setReverseGeocode(1.0348280867056236E7d,3131631.1561614675d);
        searchCriteria.addParam("scale", "100000");

        result = instance.reverseGeocode(searchCriteria);
        item = result.getSearchResultItems().get(0);

        assertEquals("source EPSG:3857", "U52", item.getTitle());

        searchCriteria.setSRS("EPSG:3067");
        // Lat,Lon order
        searchCriteria.setReverseGeocode(7506000.0d,548000.0d);
        searchCriteria.addParam("scale", "100000");

        result = instance.reverseGeocode(searchCriteria);
        item = result.getSearchResultItems().get(0);

        assertEquals("source EPSG:3067", "U52", item.getTitle());
    }

}
