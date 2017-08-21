package fi.nls.oskari.control.data;

import fi.mml.map.mapwindow.service.db.InspireThemeService;
import fi.mml.map.mapwindow.service.db.InspireThemeServiceIbatisImpl;
import fi.nls.oskari.control.ActionConstants;
import fi.nls.oskari.control.ActionDeniedException;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.domain.map.InspireTheme;
import fi.nls.oskari.util.DuplicateException;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.test.control.JSONActionRouteTest;
import fi.nls.test.util.ResourceHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 * Created with IntelliJ IDEA.
 * User: SMAKINEN
 * Date: 23.6.2014
 * Time: 16:03
 * To change this template use File | Settings | File Templates.
 */
public class InspireThemesHandlerTest extends JSONActionRouteTest {

    private InspireThemeService inspireThemeService;
    private InspireThemesHandler handler = new InspireThemesHandler();

    @Before
    public void setUp() throws Exception {
        Properties properties = new Properties();
        try {
            properties.load(InspireThemesHandlerTest.class.getResourceAsStream("test.properties"));
            PropertyUtil.addProperties(properties);
        } catch (DuplicateException e) {
            //fail("Should not throw exception" + e.getStackTrace());
        }
        inspireThemeService = mock(InspireThemeServiceIbatisImpl.class);
        InspireTheme theme1 = new InspireTheme();
        theme1.setId(1);
        theme1.setName("fi", "teema 1");
        theme1.setName("en", "theme 1");

        InspireTheme themeInsert = new InspireTheme();
        themeInsert.setId(2);
        themeInsert.setName("fi", "teema fi");
        themeInsert.setName("en", "theme en");
        doReturn(themeInsert).when(inspireThemeService).find(2);

        List<InspireTheme> list = new ArrayList<InspireTheme>(1);
        list.add(theme1);
        doReturn(list).when(inspireThemeService).findAll();
        doReturn(theme1).when(inspireThemeService).find(1);

        doReturn(2).when(inspireThemeService).insert(any(InspireTheme.class));

        handler.setInspireThemeService(inspireThemeService);
        handler.init();
    }
    @Test
    public void testHandleGetAll() throws Exception {
        ActionParameters params = createActionParams();
        handler.handleGet(params);
        verifyResponseContent(ResourceHelper.readJSONResource("InspireThemesHandlerTest-findall-expected.json", this));
    }
    @Test
    public void testHandleGetSingle() throws Exception {
        Map<String, String> map = buildParams().put(ActionConstants.PARAM_ID, 1).done();
        ActionParameters params = createActionParams(map);
        handler.handleGet(params);
        verifyResponseContent(ResourceHelper.readJSONResource("InspireThemesHandlerTest-find-expected.json", this));
    }

    @Test(expected = ActionDeniedException.class)
    public void testHandlePutGuest() throws Exception {
        ActionParameters params = createActionParams(createDummyLocale());
        handler.handlePut(params);
        fail("Should have thrown denied exception");
    }
    @Test(expected = ActionDeniedException.class)
    public void testHandlePostGuest() throws Exception {
        ActionParameters params = createActionParams(createDummyLocale());
        handler.handlePost(params);
        fail("Should have thrown denied exception");
    }
    @Test
    public void testHandlePutAdmin() throws Exception {
        ActionParameters params = createActionParams(createDummyLocale(), getAdminUser());
        handler.handlePut(params);
        verifyResponseContent(ResourceHelper.readJSONResource("InspireThemesHandlerTest-insert-expected.json", this));
    }

    @Test(expected = ActionParamsException.class)
    public void testHandlePostNoId() throws Exception {
        ActionParameters params = createActionParams(createDummyLocale(), getAdminUser());
        handler.handlePost(params);
        fail("Should have thrown params exception");
    }

    @Test
    public void testHandlePostAdmin() throws Exception {
        Map<String, String> map = buildParams().put(ActionConstants.PARAM_ID, 1).
                put(createDummyLocale()).done();
        ActionParameters params = createActionParams(map, getAdminUser());
        handler.handlePost(params);
        verifyResponseContent(ResourceHelper.readJSONResource("InspireThemesHandlerTest-update-expected.json", this));
    }

    @Test(expected = ActionDeniedException.class)
    public void testHandleDeleteGuest() throws Exception {
        ActionParameters params = createActionParams(createDummyLocale());
        handler.handleDelete(params);
        fail("Should have thrown denied exception");
    }
    @Test(expected = ActionDeniedException.class)
    public void testHandleDeleteLoggedIn() throws Exception {
        ActionParameters params = createActionParams(createDummyLocale(), getLoggedInUser());
        handler.handleDelete(params);
        fail("Should have thrown denied exception");
    }

    @Test
    public void testHandleDeleteAdmin() throws Exception {
        Map<String, String> map = buildParams().put(ActionConstants.PARAM_ID, 1).done();
        ActionParameters params = createActionParams(map, getAdminUser());
        handler.handleDelete(params);
        verifyResponseContent(ResourceHelper.readJSONResource("InspireThemesHandlerTest-find-expected.json", this));
    }

    private Map<String, String> createDummyLocale() {
        Map<String, String> map = buildParams().
                put(ActionConstants.PARAM_NAME_PREFIX + "en", "theme en").
                put(ActionConstants.PARAM_NAME_PREFIX + "fi", "teema fi").done();
        return map;
    }

    @After
    public void delete() {
        PropertyUtil.clearProperties();
    }
}
