package fi.nls.oskari.control.admin;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.*;
import fi.nls.oskari.domain.Role;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.service.UserService;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.ResponseHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

@OskariActionRoute("Users")
public class UsersHandler extends RestActionHandler {

    private Logger log = LogFactory.getLogger(UsersHandler.class);
    private UserService userService = null;

    private static final String PARAM_ID = "id";
    private static final String PARAM_FIRSTNAME = "firstName";
    private static final String PARAM_LASTNAME = "lastName";
    private static final String PARAM_SCREENNAME = "user";
    private static final String PARAM_PASSWORD = "pass";
    private static final String PARAM_EMAIL = "email";

    @Override
    public void init() {
        try {
            userService = UserService.getInstance();
        } catch (ServiceException se) {
            log.error(se, "Unable to initialize User service!");
        }
    }

    @Override
    public void handleGet(ActionParameters params) throws ActionException {
        log.info("handleGet");
        final JSONObject response;
        long id = getId(params);
        try {
            if (id > -1) {
                log.info("handleGet: has id");
                User user = userService.getUser(id);
                response = user2Json(user);
            } else {
                log.info("handleGet: no id");
                List<User> users = userService.getUsers();
                
                log.info("found: " + users.size() + "users");
                response = new JSONObject();
                JSONArray arr = new JSONArray();
                response.put("users", arr);
                
                
                List<User> newUsers = userService.getUsersWithRoles();
                
                for (User user : newUsers) {
                    arr.put(user2Json(user));
                }
            }
        } catch (ServiceException se) {
            throw new ActionException(se.getMessage(), se);
        } catch (JSONException je) {
            throw new ActionException(je.getMessage(), je);
        }
        log.info(response);
        ResponseHelper.writeResponse(params, response);
    }

    @Override
    public void handlePost(ActionParameters params) throws ActionException {
        log.debug("handlePost");
        User user = new User();
        getUserParams(user, params);
        String[] roles = params.getRequest().getParameterValues("roles");
        String password = params.getHttpParam(PARAM_PASSWORD);
        User retUser = null;
        try {
            if (user.getId() > -1) {
                //retUser = userService.modifyUser(user);
            	log.debug("roles size: " + roles.length);
            	retUser = userService.modifyUserwithRoles(user, roles);
            	log.debug("done modifying user");
                if (password != null && !"".equals(password.trim())) {
                    userService.updateUserPassword(retUser.getScreenname(), password);
                }
            } else {
            	log.debug("NOW IN POST and creating a new user!!!!!!!!!!!!!");
                if (password == null || password.trim().isEmpty()) {
                    throw new ActionException("Parameter 'password' not found.");
                }
                retUser = userService.createUser(user);
                userService.setUserPassword(retUser.getScreenname(), password);
            }

        } catch (ServiceException se) {
            throw new ActionException(se.getMessage(), se);
        }
        JSONObject response = null;
        try {
            response = user2Json(retUser);
        } catch (JSONException je) {
            throw new ActionException(je.getMessage(), je);
        }
        ResponseHelper.writeResponse(params, response);
    }

    @Override
    public void handlePut(ActionParameters params) throws ActionException {
        log.debug("handlePut");
        User user = new User();
        getUserParams(user, params);
        String password = params.getRequiredParam(PARAM_PASSWORD);
        String[] roles = params.getRequest().getParameterValues("roles");
        User retUser = null;
        try {
            retUser = userService.createUser(user, roles);
            userService.setUserPassword(retUser.getScreenname(), password);
        } catch (ServiceException se) {
            throw new ActionException(se.getMessage(), se);
        }
        JSONObject response = null;
        try {
            response = user2Json(retUser);
        } catch (JSONException je) {
            throw new ActionException(je.getMessage(), je);
        }
        ResponseHelper.writeResponse(params, response);
    }

    @Override
    public void handleDelete(ActionParameters params) throws ActionException {
        log.debug("handleDelete");
        long id = getId(params);
        if (id > -1) {
            try {
                userService.deleteUser(id);
            } catch (ServiceException se) {
                throw new ActionException(se.getMessage(), se);
            }
        } else {
            throw new ActionException("Parameter 'id' not found.");
        }
    }

    @Override
    public void preProcess(ActionParameters params) throws ActionException {
        if (!params.getUser().isAdmin()) {
            throw new ActionDeniedException("Admin only");
        }
    }

    private long getId(ActionParameters params) throws NumberFormatException {
        // see if params contains an ID
        long id = -1l;
        String idString = params.getHttpParam(PARAM_ID, "-1");
        if (idString != null && idString.length() > 0) {
            id = Long.parseLong(idString);
        }
        return id;
    }

    private void getUserParams(User user, ActionParameters params) throws ActionParamsException {
        user.setId(getId(params));
        user.setFirstname(params.getRequiredParam(PARAM_FIRSTNAME));
        user.setLastname(params.getRequiredParam(PARAM_LASTNAME));
        user.setScreenname(params.getRequiredParam(PARAM_SCREENNAME));
        user.setEmail(params.getRequiredParam(PARAM_EMAIL));
    }

    private JSONObject user2Json(User user) throws JSONException {
        // TODO: User.toJSON() should be used
        JSONObject uo = new JSONObject();
        uo.put("id", user.getId());
        uo.put("firstName", user.getFirstname());
        uo.put("lastName", user.getLastname());
        uo.put("user", user.getScreenname());
        uo.put("email", user.getEmail());
        
        JSONArray rolesArray = new JSONArray();
        for(Role role : user.getRoles()){
        	rolesArray.put(role.getId());
        }
        JSONHelper.put(uo, "roles", rolesArray);
        
        return uo;
    }

}