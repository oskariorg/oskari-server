package fi.nls.oskari.control.statistics.user;

import org.oskari.statistics.user.UserIndicatorService;
import org.oskari.statistics.user.UserIndicatorServiceImpl;
import fi.nls.oskari.control.ActionParameters;
import org.oskari.statistics.user.UserIndicator;
import fi.nls.test.control.JSONActionRouteTest;
import fi.nls.test.util.ResourceHelper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 * Created with IntelliJ IDEA.
 * User: EVALANTO
 * Date: 25.11.2013
 * Time: 11:30
 */

@RunWith(PowerMockRunner.class)
@PrepareForTest(value = {})
public class GetUserIndicatorsHandlerTest extends JSONActionRouteTest {

    final private GetUserIndicatorsHandler handler = new GetUserIndicatorsHandler();

    private UserIndicatorService userIndicatorService = null;


    @Before
    public void setUp() throws Exception {
        mockUserIndicatorService();
        handler.setUserIndicatorService(userIndicatorService);
        handler.init();
    }


    @Test
    public void testWithNoParam() throws Exception {
        final ActionParameters params = createActionParams(getLoggedInUser());
        handler.handleAction(params);


      //  verify(userIndicatorService).find(5);

        // check that the user is written to the config

        verifyResponseContent(ResourceHelper.readJSONArrayResource("GetUserIndicatorsHandlerTest-noId.json", this));
    }


    @Test
    public void testWithParam5() throws Exception {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put(SaveUserIndicatorHandler.PARAM_INDICATOR_ID, "5");
        final ActionParameters params = createActionParams(parameters, getLoggedInUser());
        handler.handleAction(params);

        //  verify(userIndicatorService).find(5);

        // check that the user is written to the config
        verifyResponseContent(ResourceHelper.readJSONResource("GetUserIndicatorsHandlerTest-id5.json", this));
    }


    /* *********************************************
     * Service mocks
     * ********************************************
     */
    private void mockUserIndicatorService() {

        userIndicatorService = mock(UserIndicatorServiceImpl.class);

        UserIndicator ui = new UserIndicator();
        ui.setId(5);
        ui.setPublished(true);
        ui.setTitle(" { \"fi\": Otsikko, \"sv\": Otsikkosv, \"en\": \"Title\" }");
        ui.setData("[ { \"region\": 49,  \"primary value\": 42 } ]");
        ui.setYear(2013);
        ui.setDescription("{ \"fi\": Kuvaus, \"sv\": Beskrivning, \"en\": Description }");
        ui.setUserId(getLoggedInUser().getId());
        ui.setMaterial(515);
        ui.setSource(" {title: { \"fi\": Indicator source }}");
        ui.setCategory("KUNTA");
        doReturn(ui).when(userIndicatorService).find(5);
        List<UserIndicator> uiList = new ArrayList<UserIndicator>();
        uiList.add(ui);
        doReturn(uiList).when(userIndicatorService).findAllOfUser(getLoggedInUser().getId()) ;
    }


}

