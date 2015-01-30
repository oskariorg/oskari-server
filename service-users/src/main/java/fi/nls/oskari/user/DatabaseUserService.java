package fi.nls.oskari.user;

import fi.nls.oskari.domain.Role;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.service.UserService;

import org.apache.commons.codec.digest.DigestUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    public List<User> getUsersWithRoles() throws ServiceException {
        log.info("getUsersWithRoles");
        List<User> users = userService.findAll();
        
        List<User> newUserList = new ArrayList<User>();
        
        for(User user : users){
        	log.debug("userid: " + user.getId());
        	List<Role> roles = roleService.findByUserId(user.getId());
        	Set<Role> hashsetRoles = new HashSet<Role>(roles);
        	user.setRoles(hashsetRoles);
        	newUserList.add(user);
        }
        
        return newUserList;
    }

    @Override
    public User createUser(User user) throws ServiceException {
        log.debug("createUser #######################");
        if(user.getUuid() == null || user.getUuid().isEmpty()) {
            user.setUuid(generateUuid());
        }
        Long id = userService.addUser(user);
        for(Role r : user.getRoles()) {
            roleService.linkRoleToNewUser(r.getId(), id);
        }
        return userService.find(id);
    }
    
    
    @Override
    public User createUser(User user, String[] roleIds) throws ServiceException {
        log.debug("createUser #######################");
        if(user.getUuid() == null || user.getUuid().isEmpty()) {
            user.setUuid(generateUuid());
        }
        Long id = userService.addUser(user);
//        for(Role r : user.getRoles()) {
//            roleService.linkRoleToNewUser(r.getId(), id);
//        }
        
        for(String roleId : roleIds){
        	log.debug("roleId: " + roleId + " userId: " + id);
            roleService.linkRoleToUser(Long.valueOf(roleId), id);
        }    
        
        return userService.find(id);
    }
    
    

    @Override
    public User modifyUser(User user) throws ServiceException {
        log.debug("modifyUser");
        userService.updateUser(user);
        User retUser = userService.find(user.getId());
        List<Role> roles = roleService.findByUserId(user.getId());
        retUser.setRoles(new HashSet<Role>(roles));
        return retUser;
    }

    @Override
    public User modifyUserwithRoles(User user, String[] roleIds) throws ServiceException {
        log.debug("modifyUserWithRoles");
        userService.updateUser(user);
        
        if(roleIds != null){
        	log.debug("starting to delete roles from a user");
            roleService.deleteUsersRoles(user.getId());
            log.debug("users roles deleted");
            for(String roleId : roleIds){
            	log.debug("roleId: " + roleId + " userId: " + user.getId());
                roleService.linkRoleToUser(Long.valueOf(roleId), user.getId());
            }
        }else{
        	log.debug("roleIds == null");
        }
        
        return userService.find(user.getId());
    }    
    
    @Override
    public void deleteUser(long id) throws ServiceException {
        log.debug("deleteUser");
        User user = userService.find(id);
        if (user != null) {
            userService.deletePassword(user.getScreenname());
            roleService.deleteUsersRoles(id);
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

    
    @Override
    public Role insertRole(String roleName) throws ServiceException {
    	log.debug("insertRole");
    	Role role = new Role();
    	role.setName(roleName);
    	log.debug("rolename: " + role.getName());
    	long id = roleService.insert(role);
    	role.setId(id);
    	return role;
    }
    
    
    @Override
    public String deleteRole(int roleId) throws ServiceException {
    	log.debug("deleteRole");
    	roleService.delete(roleId);
    	return null;
    }
   
    @Override
    public String modifyRole(String roleId, String userID) throws ServiceException {
    	log.debug("modifyRole");
    	return null;
    }

    public User updateOrAddUser(final User user) throws ServiceException {
        if(user == null) {
            return null;
        }

        ensureRolesInDB(user.getRoles());
        // check user
        final User dbUser = getUser(user.getScreenname());
        if(dbUser == null) {
            // not found from db -> add user
            return createUser(user);
        }
        // update user in database
        // TODO: Check the modifyUser implementation
        // it should update roles as well, but doesn't seem to
        return modifyUser(user);
    }

    private void ensureRolesInDB(final Set<Role> userRoles) throws ServiceException {
        Role[] systemRoles = getRoles();
        Set<Role> rolesToInsert = new HashSet<Role>(userRoles.size());
        for(Role userRole : userRoles) {
            boolean found = false;
            for(Role role : systemRoles) {
                if(role.getName().equals(userRole.getName())) {
                    // assign ID from role with same name in db
                    userRole.setId(role.getId());
                    found = true;
                }
            }
            if(!found) {
                rolesToInsert.add(userRole);
            }
        }
        // insert missing roles to DB and assign ID
        for(Role role : rolesToInsert) {
            Role dbRole = insertRole(role.getName());
            role.setId(dbRole.getId());
        }
    }
    
}
