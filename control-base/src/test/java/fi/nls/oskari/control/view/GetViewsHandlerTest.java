package fi.nls.oskari.control.view;

import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.domain.map.view.View;
import fi.nls.oskari.domain.map.view.ViewTypes;
import fi.nls.oskari.map.view.ViewService;
import fi.nls.oskari.map.view.ViewServiceIbatisImpl;
import fi.nls.oskari.util.DuplicateException;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.test.control.JSONActionRouteTest;
import fi.nls.test.util.ResourceHelper;
import fi.nls.test.view.ViewTestHelper;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.*;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

/**
 * @author SMAKINEN
 * TODO: add more tests with different parameters and view type testing!!
 * FIXME: should expect an error with guest user but route doesn't check that
 */
public class GetViewsHandlerTest extends JSONActionRouteTest {

    final private GetViewsHandler handler = new GetViewsHandler();
    private ViewService viewService = null;

    @BeforeClass
    public static void addLocales() throws Exception {
        Properties properties = new Properties();
        try {
            properties.load(GetViewsHandlerTest.class.getResourceAsStream("test.properties"));
            PropertyUtil.addProperties(properties);
            String locales = PropertyUtil.getNecessary("oskari.locales");
            if (locales == null)
                fail("No darned locales");
        } catch (DuplicateException e) {
            // ignore
        }
    }

    @Before
    public void setUp() throws Exception {
        viewService = mock(ViewServiceIbatisImpl.class);
        handler.setViewService(viewService);

        handler.init();
    }

    @Test
    public void testEmptyViews() throws Exception {

        // mock returned views
        doReturn(Collections.emptyList()).when(viewService).getViewsForUser(anyLong());

        // setup params
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put(ViewTypes.VIEW_TYPE, ViewTypes.USER);

        final ActionParameters params = createActionParams(parameters);
        assertEquals("Parameter is set correctly", ViewTypes.USER, params.getHttpParam(ViewTypes.VIEW_TYPE));

        verifyResponseNotWritten(params);
        handler.handleAction(params);
        // test that response was written once
        verifyResponseWritten(params);

        verifyResponseContent(ResourceHelper.readJSONResource("GetViewsHandlerTest-empty-views.json", this));
    }

    @Test
    public void testWithNoParams() throws Exception {
        // mock returned views
        doReturn(Collections.emptyList()).when(viewService).getViewsForUser(anyLong());
        final ActionParameters params = createActionParams();

        verifyResponseNotWritten(params);
        handler.handleAction(params);
        // test that response was written once
        verifyResponseWritten(params);

        verifyResponseContent(ResourceHelper.readJSONResource("GetViewsHandlerTest-empty-views.json", this));
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

        final ActionParameters params = createActionParams(parameters);
        assertEquals("Parameter is set correctly", ViewTypes.USER, params.getHttpParam(ViewTypes.VIEW_TYPE));

        verifyResponseNotWritten(params);
        handler.handleAction(params);
        // test that response was written once
        verifyResponseWritten(params);

        verifyResponseContent(ResourceHelper.readJSONResource("GetViewsHandlerTest-has-views.json", this));
    }

    @Test
    public void testViewListingWithNoParams() throws Exception {
        // mock returned views
        final List<View> views = new ArrayList<View>();
        views.add(ViewTestHelper.createMockView("framework.mapfull"));
        doReturn(views).when(viewService).getViewsForUser(anyLong());

        final ActionParameters params = createActionParams();

        verifyResponseNotWritten(params);
        handler.handleAction(params);
        // test that response was written once
        verifyResponseWritten(params);

        verifyResponseContent(ResourceHelper.readJSONResource("GetViewsHandlerTest-has-views.json", this));
    }

}
