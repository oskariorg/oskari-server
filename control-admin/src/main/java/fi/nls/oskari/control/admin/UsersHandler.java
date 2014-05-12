package fi.nls.oskari.control.admin;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.*;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.service.UserService;

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
        isAdmin(params);
        long id = getId(params);
        try {
            if (id > -1) {
                User user = userService.getUser(id);
            } else {
                List<User> users = userService.getUsers();
            }
        } catch (ServiceException se) {
            throw new ActionException(se.getMessage(), se);
        }
    }

    @Override
    public void handlePut(ActionParameters params) throws ActionException {
        isAdmin(params);
        User user = new User();
        getUserParams(user, params);
        String password = params.getHttpParam(PARAM_PASSWORD);
        User retUser = null;
        try {
            if (user.getId() > -1) {
                retUser = userService.modifyUser(user);
            } else {
                if (password == null || password.length() == 0) {
                    throw new ActionException("Parameter 'password' not found.");
                }
                retUser = userService.createUser(user);
            }
        } catch (ServiceException se) {
            throw new ActionException(se.getMessage(), se);
        }
        if (password != null) {
            try {
                userService.setUserPassword(retUser.getId(), password);
            } catch (ServiceException se) {
                throw new ActionException(se.getMessage(), se);
            }
        }
    }

    @Override
    public void handlePost(ActionParameters params) throws ActionException {
        isAdmin(params);
        User user = new User();
        getUserParams(user, params);
        String password = params.getHttpParam(PARAM_PASSWORD);
        if (password == null || password.length() == 0) {
            throw new ActionException("Parameter 'password' not found.");
        }
        User retUser = null;
        try {
            retUser = userService.createUser(user);
            userService.setUserPassword(retUser.getId(), password);
        } catch (ServiceException se) {
            throw new ActionException(se.getMessage(), se);
        }
    }

    @Override
    public void handleDelete(ActionParameters params) throws ActionException {
        isAdmin(params);
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
    }

    private void isAdmin(ActionParameters params) throws ActionException {
        if (!params.getUser().isAdmin()) {
            throw new ActionDeniedException("Admin only");
        }
    }

}