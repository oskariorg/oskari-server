package fi.nls.oskari.service;

import fi.nls.oskari.domain.Role;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.util.PropertyUtil;

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
        user.addRole(GUEST_ROLE, PropertyUtil.get("oskari.user.role.guest", "Guest"));
        return user;
    }

    @Override
    public User login(String username, String password) {
        User user = new User();

        if (username.equals("user")) {
            user.addRole(USER_ROLE, PropertyUtil.get("oskari.user.role.user", "User"));
        } else if (username.equals("admin")) {
            user.addRole(USER_ROLE, PropertyUtil.get("oskari.user.role.user", "User"));
            user.addRole(ADMIN_ROLE, PropertyUtil.get("oskari.user.role.admin", "Admin"));
        } else {
            return getGuestUser();
        }
        return user;
    }

    @Override
    public Role[] getRoles(Map<Object, Object> platformSpecificParams)
            throws ServiceException {
        Role guest = new Role();
        guest.setId(GUEST_ROLE);
        guest.setName(PropertyUtil.get("oskari.user.role.guest", "Guest"));

        Role user = new Role();
        user.setId(USER_ROLE);
        user.setName(PropertyUtil.get("oskari.user.role.user", "User"));

        Role admin = new Role();
        admin.setId(ADMIN_ROLE);
        admin.setName(PropertyUtil.get("oskari.user.role.admin", "Admin"));
        return new Role[] {guest, user, admin};
    }

    @Override
    public User getUser(String username) throws ServiceException {
        return login(username, null);
    }
//    
//  
//  @Override
//  public String insertRole(String roleId, String userID) throws ServiceException {
//  	return null;
//  }
//  
//  
//  @Override
//  public String deleteRole(String roleId, String userID) throws ServiceException {
//  	return null;
//  }
//
//  
//  @Override
//  public String modifyRole(String roleId, String userID) throws ServiceException {
//  	return null;
//  }    
//  
      
}
