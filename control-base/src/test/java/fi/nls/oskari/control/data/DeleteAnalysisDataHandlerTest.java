package fi.nls.oskari.control.data;

import fi.nls.oskari.control.ActionDeniedException;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.domain.map.analysis.Analysis;
import fi.nls.oskari.map.analysis.service.AnalysisDbService;
import fi.nls.oskari.map.analysis.service.AnalysisDbServiceMybatisImpl;
import fi.nls.oskari.util.DuplicateException;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.test.control.JSONActionRouteTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.exceptions.base.MockitoAssertionError;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * @author SMAKINEN
 */
public class DeleteAnalysisDataHandlerTest extends JSONActionRouteTest {

    private final static DeleteAnalysisDataHandler handler = new DeleteAnalysisDataHandler();
    private static AnalysisDbService service = null;

    private final Analysis analysisInvalid = new Analysis();
    private final Analysis analysisValid = new Analysis();

    private final static Long NON_MATCHING_ID = 12345l;
    private final static Long ANOTHER_USERS_ANALYSIS_ID = 1l;
    private final static Long VALID_ANALYSIS_ID = 2l;

    @BeforeAll
    public static void addLocales() throws Exception {
        PropertyUtil.clearProperties();
        Properties properties = new Properties();
        try {
            properties.load(DeleteAnalysisDataHandlerTest.class.getResourceAsStream("test.properties"));
            PropertyUtil.addProperties(properties);
            String locales = PropertyUtil.getNecessary("oskari.locales");
            if (locales == null)
                fail("No darned locales");
        } catch (DuplicateException e) {
            fail("Should not throw exception" + e.getStackTrace());
        }
    }

    @BeforeEach
    public void setUp() throws Exception {

        service = mock(AnalysisDbServiceMybatisImpl.class);
        doReturn(analysisInvalid).when(service).getAnalysisById(ANOTHER_USERS_ANALYSIS_ID);
        // setup matching uuid to permit delete
        analysisValid.setUuid(getLoggedInUser().getUuid());
        doReturn(analysisValid).when(service).getAnalysisById(VALID_ANALYSIS_ID);
        handler.setAnalysisDataService(service);
/*
        // mock permission service for deleting analyse
        permissionService = mock(PermissionsServiceIbatisImpl.class);
        handler.setPermissionsService(permissionService);
*/

        handler.init();
    }

    @AfterAll
    public static void teardown() {
        PropertyUtil.clearProperties();
    }

    /**
     * Tests that guest users can't call the action route
     *
     * @throws Exception
     */
    @Test()
    public void testWithGuest() throws Exception {
        assertThrows(ActionDeniedException.class, () -> {
            handler.handlePost(createActionParams());
            checkDeleteNotCalled();
        });
        fail("ActionDeniedException should have been thrown");
    }

    /**
     * Tests that correct exception is thrown if no parameters is present
     *
     * @throws Exception
     */
    @Test()
    public void testWithMissingParam() throws Exception {
        assertThrows(ActionParamsException.class, () -> {
            final ActionParameters params = createActionParams(getLoggedInUser());
            handler.handlePost(params);
            checkDeleteNotCalled();
        });
    }

    /**
     * Tests that correct exception is thrown if id parameter doesn't have valid id of type long
     *
     * @throws Exception
     */
    @Test()
    public void testWithInvalidParamType() throws Exception {
        assertThrows(ActionParamsException.class, () -> {
            final Map<String, String> parameters = new HashMap<String, String>();
            parameters.put("id", "this should be of type long");
            final ActionParameters params = createActionParams(parameters, getLoggedInUser());
            handler.handlePost(params);
            checkDeleteNotCalled();
        });
    }

    /**
     * Tests that correct exception is thrown if id parameter doesn't match an analysis in db
     *
     * @throws Exception
     */
    @Test()
    public void testWithNonMatchingIdParam() throws Exception {
        assertThrows(ActionParamsException.class, () -> {
            final Map<String, String> parameters = new HashMap<String, String>();
            parameters.put("id", NON_MATCHING_ID.toString());
            final ActionParameters params = createActionParams(parameters, getLoggedInUser());
            handler.handlePost(params);
            checkDeleteNotCalled();
        });
    }

    /**
     * Tests that correct exception is thrown if analysis matching the provided id was found,
     * but the analysis uuid doesn't match current users uuid.
     *
     * @throws Exception
     */
    @Test()
    public void testWithInvalidUser() throws Exception {
        assertThrows(ActionDeniedException.class, () -> {
            final Map<String, String> parameters = new HashMap<String, String>();
            parameters.put("id", ANOTHER_USERS_ANALYSIS_ID.toString());
            final ActionParameters params = createActionParams(parameters, getLoggedInUser());
            handler.handlePost(params);
            checkDeleteNotCalled();
        });
    }

    /**
     * Tests that service method for delete is called when valid user tries to delete his/her own analysis
     *
     * @throws Exception
     */
    @Test
    public void testWithValidUser() throws Exception {
        final Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("id", VALID_ANALYSIS_ID.toString());
        final ActionParameters params = createActionParams(parameters, getLoggedInUser());
        handler.handlePost(params);
        try {
            verify(service, times(1)).deleteAnalysis(analysisValid);
        } catch (MockitoAssertionError e) {
            // catch and throw to make a more meaningful fail message
            throw new MockitoAssertionError("Was expecting delete to have been called with valid analysis object");
        }
    }

    /**
     * Helper method that tests that delete was not called.
     *
     * @throws Exception
     */
    private void checkDeleteNotCalled() throws Exception {
        try {
            verify(service, never()).deleteAnalysis(any(Analysis.class));
        } catch (MockitoAssertionError e) {
            // catch and throw to make a more meaningful fail message
            throw new MockitoAssertionError("Was expecting delete to NOT have been called");
        }
    }
}
