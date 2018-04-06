package fi.nls.oskari.control.data;

import fi.mml.map.mapwindow.service.db.OskariMapLayerGroupService;
import fi.mml.map.mapwindow.service.db.OskariMapLayerGroupServiceIbatisImpl;
import fi.nls.oskari.control.ActionConstants;
import fi.nls.oskari.control.ActionDeniedException;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.domain.map.MaplayerGroup;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.test.control.JSONActionRouteTest;
import fi.nls.test.util.ResourceHelper;
import fi.nls.test.util.TestHelper;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;
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
public class MapLayerGroupsHandlerTest extends JSONActionRouteTest {

    private OskariMapLayerGroupService oskariMapLayerGroupService;
    private MapLayerGroupsHandler handler = new MapLayerGroupsHandler();

    @Before
    public void setUp() throws Exception {
        assumeTrue(TestHelper.dbAvailable());
        oskariMapLayerGroupService = mock(OskariMapLayerGroupServiceIbatisImpl.class);
        MaplayerGroup theme1 = new MaplayerGroup();
        theme1.setId(1);
        theme1.setName("fi", "teema 1");
        theme1.setName("en", "theme 1");

        MaplayerGroup themeInsert = new MaplayerGroup();
        themeInsert.setId(2);
        themeInsert.setName("fi", "teema fi");
        themeInsert.setName("en", "theme en");
        doReturn(themeInsert).when(oskariMapLayerGroupService).find(2);

        List<MaplayerGroup> list = new ArrayList<MaplayerGroup>(1);
        list.add(theme1);
        doReturn(list).when(oskariMapLayerGroupService).findAll();
        doReturn(theme1).when(oskariMapLayerGroupService).find(1);

        doReturn(2).when(oskariMapLayerGroupService).insert(any(MaplayerGroup.class));

        handler.setOskariMapLayerGroupService(oskariMapLayerGroupService);
        handler.init();
    }
    @Test
    public void testHandleGetAll() throws Exception {
        ActionParameters params = createActionParams();
        handler.handleGet(params);
        verifyResponseContent(ResourceHelper.readJSONResource("MapLayerGroupsHandlerTest-findall-expected.json", this));
    }
    @Test
    public void testHandleGetSingle() throws Exception {
        Map<String, String> map = buildParams().put(ActionConstants.PARAM_ID, 1).done();
        ActionParameters params = createActionParams(map);
        handler.handleGet(params);
        verifyResponseContent(ResourceHelper.readJSONResource("MapLayerGroupsHandlerTest-find-expected.json", this));
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
        verifyResponseContent(ResourceHelper.readJSONResource("MapLayerGroupsHandlerTest-insert-expected.json", this));
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
        verifyResponseContent(ResourceHelper.readJSONResource("MapLayerGroupsHandlerTest-update-expected.json", this));
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
        verifyResponseContent(ResourceHelper.readJSONResource("MapLayerGroupsHandlerTest-find-expected.json", this));
    }

    private Map<String, String> createDummyLocale() {
        Map<String, String> map = buildParams().
                put(ActionConstants.PARAM_NAME_PREFIX + "en", "theme en").
                put(ActionConstants.PARAM_NAME_PREFIX + "fi", "teema fi").done();
        return map;
    }

    @AfterClass
    public static void delete() {
        PropertyUtil.clearProperties();
    }
}
