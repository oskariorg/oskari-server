package fi.nls.oskari.user;

import fi.nls.oskari.domain.Role;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.service.UserService;

import java.util.List;
import java.util.Map;

import org.mindrot.jbcrypt.BCrypt;

public class DatabaseUserService extends UserService {
    private IbatisRoleService roleService = new IbatisRoleService();
    private IbatisUserService userService = new IbatisUserService();

    @Override
    public User getGuestUser() {
        User user = super.getGuestUser();
        user.addRole(roleService.findGuestRole());
        return user;
    }

    @Override
    public User login(String user, String pass) throws ServiceException {
        throw new ServiceException("Unsupported!");
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
        return userService.findAll();
    }

    @Override
    public User createUser(User user) throws ServiceException {
        userService.insert(user);
        return userService.findByUserName(user.getScreenname());
    }

    @Override
    public User modifyUser(User user) throws ServiceException {
        userService.update(user);
        return userService.find(user.getId());
    }

    @Override
    public void deleteUser(long id) throws ServiceException {
        userService.delete(id);
    }

    @Override
    public void setUserPassword(long id, String password) throws ServiceException {
        String hashed = BCrypt.hashpw(password, BCrypt.gensalt());
        userService.updatePassword(id, hashed);
    }

}
