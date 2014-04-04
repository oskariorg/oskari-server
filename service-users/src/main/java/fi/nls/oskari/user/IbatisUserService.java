package fi.nls.oskari.user;

import fi.nls.oskari.domain.Role;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.service.UserService;

import java.util.List;
import java.util.Map;

public class IbatisUserService extends UserService {
    private IbatisRoleService roleService = new IbatisRoleService();

    @Override
    public User login(String user, String pass) throws ServiceException {
        return null;
    }

    @Override
    public Role[] getRoles(Map<Object, Object> platformSpecificParams) throws ServiceException {
        List<Role> roleList = roleService.findAll();
        return roleList.toArray(new Role[roleList.size()]);
    }

    @Override
    public User getUser(String username) throws ServiceException {
        return null;
    }
}
