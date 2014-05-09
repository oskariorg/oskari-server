package fi.nls.oskari.user;

import fi.nls.oskari.domain.Role;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.service.db.BaseIbatisService;

import java.util.List;

public class IbatisUserService extends BaseIbatisService<User> {
    private IbatisRoleService roleService = new IbatisRoleService();

    @Override
    protected String getNameSpace() {
        return "Users";
    }

    public User find(long id) {
        return queryForObject(getNameSpace() + ".findById", id);
    }

    public User findByUserName(String username) {
        User user = queryForObject(getNameSpace() + ".findByUserName", username);
        List<Role> roleList = roleService.findByUserName(username);
        for(Role role : roleList) {
            user.addRole(role.getId(), role.getName());
        }
        return user;
    }

    public void delete(long id) {
        delete(getNameSpace() + ".deleteById", id);
    }

    public void updatePassword(long id, String password) {

    }

}
