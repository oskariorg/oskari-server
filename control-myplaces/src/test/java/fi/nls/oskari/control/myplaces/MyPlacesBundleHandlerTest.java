package fi.nls.oskari.control.myplaces;

import fi.nls.oskari.control.ActionDeniedException;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.domain.map.MyPlaceCategory;
import fi.nls.oskari.map.myplaces.domain.ProxyRequest;
import fi.nls.oskari.map.myplaces.service.GeoServerProxyService;
import fi.nls.oskari.myplaces.MyPlacesService;
import fi.nls.oskari.myplaces.MyPlacesServiceMybatisImpl;
import fi.nls.test.control.JSONActionRouteTest;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class MyPlacesBundleHandlerTest extends JSONActionRouteTest {

    private final static MyPlacesBundleHandler handler = new MyPlacesBundleHandler();
    private MyPlacesService service = null;
    private GeoServerProxyService proxyService = null;

    private static final String SUCCESS_TEXT = "Great success";

    @Before
    public void setUp() throws Exception {

        service = mock(MyPlacesServiceMybatisImpl.class);
        handler.setMyPlacesService(service);
        final List<MyPlaceCategory> list = new ArrayList<MyPlaceCategory>();
        MyPlaceCategory cat = new MyPlaceCategory();
        cat.setId(1);
        cat.setUuid("category uuid");
        list.add(cat);
        MyPlaceCategory cat2 = new MyPlaceCategory();
        cat2.setId(2);
        cat2.setUuid(getLoggedInUser().getUuid());
        list.add(cat2);

        doReturn(list).when(service).getMyPlaceLayersById(anyList());
        doReturn(false).when(service).canInsert(getGuestUser(), 2);
        doReturn(true).when(service).canInsert(getLoggedInUser(), 2);
        doReturn(true).when(service).canInsert(getGuestUser(), 1);
        doReturn(true).when(service).canInsert(getLoggedInUser(), 1);

        doReturn(false).when(service).canModifyPlace(getGuestUser(), 1);
        doReturn(true).when(service).canModifyPlace(getLoggedInUser(), 1);

        doReturn(false).when(service).canModifyPlace(getGuestUser(), 2);
        doReturn(false).when(service).canModifyPlace(getLoggedInUser(), 2);

        doReturn(true).when(service).canModifyCategory(getLoggedInUser(), 1);
        doReturn(false).when(service).canModifyCategory(getLoggedInUser(), 2);


        proxyService = mock(GeoServerProxyService.class);
        handler.setGeoServerProxyService(proxyService);
        doReturn(SUCCESS_TEXT).when(proxyService).proxy(any(ProxyRequest.class));

        handler.init();
    }

    /**
     * Tests that route doesn't accept calls without payload XML
     */
    @Test(expected = ActionParamsException.class)
    public void testWithoutPostData() throws Exception {
        handler.handleAction(createActionParams());
        fail("ActionParamsException should have been thrown with no payload");
    }
    /**
     * Tests that guest users can't call the action route with invalid content
     */
    @Test(expected = ActionParamsException.class)
    public void testWithGuestInvalidContent() throws Exception {
        InputStream payload = getClass().getResourceAsStream("MyPlacesBundleHandlerTest-input-guest-invalid-content.txt");
        handler.handleAction(createActionParams(payload));
        fail("ActionDeniedException should have been thrown with invalid content");
    }

    /**
     * Tests that guest users can't call the action route with invalid xml
     */
    @Test(expected = ActionDeniedException.class)
    public void testInsertWithGuestInvalidXMLContent() throws Exception {
        InputStream payload = getClass().getResourceAsStream("MyPlacesBundleHandlerTest-input-guest-invalid-xml-content.xml");
        handler.handleAction(createActionParams(payload));
        fail("ActionDeniedException should have been thrown with invalid content");
    }
    /**
     * Tests that guest users can't call the action route with invalid content
     */
    @Test(expected = ActionException.class)
    public void testInsertWithGuestMissingUUID() throws Exception {
        InputStream payload = getClass().getResourceAsStream("MyPlacesBundleHandlerTest-input-guest-insert-place-missing-uuid.xml");
        handler.handleAction(createActionParams(payload));
        fail("ActionException should have been thrown with invalid content");
    }

    /**
     * Tests that guest users can't call the action route with non-published categoryId
     * NOTE! categoryId is mocked to be non-writable in setup!!
     */
    @Test(expected = ActionDeniedException.class)
    public void testInsertWithGuestInvalidCategoryId() throws Exception {
        InputStream payload = getClass().getResourceAsStream("MyPlacesBundleHandlerTest-input-guest-insert-place-invalid-id.xml");
        ActionParameters params = createActionParams(payload);
        handler.handleAction(params);
        fail("ActionDeniedException should have been thrown with invalid categoryId");
    }

    /**
     * Tests that guest users can call the action route with valid content
     * NOTE! categoryId is mocked to be writable in setup!!
     */
    @Test
    public void testInsertWithGuestValidContent() throws Exception {
        InputStream payload = getClass().getResourceAsStream("MyPlacesBundleHandlerTest-input-guest-insert-place-valid.xml");
        ActionParameters params = createActionParams(payload);
        handler.handleAction(params);
        verifyResponseWritten(params);
        assertEquals("Should write '"+ SUCCESS_TEXT + "' if request is proxied to geoserver", SUCCESS_TEXT, getResponseString());
    }

    /**
     * Tests that users can insert place in their own categories
     * NOTE! categoryId is mocked to be writable in setup!!
     */
    @Test
    public void testInsertWithUserOwnCategory() throws Exception {
        InputStream payload = getClass().getResourceAsStream("MyPlacesBundleHandlerTest-input-user-insert-place-own-category-valid.xml");
        ActionParameters params = createActionParams(getLoggedInUser(), payload);
        handler.handleAction(params);
        verifyResponseWritten(params);
        assertEquals("Should write '"+ SUCCESS_TEXT + "' if request is proxied to geoserver", SUCCESS_TEXT, getResponseString());
    }

    /**
     * Tests that users can insert place in another users published draw categories
     * NOTE! categoryId is mocked to be writable in setup!!
     */
    @Test
    public void testInsertWithUserIntoDrawCategory() throws Exception {
        InputStream payload = getClass().getResourceAsStream("MyPlacesBundleHandlerTest-input-user-insert-place-draw-category-valid.xml");
        ActionParameters params = createActionParams(getLoggedInUser(), payload);
        handler.handleAction(params);
        verifyResponseWritten(params);
        assertEquals("Should write '"+ SUCCESS_TEXT + "' if request is proxied to geoserver", SUCCESS_TEXT, getResponseString());
    }


    /**
     * Tests that guest users can't call the action route with get categories
     */
    @Test(expected = ActionDeniedException.class)
    public void testWithGuestGetCategories() throws Exception {
        InputStream payload = getClass().getResourceAsStream("MyPlacesBundleHandlerTest-input-user-get-categories-valid.xml");
        ActionParameters params = createActionParams(payload);
        handler.handleAction(params);
        fail("ActionDeniedException should have been thrown for Guest calling get categories");
    }

    /**
     * Tests that users can't call the action route with  get categories using other users uuid
     */
    @Test(expected = ActionDeniedException.class)
    public void testWithInvalidUserGetCategories() throws Exception {
        InputStream payload = getClass().getResourceAsStream("MyPlacesBundleHandlerTest-input-user-get-categories-invalid.xml");
        ActionParameters params = createActionParams(getLoggedInUser(), payload);
        handler.handleAction(params);
        fail("ActionDeniedException should have been thrown for Guest calling get categories");
    }

    /**
     * Tests that users can get categories with their own id
     */
    @Test
    public void testWithGetCategories() throws Exception {
        InputStream payload = getClass().getResourceAsStream("MyPlacesBundleHandlerTest-input-user-get-categories-valid.xml");
        ActionParameters params = createActionParams(getLoggedInUser(), payload);
        handler.handleAction(params);
        verifyResponseWritten(params);
        assertEquals("Should write '" + SUCCESS_TEXT + "' if request is proxied to geoserver", SUCCESS_TEXT, getResponseString());
    }


    /**
     * Tests that guest users can't call the action route with get places
     */
    @Test(expected = ActionDeniedException.class)
    public void testWithGuestGetPlaces() throws Exception {
        InputStream payload = getClass().getResourceAsStream("MyPlacesBundleHandlerTest-input-user-get-places-valid.xml");
        ActionParameters params = createActionParams(payload);
        handler.handleAction(params);
        fail("ActionDeniedException should have been thrown for Guest calling get places");
    }

    /**
     * Tests that users can't call the action route with get places using other users uuid
     */
    @Test(expected = ActionDeniedException.class)
    public void testWithInvalidUserGetPlaces() throws Exception {
        InputStream payload = getClass().getResourceAsStream("MyPlacesBundleHandlerTest-input-user-get-places-invalid.xml");
        ActionParameters params = createActionParams(getLoggedInUser(), payload);
        handler.handleAction(params);
        fail("ActionDeniedException should have been thrown for Guest calling get places");
    }

    /**
     * Tests that users can get places with their own id
     */
    @Test
    public void testWithGetPlaces() throws Exception {
        InputStream payload = getClass().getResourceAsStream("MyPlacesBundleHandlerTest-input-user-get-places-valid.xml");
        ActionParameters params = createActionParams(getLoggedInUser(), payload);
        handler.handleAction(params);
        verifyResponseWritten(params);
        assertEquals("Should write '" + SUCCESS_TEXT + "' if request is proxied to geoserver", SUCCESS_TEXT, getResponseString());
    }

    /**
     * Tests that guest users can't call the action route with random users uuid
     */
    @Test(expected = ActionParamsException.class)
    public void testInsertCategoryWithGuest() throws Exception {
        InputStream payload = getClass().getResourceAsStream("MyPlacesBundleHandlerTest-input-user-insert-category-valid.xml");
        ActionParameters params = createActionParams(payload);
        handler.handleAction(params);
        fail("ActionParamsException should have been thrown with guest since the category");
    }

    /**
     * Tests that users can't call the action route with other users uuid
     */
    @Test(expected = ActionDeniedException.class)
    public void testInsertCategoryWithUserInvalidUUID() throws Exception {
        InputStream payload = getClass().getResourceAsStream("MyPlacesBundleHandlerTest-input-user-insert-category-invalid-uuid.xml");
        ActionParameters params = createActionParams(getLoggedInUser(), payload);
        handler.handleAction(params);
        fail("ActionDeniedException should have been thrown with invalid uuid");
    }

    /**
     * Tests that users can call the action route with modify own place
     */
    @Test
    public void testModifyUsersPlace() throws Exception {
        InputStream payload = getClass().getResourceAsStream("MyPlacesBundleHandlerTest-input-user-modify-place-valid.xml");
        ActionParameters params = createActionParams(getLoggedInUser(), payload);
        handler.handleAction(params);
        verifyResponseWritten(params);
        assertEquals("Should write '" + SUCCESS_TEXT + "' if request is proxied to geoserver", SUCCESS_TEXT, getResponseString());
    }

    /**
     * Tests that users can't call the action route with modify other users place
     */
    @Test(expected = ActionDeniedException.class)
    public void testModifyOtherUsersPlace() throws Exception {
        InputStream payload = getClass().getResourceAsStream("MyPlacesBundleHandlerTest-input-user-modify-place-other-users-place-invalid.xml");
        ActionParameters params = createActionParams(getLoggedInUser(), payload);
        handler.handleAction(params);
        fail("ActionDeniedException should have been thrown with other users place");
    }

    /**
     * Tests that users can call the action route with modify own place
     */
    @Test
    public void testMoveUsersPlace() throws Exception {
        InputStream payload = getClass().getResourceAsStream("MyPlacesBundleHandlerTest-input-user-move-place-category-valid.xml");
        ActionParameters params = createActionParams(getLoggedInUser(), payload);
        handler.handleAction(params);
        verifyResponseWritten(params);
        assertEquals("Should write '" + SUCCESS_TEXT + "' if request is proxied to geoserver", SUCCESS_TEXT, getResponseString());
    }

    /**
     * Tests that users can call the action route with delete own place
     */
    @Test
    public void testDeleteUsersPlace() throws Exception {
        InputStream payload = getClass().getResourceAsStream("MyPlacesBundleHandlerTest-input-user-delete-place-valid.xml");
        ActionParameters params = createActionParams(getLoggedInUser(), payload);
        handler.handleAction(params);
        verifyResponseWritten(params);
        assertEquals("Should write '" + SUCCESS_TEXT + "' if request is proxied to geoserver", SUCCESS_TEXT, getResponseString());
    }

    /**
     * Tests that users can't call the action route with delete other users place
     */
    @Test(expected = ActionDeniedException.class)
    public void testDeleteOtherUsersPlace() throws Exception {
        InputStream payload = getClass().getResourceAsStream("MyPlacesBundleHandlerTest-input-user-delete-place-other-user-invalid.xml");
        ActionParameters params = createActionParams(getLoggedInUser(), payload);
        handler.handleAction(params);
        fail("ActionDeniedException should have been thrown with other users place");
    }
    /**
     * Tests that users can call the action route with delete own category
     */
    @Test
    public void testDeleteUsersCategory() throws Exception {
        InputStream payload = getClass().getResourceAsStream("MyPlacesBundleHandlerTest-input-user-delete-category-valid.xml");
        ActionParameters params = createActionParams(getLoggedInUser(), payload);
        handler.handleAction(params);
        verifyResponseWritten(params);
        assertEquals("Should write '" + SUCCESS_TEXT + "' if request is proxied to geoserver", SUCCESS_TEXT, getResponseString());
    }

    /**
     * Tests that users can't call the action route with delete other users place
     */
    @Test(expected = ActionDeniedException.class)
    public void testDeleteOtherUsersCategory() throws Exception {
        InputStream payload = getClass().getResourceAsStream("MyPlacesBundleHandlerTest-input-user-delete-category-other-user-invalid.xml");
        ActionParameters params = createActionParams(getLoggedInUser(), payload);
        handler.handleAction(params);
        fail("ActionDeniedException should have been thrown with other users category");
    }

    /**
     * Tests that users can call the action route with get their own place
     */
    @Test
    public void testUsersPlace() throws Exception {
        InputStream payload = getClass().getResourceAsStream("MyPlacesBundleHandlerTest-input-user-get-feature-valid.xml");
        ActionParameters params = createActionParams(getLoggedInUser(), payload);
        handler.handleAction(params);
        verifyResponseWritten(params);
        assertEquals("Should write '" + SUCCESS_TEXT + "' if request is proxied to geoserver", SUCCESS_TEXT, getResponseString());
    }

    /**
     * Tests that users can't call the action route with get other users place
     */
    @Test(expected = ActionDeniedException.class)
    public void testOtherUsersPlace() throws Exception {
        InputStream payload = getClass().getResourceAsStream("MyPlacesBundleHandlerTest-input-user-get-feature-wrong-uuid-invalid.xml");
        ActionParameters params = createActionParams(getLoggedInUser(), payload);
        handler.handleAction(params);
        fail("ActionDeniedException should have been thrown with other users place");
    }

    /**
     * Tests that users can't call the action route with get place and without uuid filter
     */
    @Test(expected = ActionDeniedException.class)
    public void testUsersPlaceMissingUUID() throws Exception {
        InputStream payload = getClass().getResourceAsStream("MyPlacesBundleHandlerTest-input-user-get-feature-missing-uuid-invalid.xml");
        ActionParameters params = createActionParams(getLoggedInUser(), payload);
        handler.handleAction(params);
        fail("ActionDeniedException should have been thrown without uuid filter");
    }


    /**
     * Tests that users can call the action route with modify their own category
     */
    @Test
    public void testModifyCategory() throws Exception {
        InputStream payload = getClass().getResourceAsStream("MyPlacesBundleHandlerTest-input-user-modify-category-valid.xml");
        ActionParameters params = createActionParams(getLoggedInUser(), payload);
        handler.handleAction(params);
        verifyResponseWritten(params);
        assertEquals("Should write '" + SUCCESS_TEXT + "' if request is proxied to geoserver", SUCCESS_TEXT, getResponseString());
    }

    /**
     * Tests that users can't call the action route with modify other users category
     */
    @Test(expected = ActionDeniedException.class)
    public void testModifyOtherUsersCategory() throws Exception {
        InputStream payload = getClass().getResourceAsStream("MyPlacesBundleHandlerTest-input-user-modify-category-other-users-category-invalid.xml");
        ActionParameters params = createActionParams(getLoggedInUser(), payload);
        handler.handleAction(params);
        fail("ActionDeniedException should have been thrown without uuid filter");
    }
}
