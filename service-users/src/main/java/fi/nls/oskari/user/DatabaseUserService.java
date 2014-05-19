package fi.nls.oskari.user;

import fi.nls.oskari.domain.Role;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.service.UserService;
import org.apache.commons.codec.digest.DigestUtils;

import java.util.List;
import java.util.Map;

public class DatabaseUserService extends UserService {
    private IbatisRoleService roleService = new IbatisRoleService();
    private IbatisUserService userService = new IbatisUserService();

    private static final Logger log = LogFactory.getLogger(DatabaseUserService.class);

    @Override
    public User getGuestUser() {
        User user = super.getGuestUser();
        user.addRole(roleService.findGuestRole());
        return user;
    }

    @Override
    public User login(final String user, final String pass) throws ServiceException {
        try {
            final String hashedPass = "MD5:" + DigestUtils.md5Hex(pass);
            final String username = userService.login(user, hashedPass);
            log.debug("Tried to login user with:", user, "/", pass, "-> ", hashedPass, "- Got username:", username);
            if(username == null) {
                return null;
            }
            return getUser(username);
        }
        catch (Exception ex) {
            throw new ServiceException("Unable to handle login", ex);
        }
    }

    @Override
    public Role[] getRoles(Map<Object, Object> platformSpecificParams) throws ServiceException {
        List<Role> roleList = roleService.findAll();
        return roleList.toArray(new Role[roleList.size()]);
    }

    @Override
    public User getUser(String username) throws ServiceException {
        return userService.findByUserName(username);
    }

    @Override
    public User getUser(long id) throws ServiceException {
        return userService.find(id);
    }

    @Override
    public List<User> getUsers() throws ServiceException {
        log.info("getUsers");
        return userService.findAll();
    }

    @Override
    public User createUser(User user) throws ServiceException {
        log.debug("createUser");
        if(user.getUuid() == null || user.getUuid().isEmpty()) {
            user.setUuid(generateUuid());
        }
        Long id = userService.addUser(user);
        return userService.find(id);
    }

    @Override
    public User modifyUser(User user) throws ServiceException {
        log.debug("modifyUser");
        userService.updateUser(user);
        return userService.find(user.getId());
    }

    @Override
    public void deleteUser(long id) throws ServiceException {
        log.debug("deleteUser");
        User user = userService.find(id);
        if (user != null) {
            userService.deletePassword(user.getScreenname());
            userService.delete(id);
        }
    }

    @Override
    public void setUserPassword(String username, String password) throws ServiceException {
        String hashed = "MD5:" + DigestUtils.md5Hex(password);
        userService.setPassword(username, hashed);
    }

    @Override
    public void updateUserPassword(String username, String password) throws ServiceException {
        String hashed = "MD5:" + DigestUtils.md5Hex(password);
        userService.updatePassword(username, hashed);
    }

}
