package fi.nls.oskari.service;

import fi.nls.oskari.domain.Role;
import fi.nls.oskari.domain.User;

import java.util.Map;

/**
 * Dummy implementation for UserService
 *
 * ONLY USE FOR TESTING!!!
 */
public class DummyUserService extends UserService {
    private final static int GUEST_ROLE = 1;
    private final static int USER_ROLE = 2;
    private final static int ADMIN_ROLE = 3;

    public User getGuestUser() {
        final User user = super.getGuestUser();
        user.addRole(GUEST_ROLE, "Guest");
        return user;
    }

    @Override
    public User login(String username, String password) {
        User user = new User();

        if (username.equals("user")) {
            user.addRole(USER_ROLE, "User");
        } else if (username.equals("admin")) {
            user.addRole(USER_ROLE, "User");
            user.addRole(ADMIN_ROLE, "Administrator");
        } else {
            return getGuestUser();
        }
        return user;
    }

    @Override
    public Role[] getRoles(Map<Object, Object> platformSpecificParams)
            throws ServiceException {
        return new Role[0];
    }

    @Override
    public User getUser(String username) throws ServiceException {
        return login(username, null);
    }
}
