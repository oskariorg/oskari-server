package fi.nls.oskari.control.view;

import fi.nls.oskari.control.ActionDeniedException;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.domain.map.view.View;
import fi.nls.oskari.domain.map.view.ViewTypes;
import fi.nls.oskari.map.view.ViewService;
import fi.nls.oskari.map.view.ViewServiceIbatisImpl;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.test.control.JSONActionRouteTest;
import fi.nls.test.util.ResourceHelper;
import fi.nls.test.view.ViewTestHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

/**
 * @author SMAKINEN
 * TODO: add more tests with different parameters and view type testing!!
 * FIXME: should expect an error with guest user but route doesn't check that
 */
public class GetViewsHandlerTest extends JSONActionRouteTest {

    final private GetViewsHandler handler = new GetViewsHandler();
    private ViewService viewService = null;

    public void loadProperties() {
        try {
            Properties properties = new Properties();
            properties.load(GetViewsHandlerTest.class.getResourceAsStream("test.properties"));
            PropertyUtil.addProperties(properties);
        } catch (Exception e) {
        }
    }

    @Before
    public void setUp() throws Exception {
        loadProperties();
        viewService = mock(ViewServiceIbatisImpl.class);
        handler.setViewService(viewService);

        handler.init();
    }
    @After
    public void teardown() {
        PropertyUtil.clearProperties();
    }

    @Test(expected = ActionDeniedException.class)
    public void tesGetWithGuest() throws Exception {

        // mock returned views
        doReturn(Collections.emptyList()).when(viewService).getViewsForUser(anyLong());

        // setup params
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put(ViewTypes.VIEW_TYPE, ViewTypes.USER);

        final ActionParameters params = createActionParams(parameters);
        assertEquals("Parameter is set correctly", ViewTypes.USER, params.getHttpParam(ViewTypes.VIEW_TYPE));

        verifyResponseNotWritten(params);
        handler.handleAction(params);
    }
    @Test
    public void testEmptyViews() throws Exception {

        // mock returned views
        doReturn(Collections.emptyList()).when(viewService).getViewsForUser(anyLong());

        // setup params
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put(ViewTypes.VIEW_TYPE, ViewTypes.USER);

        final ActionParameters params = createActionParams(parameters,getLoggedInUser());
        assertEquals("Parameter is set correctly", ViewTypes.USER, params.getHttpParam(ViewTypes.VIEW_TYPE));

        verifyResponseNotWritten(params);
        handler.handleAction(params);
        // test that response was written once
        verifyResponseWritten(params);

        verifyResponseContent(ResourceHelper.readJSONResource("GetViewsHandlerTest-empty-views.json", this));
    }


    @Test(expected = ActionDeniedException.class)
    public void testWithNoParamsGuest() throws Exception {
        // mock returned views
        doReturn(Collections.emptyList()).when(viewService).getViewsForUser(anyLong());
        final ActionParameters params = createActionParams();

        verifyResponseNotWritten(params);
        handler.handleAction(params);
    }
    @Test
    public void testWithNoParams() throws Exception {
        // mock returned views
        doReturn(Collections.emptyList()).when(viewService).getViewsForUser(anyLong());
        final ActionParameters params = createActionParams(getLoggedInUser());

        verifyResponseNotWritten(params);
        handler.handleAction(params);
        // test that response was written once
        verifyResponseWritten(params);

        verifyResponseContent(ResourceHelper.readJSONResource("GetViewsHandlerTest-empty-views.json", this));
    }

    @Test(expected = ActionDeniedException.class)
    public void testViewListingGuest() throws Exception {
        // mock returned views
        final List<View> views = new ArrayList<View>();
        views.add(ViewTestHelper.createMockView("framework.mapfull"));
        doReturn(views).when(viewService).getViewsForUser(anyLong());

        // setup params
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put(ViewTypes.VIEW_TYPE, ViewTypes.USER);

        final ActionParameters params = createActionParams(parameters);
        assertEquals("Parameter is set correctly", ViewTypes.USER, params.getHttpParam(ViewTypes.VIEW_TYPE));

        verifyResponseNotWritten(params);
        handler.handleAction(params);
    }
    @Test
    public void testViewListing() throws Exception {
        // mock returned views
        final List<View> views = new ArrayList<View>();
        views.add(ViewTestHelper.createMockView("framework.mapfull"));
        doReturn(views).when(viewService).getViewsForUser(anyLong());

        // setup params
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put(ViewTypes.VIEW_TYPE, ViewTypes.USER);

        final ActionParameters params = createActionParams(parameters,getLoggedInUser());
        assertEquals("Parameter is set correctly", ViewTypes.USER, params.getHttpParam(ViewTypes.VIEW_TYPE));

        verifyResponseNotWritten(params);
        handler.handleAction(params);
        // test that response was written once
        verifyResponseWritten(params);

        verifyResponseContent(ResourceHelper.readJSONResource("GetViewsHandlerTest-has-views.json", this));
    }

    @Test(expected = ActionDeniedException.class)
    public void testViewListingWithNoParamsGuest() throws Exception {
        // mock returned views
        final List<View> views = new ArrayList<View>();
        views.add(ViewTestHelper.createMockView("framework.mapfull"));
        doReturn(views).when(viewService).getViewsForUser(anyLong());

        final ActionParameters params = createActionParams();

        verifyResponseNotWritten(params);
        handler.handleAction(params);
    }
    @Test
    public void testViewListingWithNoParams() throws Exception {
        // mock returned views

        final List<View> views = new ArrayList<View>();
        views.add(ViewTestHelper.createMockView("framework.mapfull"));
        doReturn(views).when(viewService).getViewsForUser(anyLong());

        final ActionParameters params = createActionParams(getLoggedInUser());

        verifyResponseNotWritten(params);
        handler.handleAction(params);
        // test that response was written once
        verifyResponseWritten(params);

        verifyResponseContent(ResourceHelper.readJSONResource("GetViewsHandlerTest-has-views.json", this));

    }

}
