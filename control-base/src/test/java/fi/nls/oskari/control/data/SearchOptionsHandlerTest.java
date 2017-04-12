package fi.nls.oskari.control.data;

import fi.nls.oskari.domain.User;
import fi.nls.oskari.search.channel.SearchChannel;
import fi.nls.oskari.search.channel.SearchableChannel;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.test.control.JSONActionRouteTest;
import org.apache.poi.hpsf.Property;
import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 * Created by SMAKINEN on 27.3.2017.
 */
public class SearchOptionsHandlerTest extends JSONActionRouteTest {

    @After
    public void tearDown() throws Exception {
        PropertyUtil.clearProperties();
    }
    private void mockCapabilities(SearchChannel channel, boolean hasTextualSearch) {
        if(hasTextualSearch) {
            doReturn(SearchChannel.Capabilities.TEXT).when(channel).getCapabilities();
        } else {
            doReturn(SearchChannel.Capabilities.COORD).when(channel).getCapabilities();
        }
    }

    @Test
    public void testShouldBeIncludedNonTextualSearch() throws Exception {
        SearchChannel channel = mock(SearchChannel.class);
        mockCapabilities(channel, false);
        SearchOptionsHandler handler = new SearchOptionsHandler();
        handler.init();
        assertFalse("Channel should not permitted if there's no textual capabilities", handler.shouldBeIncluded(channel, getGuestUser()));
    }
    @Test
    public void testShouldBeIncludedTextualSearch() throws Exception {
        SearchChannel channel = mock(SearchChannel.class);
        mockCapabilities(channel, true);
        doReturn(true).when(channel).hasPermission(getGuestUser());
        SearchOptionsHandler handler = new SearchOptionsHandler();
        handler.init();
        assertTrue("Channel should be permitted if there's textual capabilities, no blacklist and user is permitted", handler.shouldBeIncluded(channel, getGuestUser()));
    }

    @Test
    public void testShouldBeIncludedPermission() throws Exception {
        SearchChannel channel = mock(SearchChannel.class);

        mockCapabilities(channel, true);
        doReturn(false).when(channel).hasPermission(getGuestUser());
        doReturn(true).when(channel).hasPermission(getLoggedInUser());
        SearchOptionsHandler handler = new SearchOptionsHandler();
        handler.init();
        assertFalse("Channel should not permitted for guest", handler.shouldBeIncluded(channel, getGuestUser()));
        assertTrue("Channel should be permitted for user", handler.shouldBeIncluded(channel, getLoggedInUser()));
    }

    @Test
    public void testShouldBeIncludedBlacklist() throws Exception {
        SearchChannel channel = mock(SearchChannel.class);

        mockCapabilities(channel, true);
        doReturn(true).when(channel).hasPermission(any(User.class));
        doReturn("TestChannel").when(channel).getId();
        // blacklisted - should not be allowed
        PropertyUtil.addProperty("actionhandler.SearchOptions.blacklist", "TestChannel");

        SearchOptionsHandler handler = new SearchOptionsHandler();
        handler.init();
        assertFalse("Channel should not permitted for guest", handler.shouldBeIncluded(channel, getGuestUser()));
        assertFalse("Channel should be permitted for user", handler.shouldBeIncluded(channel, getLoggedInUser()));

        // clear blacklist - should be allowed
        PropertyUtil.clearProperties();
        handler = new SearchOptionsHandler();
        handler.init();
        assertTrue("Channel should not permitted for guest", handler.shouldBeIncluded(channel, getGuestUser()));
        assertTrue("Channel should be permitted for user", handler.shouldBeIncluded(channel, getLoggedInUser()));

    }

}