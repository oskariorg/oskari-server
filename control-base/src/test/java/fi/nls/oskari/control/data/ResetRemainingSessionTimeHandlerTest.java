package fi.nls.oskari.control.data;

import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.session.ResetRemainingSessionTimeHandler;
import fi.nls.test.control.JSONActionRouteTest;
import fi.nls.test.util.ResourceHelper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ResetRemainingSessionTimeHandlerTest extends JSONActionRouteTest {
    
    private static final int MAX_INACTIVE_INTERVAL = 1800;
    private static final long NOW = System.currentTimeMillis();
    private static final long FIVE_SECONDS_AGO = NOW - 5000;
    private ResetRemainingSessionTimeHandler handler = new ResetRemainingSessionTimeHandler();

    @Test
    @Disabled("Mocking system not allowed with mockito")
    public void testResponseSessionExists() throws Exception {
        
        ActionParameters params = createActionParams();
        
    // Mocking System isn't allowed by mockito and even before when this was done using powermock it's considered bad practice
//        Mockito.mockStatic(System.class);
//        when(System.currentTimeMillis()).thenReturn(NOW);
        doReturn(params.getRequest().getSession()).when(params.getRequest()).getSession(false);
        when(params.getRequest().getSession().getMaxInactiveInterval()).thenReturn(MAX_INACTIVE_INTERVAL);
        when(params.getRequest().getSession().getLastAccessedTime()).thenReturn(System.currentTimeMillis() - 5000);
        handler.handleAction(params);
        verifyResponseContent(ResourceHelper
                .readJSONResource("ResetRemainingSessionTimeHandlerTest-session-response-expected.json", this));
    }

    @Test
    public void testResponseNoSession() throws Exception {
        ActionParameters params = createActionParams();
        doReturn(null).when(params.getRequest()).getSession(false);
        handler.handleAction(params);
        verifyResponseContent(ResourceHelper
                .readJSONResource("ResetRemainingSessionTimeHandlerTest-no-session-response-expected.json", this));
    }
    
    
}
