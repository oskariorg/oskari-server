package fi.nls.oskari.control.users;

import org.json.JSONException;
import org.json.JSONObject;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.ResponseHelper;

@OskariActionRoute("UserRegistration")
public class UserRegistrationHandler extends ActionHandler {

	private static final Logger log = LogFactory.getLogger(UserRegistrationHandler.class);
	
    private static final String PARAM_FIRSTNAME = "firstname";
    private static final String PARAM_LASTNAME = "lastname";
    private static final String PARAM_SCREENNAME = "username";
    private static final String PARAM_EMAIL = "email";
    
	@Override
	public void handleAction(ActionParameters params) throws ActionException {
		User user = new User();
		System.out.println("params: " + params.getRequiredParam(PARAM_FIRSTNAME));
		getUserParams(user, params);
		System.out.println("Test: " + user.getEmail());
		
		JSONObject result = new JSONObject();
        try {
            result.put("status", "SUCCESS");
        } catch (JSONException e) {
            throw new ActionException("Could not construct JSON", e);
        }
        ResponseHelper.writeResponse(params, result);
		
	}
	
	 private void getUserParams(User user, ActionParameters params) throws ActionParamsException {
        user.setFirstname(params.getRequiredParam(PARAM_FIRSTNAME));
        user.setLastname(params.getRequiredParam(PARAM_LASTNAME));
        user.setScreenname(params.getRequiredParam(PARAM_SCREENNAME));
        user.setEmail(params.getRequiredParam(PARAM_EMAIL));
    }
	 
}
