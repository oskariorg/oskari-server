package fi.nls.oskari.control.users;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.*;
import fi.nls.oskari.control.users.model.Email;
import fi.nls.oskari.control.users.service.MailSenderService;
import fi.nls.oskari.control.users.service.UserRegistrationService;
import fi.nls.oskari.domain.Role;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.service.UserService;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.ResponseHelper;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.Map;

/**
 * Note! This might change a bit to make it more RESTish instead of params to tell the operation
 */
@OskariActionRoute("UserRegistration")
public class UserRegistrationHandler extends RestActionHandler {

	private static final Logger LOG = LogFactory.getLogger(UserRegistrationHandler.class);
    private static final String PARAM_FIRSTNAME = "firstname";
    private static final String PARAM_LASTNAME = "lastname";
    private static final String PARAM_USERNAME = "username";
    private static final String PARAM_EMAIL = "email";
	private static final String ATTR_PARAM_PREFIX = "user_";
    
    private UserService userService;
	private UserRegistrationService registerTokenService = null;
    private final MailSenderService mailSenderService = new MailSenderService();
	private Role defaultRole = null;

	public void init() {
		try {
			userService = UserService.getInstance();
			// configured with "oskari.user.role.loggedIn" property
			defaultRole = Role.getDefaultUserRole();
		} catch (ServiceException ex) {
			throw new RuntimeException("Unable to get user service reference", ex);
		}
		registerTokenService = OskariComponentManager.getComponentOfType(UserRegistrationService.class);
	}

	@Override
	public void preProcess(ActionParameters params) throws ActionException {
		if(!RegistrationUtil.isEnabled()) {
			throw new ActionDeniedException("Registration disabled");
		}
	}

	@Override
	public void handlePost(ActionParameters params) throws ActionException {
		if(!params.getUser().isGuest()) {
			throw new ActionDeniedException("Registration expects guest user");
		}
		User user = new User();
		getUserParams(user, params);
		String language = params.getLocale().getLanguage();
		if (isEmailAlreadyExist(user.getEmail())) {
			try {
				mailSenderService.sendEmailAlreadyExists(user, RegistrationUtil.getServerAddress(params), language);
			} catch (ServiceException se) {
				//Do nothing, email already exists and tried to send email about that failed.
			}
			LOG.warn("Tried to register with known email:", user.getEmail());
			// don't bleed out the information
			throw new ActionParamsException("Error registering.");
		}
		if (isUsernameAlreadyExist(user.getScreenname())) {
			throw new ActionException("Username already exists.");
		}
		try {
			user.addRole(defaultRole);
			userService.createUser(user);
		} catch (ServiceException se) {
			throw new ActionException(se.getMessage(), se);
		}
		Email emailToken = new Email();
		emailToken.setEmail(user.getEmail());
		emailToken.setScreenname(user.getScreenname());
		emailToken.setUuid(user.getUuid());
		try {
			emailToken.setExpiryTimestamp(RegistrationUtil.createExpiryTime());
		}
		catch (Exception e) {
			throw new ActionException("Unable to read the configuration properties.");
		}
		registerTokenService.addEmail(emailToken);

		try {
			mailSenderService.sendEmailForRegistrationActivation(user, RegistrationUtil.getServerAddress(params), language);
		} catch (ServiceException se) {
			throw new ActionException(se.getMessage(), se);
		}
		ResponseHelper.writeResponse(params, user2Json(user));
	}

	@Override
	public void handleDelete(ActionParameters params) throws ActionException {
		// remove
		params.requireLoggedInUser();
		User sessionUser = params.getUser();
		try {
			User retUser = userService.getUser(sessionUser.getId());
			if (retUser == null) {
				throw new ActionParamsException("User doesn't exist.");
			}
			userService.deleteUser(sessionUser.getId());
			params.getRequest().getSession().invalidate();
		} catch (ServiceException se) {
			throw new ActionException(se.getMessage(), se);
		}
	}
	@Override
	public void handlePut(ActionParameters params) throws ActionException {
		// edit
		params.requireLoggedInUser();
		User user = params.getUser();
		getUserParams(user, params);
		User sessionUser = params.getUser();
		getUserParams(user, params);
		user.setId(sessionUser.getId());
		try {
			User retUser = userService.getUser(user.getId());
			if (retUser == null)
				throw new ActionException("User doesn't exist.");
			if (!retUser.getEmail().equals(user.getEmail()) && isEmailAlreadyExist(user.getEmail()))
				throw new ActionException("Email already exists.");
			user.setScreenname(retUser.getScreenname());
			userService.modifyUser(user);
		} catch (ServiceException se) {
			throw new ActionException(se.getMessage(), se);
		}

		ResponseHelper.writeResponse(params, user2Json(user));
	}
	
	private final boolean isEmailAlreadyExist(final String emailAddress) {
		return registerTokenService.findUsernameForEmail(emailAddress) != null;
	}
	
	private final boolean isUsernameAlreadyExist(final String username) {
		return registerTokenService.findEmailForUsername(username) != null;
	}
	
	private void getUserParams(User user, ActionParameters params) throws ActionParamsException {
        user.setFirstname(params.getRequiredParam(PARAM_FIRSTNAME));
        user.setLastname(params.getRequiredParam(PARAM_LASTNAME));       
        user.setEmail(params.getRequiredParam(PARAM_EMAIL));
        //check is done because while updating user info, username is not changed.
		if(user.isGuest()) {
			user.setScreenname(params.getRequiredParam(PARAM_USERNAME));
		}
		// loop parameters and add the rest starting with user_
		for(Map.Entry<String, String[]> param : params.getRequest().getParameterMap().entrySet()) {
			if(!param.getKey().startsWith(ATTR_PARAM_PREFIX)) {
				continue;
			}
			final String key = param.getKey().substring(ATTR_PARAM_PREFIX.length());
			if(key.isEmpty()) {
				continue;
			}
			user.setAttribute(key, params.getHttpParam(key));
		}
    }

    private JSONObject user2Json(User user) throws ActionException {
		if (user == null) {
			throw new ActionParamsException("User doesn't exists.");
		}
		JSONObject json = user.toJSON();
		// include attributes prefixed with user_
		JSONObject attrs = user.getAttributesJSON();
		Iterator<String> it = attrs.keys();
		while(it.hasNext()) {
			final String key = it.next();
			JSONHelper.putValue(json, ATTR_PARAM_PREFIX + key, attrs.opt(key));
		}
		// roles can be skipped here.
		json.remove("roles");
		return json;
    }
}
