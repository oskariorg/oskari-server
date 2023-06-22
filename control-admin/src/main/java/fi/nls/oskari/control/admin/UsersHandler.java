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
import org.oskari.user.util.UserHelper;

import java.util.Collections;
import java.util.List;

@OskariActionRoute("Users")
public class UsersHandler extends RestActionHandler {

    private static final Logger LOG = LogFactory.getLogger(UsersHandler.class);
    private UserService userService = null;

    private static final String PARAM_ID = "id";
    private static final String PARAM_ROLE_ID = "roleId";
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
        JSONObject response = new JSONObject();
        List<User> users = Collections.emptyList();
        long id = params.getHttpParam(PARAM_ID, -1L);
        long roleId = params.getHttpParam(PARAM_ROLE_ID, -1L);
        int limit = params.getHttpParam(PARAM_LIMIT, -1);
        int offset = params.getHttpParam(PARAM_OFFSET, 0);
        String search = params.getHttpParam(PARAM_SEARCH, "");
        try {
            if (roleId > 0) {
                LOG.debug("handleGet by role id", roleId);
                users = userService.getUsersByRole(roleId);
            } else if (id > 0) {
                LOG.debug("handleGet: has id", id);
                User user = userService.getUser(id);
                response = user2Json(user);
            } else {
                LOG.debug("handleGet: no id");
                users = userService.getUsersWithRoles(limit, offset, search);
                if (search != null && search.trim() != "") {
                    response.put("total_count", userService.getUserSearchCount(search));
                } else {
                    response.put("total_count", userService.getUserCount());
                }
            }
            if (!users.isEmpty()) {
                JSONArray arr = new JSONArray();
                response.put("users", arr);
                for (User user : users) {
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
        // modify existing user
        boolean extUsers = UsersBundleHandler.isUsersFromExternalSource();
        long userId = params.getRequiredParamLong(PARAM_ID);
        User user;
        String password = null;
        if (extUsers) {
            user = findExistingUser(userId);
        } else {
            user = getUserFromParams(params);
            password = params.getHttpParam(PARAM_PASSWORD);
        }
        String[] roles = params.getRequest().getParameterValues("roles");

        User retUser;
        try {
            retUser = userService.modifyUserwithRoles(user, roles);
            LOG.debug("done modifying user");
            if (!extUsers) {
                if (password != null && !password.trim().isEmpty()) {
                    if (!UserHelper.isPasswordOk(password)) {
                        throw new ActionParamsException("Password too weak");
                    }
                    userService.updateUserPassword(retUser.getScreenname(), password);
                }
            }
        } catch (ServiceException se) {
            throw new ActionException(se.getMessage(), se);
        }

        AuditLog.user(params.getClientIp(), params.getUser())
                .withParam("email", user.getEmail())
                .updated(AuditLog.ResourceType.USER);
        try {
            ResponseHelper.writeResponse(params, user2Json(retUser));
        } catch (JSONException je) {
            throw new ActionException(je.getMessage(), je);
        }
    }

    private User findExistingUser(long id) throws ActionParamsException {
        try {
            return userService.getUser(id);
        } catch (ServiceException e) {
            throw new ActionParamsException("Error loading user with id: " + id);
        }
    }
    private User getUserFromParams(ActionParameters params) throws ActionParamsException {
        User user = new User();
        getUserParams(user, params);
        return user;
    }

    @Override
    public void handlePut(ActionParameters params) throws ActionException {
        LOG.debug("handlePut");
        if (UsersBundleHandler.isUsersFromExternalSource()) {
            throw new ActionParamsException("Users from external source, adding is disabled");
        }
        User user = new User();
        getUserParams(user, params);
        String password = params.getRequiredParam(PARAM_PASSWORD);
        String[] roles = params.getRequest().getParameterValues("roles");

        if (!UserHelper.isPasswordOk(password)) {
            throw new ActionParamsException("Password too weak");
        }

        User retUser = null;
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

    private void getUserParams(User user, ActionParameters params) throws ActionParamsException {
        user.setId(params.getHttpParam(PARAM_ID, -1L));
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