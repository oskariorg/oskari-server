package oskari.search.channel;

import fi.mml.portti.service.search.SearchCriteria;
import fi.nls.oskari.search.channel.SimpleAddressWFSSearchHandler;
import fi.nls.oskari.search.channel.WFSChannelHandler;
import fi.nls.oskari.wfs.WFSSearchChannelsConfiguration;
import fi.nls.test.util.ResourceHelper;
import org.json.JSONArray;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.xmlunit.diff.Diff;

import static fi.nls.test.util.XmlTestHelper.compareXML;

/**
 * These test cases might not make sense (or the filter impl).
 * But the tests have been added after the implementation so we can update the code and make sure it works the same way.
 * It's another conversation if the actual filters make sense...
 */
public class WFSChannelHandlerTest  {

    @Test
    public void createSimpleFilter() throws Exception {
        WFSChannelHandler handler = new WFSChannelHandler();
        SearchCriteria sc = getCriteria("testing");
        WFSSearchChannelsConfiguration config = getConfig("testParam");

        String filter = handler.createFilter(sc, config);

        String expected = ResourceHelper.readStringResource("WFSFilter-simple.xml", this);
        Diff xmlDiff = compareXML(filter, expected);
        Assertions.assertFalse(xmlDiff.hasDifferences(), "Should get expected simple request" + xmlDiff);
    }

    @Test
    public void createOrFilter() throws Exception {
        WFSChannelHandler handler = new WFSChannelHandler();
        SearchCriteria sc = getCriteria("testing");
        WFSSearchChannelsConfiguration config = getConfig("testParam", "anotherParam");

        String filter = handler.createFilter(sc, config);

        String expected = ResourceHelper.readStringResource("WFSFilter-multiple.xml", this);

        Diff xmlDiff = compareXML(filter, expected);
        Assertions.assertFalse(xmlDiff.hasDifferences(), "Should get expected simple request" + xmlDiff);
    }

    @Test
    public void createSimpleAddressFilterWithOneAttribute() throws Exception {
        WFSChannelHandler handler = new SimpleAddressWFSSearchHandler();
        SearchCriteria sc = getCriteria("testing");
        WFSSearchChannelsConfiguration config = getConfig("testParam");

        String filter = handler.createFilter(sc, config);

        String expected = ResourceHelper.readStringResource("WFSFilter-simple.xml", this);
        Diff xmlDiff = compareXML(filter, expected);
        Assertions.assertFalse(xmlDiff.hasDifferences(), "Should return same result as the normal handler for single search term" + xmlDiff);
    }

    @Test
    public void createSimpleAddressFilterWithTwoAttributesButSingleQueryTerm() throws Exception {
        WFSChannelHandler handler = new SimpleAddressWFSSearchHandler();
        SearchCriteria sc = getCriteria("testing");
        WFSSearchChannelsConfiguration config = getConfig("street", "number");

        String filter = handler.createFilter(sc, config);

        // this makes little sense, but it's how it's implemented.
        // adding the test to see if updating the filter writing will break it so testing this case as well
        String expected = ResourceHelper.readStringResource("WFSFilter-address-weird.xml", this);
        Diff xmlDiff = compareXML(filter, expected);
        Assertions.assertFalse(xmlDiff.hasDifferences(), "Should return same result as the normal handler for single search term" + xmlDiff);
    }

    @Test
    public void createAddressFilter() throws Exception {
        WFSChannelHandler handler = new SimpleAddressWFSSearchHandler();
        SearchCriteria sc = getCriteria("testing 2");
        WFSSearchChannelsConfiguration config = getConfig("street", "number");

        String filter = handler.createFilter(sc, config);
        System.out.println(filter);
        String expected = ResourceHelper.readStringResource("WFSFilter-address-proper.xml", this);
        Diff xmlDiff = compareXML(filter, expected);
        Assertions.assertFalse(xmlDiff.hasDifferences(), "Should return same result as the normal handler for single search term" + xmlDiff);
    }

    SearchCriteria getCriteria(String query) {
        SearchCriteria sc = new SearchCriteria();
        sc.setSearchString(query);
        return sc;
    }

    WFSSearchChannelsConfiguration getConfig(String... attributeNames) throws Exception {
        WFSSearchChannelsConfiguration config = new WFSSearchChannelsConfiguration();
        JSONArray params = new JSONArray(attributeNames);
        config.setParamsForSearch(params);
        return config;
    }
}