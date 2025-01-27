package fi.nls.oskari.search;

import fi.mml.portti.service.search.SearchCriteria;
import fi.mml.portti.service.search.SearchResultItem;
import fi.nls.oskari.domain.geo.Point;
import fi.nls.oskari.search.channel.SearchChannel;
import fi.nls.test.util.ResourceHelper;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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
        Assertions.assertFalse(channel.isValidSearchTerm(sc), "Invalid search term: " + sc.getSearchString());
        sc.setSearchString("carting.pint.invent");
        Assertions.assertTrue(channel.isValidSearchTerm(sc), "Valid search term: " + sc.getSearchString());

    }

    @Test
    public void testParse()
            throws Exception {
        What3WordsSearchChannel channel = new What3WordsSearchChannel();
        JSONObject json = ResourceHelper.readJSONResource("What3Words-success.json", this);
        SearchResultItem item = channel.parseResult(json, "EPSG:3067");
        Assertions.assertEquals(item.getTitle(), "carting.pint.invent", "Title");
        Assertions.assertEquals(item.getLat(), 6675293.715526561, 0.1, "Lat");
        Assertions.assertEquals(item.getLon(), 385547.65760422836, 0.1, "Lon");
    }

    @Test
    public void testTransform() throws Exception {
        What3WordsSearchChannel channel = new What3WordsSearchChannel();
        Point p = channel.getServiceCoordinates(385547.65760422836, 6675293.715526561, "EPSG:3067");
        Assertions.assertEquals(60.19837505386789, p.getLat(), 0.1);
        Assertions.assertEquals(24.93546499999964, p.getLon(), 0.1);
    }
}