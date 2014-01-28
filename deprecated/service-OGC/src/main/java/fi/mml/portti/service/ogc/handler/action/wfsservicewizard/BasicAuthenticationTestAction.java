package fi.mml.portti.service.ogc.handler.action.wfsservicewizard;

import fi.mml.portti.domain.ogc.util.http.EasyHttpClient;
import fi.mml.portti.domain.ogc.util.http.HttpPostResponse;
import fi.mml.portti.service.ogc.handler.FlowModel;
import fi.mml.portti.service.ogc.handler.OGCActionHandler;

/**
 * This action will test if given username and password are correct
 *
 */
public class BasicAuthenticationTestAction implements OGCActionHandler {
	
	public static final String RESULT_KEY = "BASIC_AUTHENTICATION_TEST_RESULT";
	public static final String RESULT_VALUE_OK = "OK";
	public static final String RESULT_VALUE_SERVICE_DOWN = "SERVICE_DOWN";
	public static final String RESULT_VALUE_INCORRECT_CREDENTIALS = "INCORRECT_CREDENTIALS";
	
	public void handleAction(FlowModel flowModel) {
		String endpoint = flowModel.getAsString(FlowModel.FLOW_PM_URL);
		
		String username = null;
		String password = null; 
		
		if (!flowModel.isEmpty(FlowModel.FLOW_PM_USERNAME)) {
			username = flowModel.getAsString(FlowModel.FLOW_PM_USERNAME);
			password = flowModel.getAsString(FlowModel.FLOW_PM_PASSWORD);
		}
		
		/* TODO USE PROXY setting must be configured in wizard */
		HttpPostResponse response = EasyHttpClient.post(endpoint, username, password, null, false);
		try {		
			if (response.wasSuccessful()) {
				flowModel.putValueToRootJson(RESULT_KEY, RESULT_VALUE_OK);
			} else if (response.wasUnauthorized()) {
				flowModel.putValueToRootJson(RESULT_KEY, RESULT_VALUE_INCORRECT_CREDENTIALS);
			} else {
				flowModel.putValueToRootJson(RESULT_KEY, RESULT_VALUE_SERVICE_DOWN);
			}
		} finally {
			response.closeResponseStream();
		}
	}
}
