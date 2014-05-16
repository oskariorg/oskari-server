package fi.nls.oskari.user;

import fi.nls.oskari.domain.Role;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.service.db.BaseIbatisService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IbatisUserService extends BaseIbatisService<User> {
    private IbatisRoleService roleService = new IbatisRoleService();

    @Override
    protected String getNameSpace() {
        return "Users";
    }

    public Long addUser(User user) {
        return queryForObject(getNameSpace() + ".addUser", user);
    }

    public void updateUser(User user) {
        queryForObject(getNameSpace() + ".updateUser", user);
    }

    public User find(long id) {
        return queryForObject(getNameSpace() + ".findByUserId", id);
    }

    /**
     * Returns null if doesn't match any user or username for the user that was found
     * @param username
     * @param password
     * @return
     */
    public String login(final String username, final String password) {
        Map<String, String> params = new HashMap<String, String>(2);
        params.put("username", username);
        params.put("password", password);
        return (String) queryForRawObject(getNameSpace() + ".login", params);
    }

    public String getPassword(final String username) {
        Map<String, String> params = new HashMap<String, String>(2);
        params.put("username", username);
        return (String) queryForRawObject(getNameSpace() + ".getPassword", params);
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

    public void setPassword(String username, String password) {
        Map<String, String> params = new HashMap<String, String>(2);
        params.put("username", username);
        params.put("password", password);
        queryForObject(getNameSpace() + ".insertPassword", params);
    }

    public void updatePassword(String username, String password) {
        Map<String, String> params = new HashMap<String, String>(2);
        params.put("username", username);
        params.put("password", password);
        update(getNameSpace() + ".updatePassword", params);
    }

    public void deletePassword(String username) {
        Map<String, String> params = new HashMap<String, String>(2);
        params.put("username", username);
        delete(getNameSpace() + ".deletePassword", params);
    }

}
