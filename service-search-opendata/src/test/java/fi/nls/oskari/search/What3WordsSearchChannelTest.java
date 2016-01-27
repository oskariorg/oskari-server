package fi.nls.oskari.search;

import fi.mml.portti.service.search.SearchCriteria;
import fi.mml.portti.service.search.SearchResultItem;
import fi.nls.oskari.search.channel.SearchChannel;
import fi.nls.test.util.ResourceHelper;
import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by SMAKINEN on 26.1.2016.
 */
public class What3WordsSearchChannelTest {

    @Test
    public void testIsValidSearchTerm()
            throws Exception {
        SearchChannel channel = new What3WordsSearchChannel();
        SearchCriteria sc = new SearchCriteria();
        sc.setSearchString("asdf");
        assertFalse("Invalid search term: " + sc.getSearchString(), channel.isValidSearchTerm(sc));
        sc.setSearchString("carting.pint.invent");
        assertTrue("Valid search term: " + sc.getSearchString(), channel.isValidSearchTerm(sc));

    }

    @Test
    public void testParse()
            throws Exception {
        What3WordsSearchChannel channel = new What3WordsSearchChannel();
        JSONObject json = ResourceHelper.readJSONResource("What3Words-success.json", this);
        SearchResultItem item = channel.parseResult(json, "EPSG:3067");
        assertEquals("Title", item.getTitle(), "carting.pint.invent");
        assertEquals("Lon", item.getLon(), "385547.65760422836");
        assertEquals("Lat", item.getLat(), "6675293.715526561");
    }
}