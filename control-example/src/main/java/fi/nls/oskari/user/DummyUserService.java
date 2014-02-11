package fi.nls.oskari.user;

import java.util.Map;

import fi.nls.oskari.domain.Role;

import fi.nls.oskari.domain.GuestUser;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.service.UserService;
import fi.nls.oskari.service.ServiceException;

public class DummyUserService extends UserService {
    private final static int GUEST_ROLE = 10110;
    private final static int MAP_ROLE = 2;
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
            user.addRole(MAP_ROLE, "User");
        } else if (username.equals("admin")) {
            user.addRole(MAP_ROLE, "User");
            user.addRole(ADMIN_ROLE, "Administrator");
        } else {
            user = new GuestUser();
            user.addRole(GUEST_ROLE, "Guest");
            // log.debug(username, password);
        }
        return user;
    }

    @Override
    public Role[] getRoles(Map<Object, Object> platformSpecificParams)
            throws ServiceException {
        return new Role[0];
    }

}
