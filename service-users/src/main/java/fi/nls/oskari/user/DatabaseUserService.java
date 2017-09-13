package fi.nls.oskari.user;

import fi.nls.oskari.domain.Role;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.service.UserService;
import fi.nls.oskari.service.db.UserContentService;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.security.crypto.bcrypt.BCrypt;

import java.util.*;

public class DatabaseUserService extends UserService {
    private MybatisRoleService roleService = new MybatisRoleService();
    private MybatisUserService userService = new MybatisUserService();

    private static final String ERR_USER_MISSING = "User was null";
    private static final int BCRYPT_PASSWORD_LENGTH = 60;

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
            final String expectedHashedPassword = userService.getPassword(user);
            if (expectedHashedPassword == null) {
                return null;
            }
            
            final String username;
            if (expectedHashedPassword.startsWith("MD5:")) {
                final String hashedPass = "MD5:" + DigestUtils.md5Hex(pass);
                username = userService.login(user, hashedPass);
                log.debug("Tried to login user with:", user, "/", pass, "-> ", hashedPass, "- Got username:", username);
                if (username == null) {
                    return null;
                }
            }
            else if (expectedHashedPassword.length() == BCRYPT_PASSWORD_LENGTH) {
                log.debug("Tried to login user:", user, "/", pass, " with BCrypt password");
                if (!BCrypt.checkpw(pass, expectedHashedPassword)) {
                    return null;
                }
                username = user;
            }
            else {
                log.error("Unknown password hash format for user ", user);
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

    // TODO: make this part of the UserService interface
    public User getUserByEmail(String email) throws ServiceException {
        return userService.findByEmail(email);
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
        if(user == null) {
            throw new ServiceException(ERR_USER_MISSING);
        }
        if(user.getUuid() == null || user.getUuid().isEmpty()) {
            user.setUuid(generateUuid());
        }
        Long id = userService.addUser(user);
        
        for(String roleId : roleIds){
        	log.debug("roleId: " + roleId + " userId: " + id);
            roleService.linkRoleToUser(Long.valueOf(roleId), id);
        }    
        
        return userService.find(id);
    }


    /**
     * Only updates user information, NOT roles!
     * @param user Modified user
     * @return
     * @throws ServiceException
     */
    @Override
    public User modifyUser(User user) throws ServiceException {
        if(user == null) {
            throw new ServiceException(ERR_USER_MISSING);
        }
        log.debug("modifyUser");
        userService.updateUser(user);
        User retUser = userService.find(user.getId());
        List<Role> roles = roleService.findByUserId(user.getId());
        retUser.setRoles(new HashSet<Role>(roles));
        return retUser;
    }
    /**
     * Updates user information AND roles based on screenname! Creating both roles and user if they are not found in database.
     * @param user User details
     * @return saved user with populated role/user IDs.
     * @throws ServiceException if given user is null or something went wrong while updating the database
     */
    public User saveUser(final User user) throws ServiceException {
        if(user == null) {
            throw new ServiceException(ERR_USER_MISSING);
        }
        log.debug("Saving user:", user, "with roles:", user.getRoles());
        // ensure roles are in DB
        final Set<Role> roles = ensureRolesInDB(user.getRoles());
        user.setRoles(roles);
        // check if user details exist in DB
        final User dbUser = getUser(user.getScreenname());
        if(dbUser == null) {
            // not found from DB -> add user
            return createUser(user);
        }
        user.setId(dbUser.getId());
        // existing user -> update details in database
        final User savedUser = modifyUserwithRoles(user, roles);
        log.debug("Saved user:", user, "with roles:", user.getRoles());
        return savedUser;
    }

    private User modifyUserwithRoles(User user, Set<Role> roles) throws ServiceException {
        final String[] roleIds = new String[roles.size()];
        final Iterator<Role> it = roles.iterator();
        for(int i = 0; i < roleIds.length; ++i) {
            Role role = it.next();
            roleIds[i] = "" + role.getId();
        }
        return modifyUserwithRoles(user, roleIds);
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
            Map<String, UserContentService> userContentServices = OskariComponentManager.getComponentsOfType(UserContentService.class);
            String serviceClass = "";
            try {
                for (Map.Entry<String, UserContentService> userContentService : userContentServices.entrySet()) {
                    serviceClass = userContentService.getKey();
                    userContentService.getValue().deleteUserContent(user);
                }
                userService.deletePassword(user.getScreenname());
                roleService.deleteUsersRoles(id);
                userService.delete(id);
            }
            catch (Exception e) {
                log.error("Deleting user data failed in service:", serviceClass);
                throw new ServiceException("Deleting user data failed in service: " + serviceClass);
            }
        }
    }

    @Override
    public void setUserPassword(String username, String password) throws ServiceException {
        String hashed = BCrypt.hashpw(password, BCrypt.gensalt());
        userService.setPassword(username, hashed);
    }

    @Override
    public void updateUserPassword(String username, String password) throws ServiceException {
        String hashed = BCrypt.hashpw(password, BCrypt.gensalt());
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

    private Set<Role> ensureRolesInDB(final Set<Role> userRoles) throws ServiceException {
        final Role[] systemRoles = getRoles();
        final Set<Role> rolesToInsert = new HashSet<Role>(userRoles.size());
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
        return userRoles;
    }
    
}
