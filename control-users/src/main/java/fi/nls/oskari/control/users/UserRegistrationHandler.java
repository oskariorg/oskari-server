package fi.nls.oskari.control.users;

import org.json.JSONException;
import org.json.JSONObject;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.control.users.service.IbatisEmailService;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.user.DatabaseUserService;
import fi.nls.oskari.util.ResponseHelper;

@OskariActionRoute("UserRegistration")
public class UserRegistrationHandler extends ActionHandler {

	private static final Logger log = LogFactory.getLogger(UserRegistrationHandler.class);
	
    private static final String PARAM_FIRSTNAME = "firstname";
    private static final String PARAM_LASTNAME = "lastname";
    private static final String PARAM_SCREENNAME = "username";
    private static final String PARAM_EMAIL = "email";
    
    private DatabaseUserService userService = new DatabaseUserService();
    private IbatisEmailService emailService = new IbatisEmailService();
    
	@Override
	public void handleAction(ActionParameters params) throws ActionException {
		User user = new User();
		getUserParams(user, params);
		
		if (isEmailAlreadyExist(user.getEmail()) || isUsernameAlreadyExist(user.getScreenname())) {
			throw new ActionException("Either username or email already exists.");
		}
		
		try {
			userService.createUser(user);
		} catch (ServiceException se) {			
			throw new ActionException(se.getMessage(), se);
		}
		
		JSONObject result = new JSONObject();
        try {
            result.put("status", "SUCCESS");
        } catch (JSONException e) {
            throw new ActionException("Could not construct JSON", e);
        }
        ResponseHelper.writeResponse(params, result);
		
	}
		
	private final boolean isEmailAlreadyExist(final String emailAddress) {
		if (emailService.findUsernameForEmail(emailAddress) != null)
			return true;
		else 
			return false;
	}
	
	private final boolean isUsernameAlreadyExist(final String username) {
		if (emailService.findEmailForUsername(username) != null)
			return true;
		else 
			return false;
	}
	
	private void getUserParams(User user, ActionParameters params) throws ActionParamsException {
        user.setFirstname(params.getRequiredParam(PARAM_FIRSTNAME));
        user.setLastname(params.getRequiredParam(PARAM_LASTNAME));
        user.setScreenname(params.getRequiredParam(PARAM_SCREENNAME));
        user.setEmail(params.getRequiredParam(PARAM_EMAIL));
    }
	 
}
