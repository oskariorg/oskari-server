package fi.nls.oskari.control.view;

import fi.nls.oskari.control.ActionDeniedException;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.domain.map.view.View;
import fi.nls.oskari.domain.map.view.ViewTypes;
import fi.nls.oskari.map.view.AppSetupServiceMybatisImpl;
import fi.nls.oskari.map.view.ViewService;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.test.control.JSONActionRouteTest;
import fi.nls.test.util.ResourceHelper;
import fi.nls.test.view.ViewTestHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

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

    @BeforeEach
    public void setUp() throws Exception {
        loadProperties();
        viewService = mock(AppSetupServiceMybatisImpl.class);
        handler.setViewService(viewService);

        handler.init();
    }
    @AfterEach
    public void teardown() {
        PropertyUtil.clearProperties();
    }

    @Test()
    public void tesGetWithGuest() throws Exception {

        assertThrows(ActionDeniedException.class, () -> {
            // mock returned views
            doReturn(Collections.emptyList()).when(viewService).getViewsForUser(anyLong());

            // setup params
            Map<String, String> parameters = new HashMap<String, String>();
            parameters.put(ViewTypes.VIEW_TYPE, ViewTypes.USER);

            final ActionParameters params = createActionParams(parameters);
            assertEquals(ViewTypes.USER, params.getHttpParam(ViewTypes.VIEW_TYPE), "Parameter is set correctly");

            verifyResponseNotWritten(params);
            handler.handleAction(params);

        });
    }
    @Test
    @Disabled
    // java.lang.NullPointerException: Cannot invoke "java.io.PrintWriter.print(Object)" because the return value of "javax.servlet.http.HttpServletResponse.getWriter()" is null
    public void testEmptyViews() throws Exception {

        // mock returned views
        doReturn(Collections.emptyList()).when(viewService).getViewsForUser(anyLong());

        // setup params
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put(ViewTypes.VIEW_TYPE, ViewTypes.USER);

        final ActionParameters params = createActionParams(parameters,getLoggedInUser());
        assertEquals(ViewTypes.USER, params.getHttpParam(ViewTypes.VIEW_TYPE), "Parameter is set correctly");

        verifyResponseNotWritten(params);
        handler.handleAction(params);
        // test that response was written once
        verifyResponseWritten(params);

        verifyResponseContent(ResourceHelper.readJSONResource("GetViewsHandlerTest-empty-views.json", this));
    }


    @Test()
    public void testWithNoParamsGuest() throws Exception {
        assertThrows(ActionDeniedException.class, () -> {
            // mock returned views
            doReturn(Collections.emptyList()).when(viewService).getViewsForUser(anyLong());
            final ActionParameters params = createActionParams();

            verifyResponseNotWritten(params);
            handler.handleAction(params);
        });

    }
    @Test
    @Disabled
    // java.lang.NullPointerException: Cannot invoke "java.io.PrintWriter.print(Object)" because the return value of "javax.servlet.http.HttpServletResponse.getWriter()" is null
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

    @Test()
    public void testViewListingGuest() throws Exception {
        assertThrows(ActionDeniedException.class, () -> {
            // mock returned views
            final List<View> views = new ArrayList<View>();
            views.add(ViewTestHelper.createMockView("framework.mapfull"));
            doReturn(views).when(viewService).getViewsForUser(anyLong());

            // setup params
            Map<String, String> parameters = new HashMap<String, String>();
            parameters.put(ViewTypes.VIEW_TYPE, ViewTypes.USER);

            final ActionParameters params = createActionParams(parameters);
            assertEquals(ViewTypes.USER, params.getHttpParam(ViewTypes.VIEW_TYPE), "Parameter is set correctly");

            verifyResponseNotWritten(params);
            handler.handleAction(params);

        });
    }
    @Test
    @Disabled
    // java.lang.NullPointerException: Cannot invoke "java.io.PrintWriter.print(Object)" because the return value of "javax.servlet.http.HttpServletResponse.getWriter()" is null
    public void testViewListing() throws Exception {
        // mock returned views
        final List<View> views = new ArrayList<View>();
        views.add(ViewTestHelper.createMockView("framework.mapfull"));
        doReturn(views).when(viewService).getViewsForUser(anyLong());

        // setup params
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put(ViewTypes.VIEW_TYPE, ViewTypes.USER);

        final ActionParameters params = createActionParams(parameters,getLoggedInUser());
        assertEquals(ViewTypes.USER, params.getHttpParam(ViewTypes.VIEW_TYPE), "Parameter is set correctly");

        verifyResponseNotWritten(params);
        handler.handleAction(params);
        // test that response was written once
        verifyResponseWritten(params);

        verifyResponseContent(ResourceHelper.readJSONResource("GetViewsHandlerTest-has-views.json", this));
    }

    @Test()
    public void testViewListingWithNoParamsGuest() throws Exception {
        assertThrows(ActionDeniedException.class, () -> {
            // mock returned views
            final List<View> views = new ArrayList<View>();
            views.add(ViewTestHelper.createMockView("framework.mapfull"));
            doReturn(views).when(viewService).getViewsForUser(anyLong());

            final ActionParameters params = createActionParams();

            verifyResponseNotWritten(params);
            handler.handleAction(params);

        });
    }
    @Test
    @Disabled
    // java.lang.NullPointerException: Cannot invoke "java.io.PrintWriter.print(Object)" because the return value of "javax.servlet.http.HttpServletResponse.getWriter()" is null
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
