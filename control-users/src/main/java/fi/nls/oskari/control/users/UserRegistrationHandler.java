package fi.nls.oskari.control.users;

import fi.nls.oskari.control.*;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.service.UserService;
import fi.nls.oskari.util.PropertyUtil;
import org.json.JSONException;
import org.json.JSONObject;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.users.model.Email;
import fi.nls.oskari.control.users.service.UserRegistrationService;
import fi.nls.oskari.control.users.service.MailSenderService;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.user.IbatisUserService;
import fi.nls.oskari.util.ResponseHelper;

/**
 * Note! This might change a bit to make it more RESTish instead of params to tell the operation
 */
@OskariActionRoute("UserRegistration")
public class UserRegistrationHandler extends ActionHandler {

	private static final String PARAM_REGISTER = "register";
	private static final String PARAM_EDIT = "edit";
	private static final String PARAM_UPDATE = "update";
	private static final String PARAM_DELETE = "delete";
	
    private static final String PARAM_FIRSTNAME = "firstname";
    private static final String PARAM_LASTNAME = "lastname";
    private static final String PARAM_USERNAME = "username";
    private static final String PARAM_EMAIL = "email";
    
    private UserService userService;
	private final UserRegistrationService registerTokenService = OskariComponentManager.getComponentOfType(UserRegistrationService.class);
    private final MailSenderService mailSenderService = new MailSenderService();
    private final IbatisUserService ibatisUserService = new IbatisUserService();

	public void init() {
		try {
			userService = UserService.getInstance();
		} catch (ServiceException ex) {
			throw new RuntimeException("Unable to get user service reference", ex);
		}
	}
    
	@Override
	public void handleAction(ActionParameters params) throws ActionException {
		if(!PropertyUtil.getOptional("allow.registration", false)) {
			throw new ActionDeniedException("Registration disabled");
		}
		// TODO: this check seems a bit weird, maybe do some other validation instead?
		if (getRequestParameterCount(params.getRequest().getQueryString()) != 1) {
			throw new ActionException("Request URL must contain ONLY ONE parameter.");
		}
					
		String requestEdit = params.getRequest().getParameter(PARAM_EDIT);	
		User user = new User();
		if (params.getHttpParam(PARAM_REGISTER) != null) {
			getUserParams(user, params);
			if (isEmailAlreadyExist(user.getEmail())) {
				throw new ActionException("Email already exists.");
			}
			if (isUsernameAlreadyExist(user.getScreenname())) {
				throw new ActionException("Username already exists.");
			}
			try {
				userService.createUser(user);
			} catch (ServiceException se) {			
				throw new ActionException(se.getMessage(), se);
			}
			
	    	Email emailToken = new Email();
	    	emailToken.setEmail(user.getEmail());
			emailToken.setScreenname(user.getScreenname());
	    	emailToken.setUuid(user.getUuid());
	    	emailToken.setExpiryTimestamp(RegistrationUtil.createExpiryTime());
	    	registerTokenService.addEmail(emailToken);
	    	
	    	mailSenderService.sendEmailForRegistrationActivation(user, RegistrationUtil.getServerAddress(params));
				    	
		} else if (requestEdit != null && !requestEdit.isEmpty()) {
			try {
				params.requireLoggedInUser();
				// we could propably just use params.getUser() instead of loading from db
				User retUser = userService.getUser(params.getUser().getId());
				ResponseHelper.writeResponse(params, user2Json(retUser));
			} catch (ServiceException se) {			
				throw new ActionException(se.getMessage(), se);
			}
			
		} else if (params.getHttpParam(PARAM_UPDATE) != null) {
			params.requireLoggedInUser();
			User sessionUser = params.getUser();
			getUserParams(user, params);
			user.setId(sessionUser.getId());
			try {
				User retUser = ibatisUserService.find(user.getId());
				if (retUser == null)
					throw new ActionException("User doesn't exist.");
				if (!retUser.getEmail().equals(user.getEmail()) && isEmailAlreadyExist(user.getEmail()))
					throw new ActionException("Email already exists.");
				user.setScreenname(retUser.getScreenname());
				userService.modifyUser(user);				
			} catch (ServiceException se) {			
				throw new ActionException(se.getMessage(), se);				
			} 
			
		} else if (params.getHttpParam(PARAM_DELETE) != null) {
			User sessionUser = params.getUser();
			params.requireLoggedInUser();
			try {
				User retUser = ibatisUserService.find(sessionUser.getId());
				if (retUser == null)
					throw new ActionException("User doesn't exist.");					
				userService.deleteUser(sessionUser.getId());				
			} catch (ServiceException se) {			
				throw new ActionException(se.getMessage(), se);				
			} 
		
		} else {
			throw new ActionException("Request URL should contain ONLY ONE: Either 'register' OR "
					+ "'edit' OR 'update' OR 'delete'.");
		}
	}
	
	private final boolean isEmailAlreadyExist(final String emailAddress) {
		if (registerTokenService.findUsernameForEmail(emailAddress) != null)
			return true;
		else 
			return false;
	}
	
	private final boolean isUsernameAlreadyExist(final String username) {
		if (registerTokenService.findEmailForUsername(username) != null)
			return true;
		else 
			return false;
	}
	
	private void getUserParams(User user, ActionParameters params) throws ActionParamsException {
        user.setFirstname(params.getRequiredParam(PARAM_FIRSTNAME));
        user.setLastname(params.getRequiredParam(PARAM_LASTNAME));       
        user.setEmail(params.getRequiredParam(PARAM_EMAIL));
        //check is done because while updating user info, username is not changed.
        if (params.getRequest().getParameter(PARAM_USERNAME) != null)
        	 user.setScreenname(params.getRequest().getParameter(PARAM_USERNAME));
    }

    
    private JSONObject user2Json(User user) throws ActionException {
		if (user == null) {
			throw new ActionParamsException("User doesn't exists.");
		}
		try {
			JSONObject uo = new JSONObject();
			uo.put("id", user.getId());
			uo.put("firstName", user.getFirstname());
			uo.put("lastName", user.getLastname());
			uo.put("userName", user.getScreenname());
			uo.put("email", user.getEmail());
			return uo;
		} catch (JSONException je) {
			throw new ActionException(je.getMessage(), je);
		}
    }
    
    public final int getRequestParameterCount(String query) {   
    	int count = 0;
    	for (int i = 0; i < query.length(); i++){
    		if (query.charAt(i) == '&')
    			++count;
    	}
    	return count;
    }
    
}
