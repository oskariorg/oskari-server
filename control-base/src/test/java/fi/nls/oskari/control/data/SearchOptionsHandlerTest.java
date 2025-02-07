package fi.nls.oskari.control.data;

import fi.mml.portti.service.search.SearchService;
import org.oskari.user.User;
import fi.nls.oskari.search.channel.SearchChannel;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.test.control.JSONActionRouteTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 * Created by SMAKINEN on 27.3.2017.
 */
public class SearchOptionsHandlerTest extends JSONActionRouteTest {

    @AfterEach
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

    private SearchOptionsHandler getHandler() {
        SearchOptionsHandler handler = new SearchOptionsHandler();
        handler.setSearchService(mock(SearchService.class));
        handler.init();
        return handler;
    }

    @Test
    public void testShouldBeIncludedNonTextualSearch() throws Exception {
        SearchChannel channel = mock(SearchChannel.class);
        mockCapabilities(channel, false);
        SearchOptionsHandler handler = getHandler();
        Assertions.assertFalse(handler.shouldBeIncluded(channel, getGuestUser()), "Channel should not permitted if there's no textual capabilities");
    }
    @Test
    public void testShouldBeIncludedTextualSearch() throws Exception {
        SearchChannel channel = mock(SearchChannel.class);
        mockCapabilities(channel, true);
        doReturn(true).when(channel).hasPermission(getGuestUser());
        SearchOptionsHandler handler = getHandler();
        Assertions.assertTrue(handler.shouldBeIncluded(channel, getGuestUser()), "Channel should be permitted if there's textual capabilities, no blacklist and user is permitted");
    }

    @Test
    public void testShouldBeIncludedPermission() throws Exception {
        SearchChannel channel = mock(SearchChannel.class);

        mockCapabilities(channel, true);
        doReturn(false).when(channel).hasPermission(getGuestUser());
        doReturn(true).when(channel).hasPermission(getLoggedInUser());
        SearchOptionsHandler handler = getHandler();
        Assertions.assertFalse(handler.shouldBeIncluded(channel, getGuestUser()), "Channel should not permitted for guest");
        Assertions.assertTrue(handler.shouldBeIncluded(channel, getLoggedInUser()), "Channel should be permitted for user");
    }

    @Test
    public void testShouldBeIncludedBlacklist() throws Exception {
        SearchChannel channel = mock(SearchChannel.class);

        mockCapabilities(channel, true);
        doReturn(true).when(channel).hasPermission(any(User.class));
        doReturn("TestChannel").when(channel).getId();
        // blacklisted - should not be allowed
        PropertyUtil.addProperty("actionhandler.SearchOptions.blacklist", "TestChannel");

        SearchOptionsHandler handler = getHandler();
        Assertions.assertFalse(handler.shouldBeIncluded(channel, getGuestUser()), "Channel should not permitted for guest");
        Assertions.assertFalse(handler.shouldBeIncluded(channel, getLoggedInUser()), "Channel should be permitted for user");

        // clear blacklist - should be allowed
        PropertyUtil.clearProperties();
        handler = getHandler();
        Assertions.assertTrue(handler.shouldBeIncluded(channel, getGuestUser()), "Channel should not permitted for guest");
        Assertions.assertTrue(handler.shouldBeIncluded(channel, getLoggedInUser()), "Channel should be permitted for user");

    }

}