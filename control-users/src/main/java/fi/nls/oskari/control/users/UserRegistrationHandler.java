package fi.nls.oskari.control.users;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.*;
import fi.nls.oskari.control.users.model.EmailToken;
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
	private static final String ATTR_PARAM_PREFIX = "user_";
    
    private UserService userService;
	private UserRegistrationService registerTokenService = null;
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
    public void handleGet(ActionParameters params) throws ActionException {
        // check if username is reserved
        if (registerTokenService.isUsernameReserved(params.getRequiredParam("username"))) {
            throw new ActionParamsException("Username already exists.");
        }
        ResponseHelper.writeResponse(params, "OK");
    }

    @Override
	public void handlePost(ActionParameters params) throws ActionException {
		if(!params.getUser().isGuest()) {
			throw new ActionDeniedException("Registration expects guest user");
		}
        final String uuid = params.getRequiredParam("uuid");
        final String password = params.getRequiredParam("password");
        if(!RegistrationUtil.isPasswordOk(password)) {
            throw new ActionParamsException("Password too weak");
        }

        // uuid:a5f1a383-47d5-458c-8373-efbc10cdac16
        EmailToken token = registerTokenService.findByToken(uuid);
        if(token == null) {
            // "Please restart the registration process"
            throw new ActionParamsException("Unknown token");
        }
        // check expiration here
        if(token.hasExpired()) {
            // "Please restart the registration process"
            throw new ActionParamsException("Token expired");
        }
        if (isEmailRegistered(token.getEmail())) {
            // "You can use forgot password feature to reset the password"
            throw new ActionParamsException("User already exists.");
        }
        User user = new User();
        getUserParams(user, params);
        user.setEmail(token.getEmail());

		if (registerTokenService.isUsernameReserved(user.getScreenname())) {
			throw new ActionParamsException("Username already exists.");
		}
		try {
			user.addRole(defaultRole);
			userService.createUser(user);
            // add password
            userService.setUserPassword(user.getScreenname(), password);
            // cleanup the token
            registerTokenService.removeTokenByUUID(uuid);
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
            // logout for current user
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
		try {
			userService.modifyUser(user);
		} catch (ServiceException se) {
			throw new ActionException(se.getMessage(), se);
		}

		ResponseHelper.writeResponse(params, user2Json(user));
	}
	
	private final boolean isEmailRegistered(final String emailAddress) {
		return registerTokenService.findUsernameForEmail(emailAddress) != null;
	}

	private void getUserParams(User user, ActionParameters params) throws ActionParamsException {
        user.setFirstname(params.getRequiredParam(PARAM_FIRSTNAME));
        user.setLastname(params.getRequiredParam(PARAM_LASTNAME));
        //check is done because while updating user info, username is not changed.
		if(user.getId() == -1) {
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
