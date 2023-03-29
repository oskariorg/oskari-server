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
import org.oskari.log.AuditLog;
import org.oskari.user.util.PasswordRules;

import java.util.List;

@OskariActionRoute("Users")
public class UsersHandler extends RestActionHandler {

    private static final Logger LOG = LogFactory.getLogger(UsersHandler.class);
    private UserService userService = null;

    private static final String PARAM_ID = "id";
    private static final String PARAM_FIRSTNAME = "firstName";
    private static final String PARAM_LASTNAME = "lastName";
    private static final String PARAM_SCREENNAME = "user";
    private static final String PARAM_PASSWORD = "pass";
    private static final String PARAM_EMAIL = "email";
    private static final String PARAM_LIMIT = "limit";
    private static final String PARAM_OFFSET = "offset";
    private static final String PARAM_SEARCH = "search";

    @Override
    public void init() {
        try {
            userService = UserService.getInstance();
        } catch (ServiceException se) {
            LOG.error(se, "Unable to initialize User service!");
        }
    }

    @Override
    public void handleGet(ActionParameters params) throws ActionException {
        final JSONObject response;
        long id = getId(params);
        long limit = params.getHttpParam(PARAM_LIMIT, 0);
        long offset = params.getHttpParam(PARAM_OFFSET, 0);
        String search = params.getHttpParam(PARAM_SEARCH);
        try {
            if (id > -1) {
                LOG.debug("handleGet: has id", id);
                User user = userService.getUser(id);
                response = user2Json(user);
            } else {
                LOG.debug("handleGet: no id");
                response = new JSONObject();
                JSONArray arr = new JSONArray();
                response.put("users", arr);

                List<User> newUsers = userService.getUsersWithRoles(limit, offset, search);

                if (search != null && search.trim() != "") {
                    response.put("total_count", userService.getUserSearchCount(search));
                } else {
                    response.put("total_count", userService.getUserCount());
                }

                for (User user : newUsers) {
                    arr.put(user2Json(user));
                }
            }
        } catch (ServiceException | JSONException se) {
            throw new ActionException(se.getMessage(), se);
        }
        ResponseHelper.writeResponse(params, response);
    }

    @Override
    public void handlePost(ActionParameters params) throws ActionException {
        User user = new User();
        getUserParams(user, params);
        String[] roles = params.getRequest().getParameterValues("roles");
        String password = params.getHttpParam(PARAM_PASSWORD);
        User retUser = null;

        AuditLog audit = AuditLog.user(params.getClientIp(), params.getUser())
                .withParam("email", user.getEmail());

        try {
            if (user.getId() > -1) {
                //retUser = userService.modifyUser(user);
                LOG.debug("roles size: " + roles.length);
                retUser = userService.modifyUserwithRoles(user, roles);
                LOG.debug("done modifying user");
                if (password != null && !password.trim().isEmpty()) {
                    if (!PasswordRules.isPasswordOk(password)) {
                        throw new ActionParamsException("Password too weak");
                    } else {
                        userService.updateUserPassword(retUser.getScreenname(), password);
                    }
                }
                audit.updated(AuditLog.ResourceType.USER);
            } else {
                LOG.debug("NOW IN POST and creating a new user!!!!!!!!!!!!!");
                if (password == null || password.trim().isEmpty()) {
                    throw new ActionException("Parameter 'password' not found.");
                }
                if (!PasswordRules.isPasswordOk(password)) {
                    throw new ActionParamsException("Password too weak");
                }
                retUser = userService.createUser(user);
                userService.setUserPassword(retUser.getScreenname(), password);
                audit.added(AuditLog.ResourceType.USER);
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
        LOG.debug("handlePut");
        User user = new User();
        getUserParams(user, params);
        String password = params.getRequiredParam(PARAM_PASSWORD);
        String[] roles = params.getRequest().getParameterValues("roles");
        User retUser = null;

        if (!PasswordRules.isPasswordOk(password)) {
            throw new ActionParamsException("Password too weak");
        }

        try {
            retUser = userService.createUser(user, roles);
            userService.setUserPassword(retUser.getScreenname(), password);
        } catch (ServiceException se) {
            throw new ActionException(se.getMessage(), se);
        }
        AuditLog.user(params.getClientIp(), params.getUser())
                .withParam("email", user.getEmail())
                .added(AuditLog.ResourceType.USER);
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
        LOG.debug("handleDelete");
        long id = params.getRequiredParamLong(PARAM_ID);
        try {
            userService.deleteUser(id);
            AuditLog.user(params.getClientIp(), params.getUser())
                    .withParam("id", id)
                    .deleted(AuditLog.ResourceType.USER);
        } catch (ServiceException se) {
            throw new ActionException(se.getMessage(), se);
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
        return params.getHttpParam(PARAM_ID, -1);
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
        for (Role role : user.getRoles()) {
            rolesArray.put(role.getId());
        }
        JSONHelper.put(uo, "roles", rolesArray);

        return uo;
    }

}