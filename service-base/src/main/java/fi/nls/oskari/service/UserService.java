package fi.nls.oskari.service;

import fi.nls.oskari.domain.GuestUser;
import fi.nls.oskari.domain.Role;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.PropertyUtil;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Common interface for managing users.
 * TODO: this interface is still under development and new methods will propably be added when needed.
 */
public abstract class UserService {

    private static final Logger log = LogFactory.getLogger(UserService.class);
    private static UserService instance = null;

    /**
     * Returns a concrete implementation of UserService. Class to be returned is defined with property "oskari.user.service".
     * @return
     * @throws ServiceException if class cannot be
     */
    public static UserService getInstance() throws ServiceException {
        if(instance != null) {
            return instance;
        }
        final String className = PropertyUtil.getOptional("oskari.user.service");
        if(className == null) {
            throw new ServiceException("User service implementation not defined, add 'oskari.user.service' property with a value of fully qualified classname extending " + UserService.class.getCanonicalName());
        }
        try {
            instance = (UserService)Class.forName(className).newInstance();
            instance.init();
            return instance;
        } catch (Exception e) {
            throw new ServiceException("Error initializing UserService for classname: "+ className, e);
        }   
    }

    /**
     * Returns a Guest user. This method should be overridden by implementations to add a Guest role to the user.
     * Permission mappings can't be done correctly for guests if they have no roles.
     * @return
     */
    public User getGuestUser() {
        return new GuestUser();
    }

    /**
     * Optional initialize hook. Original init does nothing, so this is just a hook to do initialization on actual service implementations.
     * @throws ServiceException should be thrown if something goes wrong on init.
     */
    public void init() throws ServiceException {
        
    }
    /**
     * Optional destroy hook. Original teardown does nothing, so this is just a hook for cleaning up on actual service implementations.
     * @throws ServiceException should be thrown if something goes wrong on teardown.
     */
    public void teardown() throws ServiceException {

    }

    /**
     * Checks if the user exists in the system.
     * @param user username
     * @param pass password
     * @return logged in user if successful, null if user not found
     * @throws ServiceException if anything goes wrong internally.
     */
    public abstract User login(String user, String pass) throws ServiceException;

    
    /**
     * Inserts role to a user
     * @param roleId String
     * @return something
     * @throws ServiceException if anything goes wrong internally.
     */
    public Role insertRole(String roleId) throws ServiceException{
    	throw new ServiceException("Not Implemented Yet");
    }
    
    
    /**
     * Deletes role from a user
     * @param roleId String
     * @return something
     * @throws ServiceException if anything goes wrong internally.
     */
    public String deleteRole(int roleId) throws ServiceException{
    	throw new ServiceException("Not Implemented Yet");
    }
    
    /**
     * Modifies a users role
     * @param roleId String
     * @return something
     * @throws ServiceException if anything goes wrong internally.
     */
    public String modifyRole(String roleId, String userID) throws ServiceException{
    	throw new ServiceException("Not Implemented Yet");
    }
    
    /**
     * Returns all roles that exist in the system
     * @param platformSpecificParams optional platform specific parameters needed to get/filter roles. If implementation doesnt need any an empty map can be used.
     * @return all roles from the system
     * @throws ServiceException if anything goes wrong internally.
     */
    public abstract Role[] getRoles(Map<Object, Object> platformSpecificParams) throws ServiceException;


    /**
     * Generates UUID from unique user id
     * @param uid string that identifies user
     * @return uuid
     */
    public String generateUuid(String uid) {
        if(uid == null) {
            return generateUuid();
        }
        return UUID.nameUUIDFromBytes(uid.getBytes()).toString();
    }

    /**
     * Generates random UUID
     * @return uuid
     */
    public String generateUuid() {
        return UUID.randomUUID().toString();
    }
    /**
     * Returns all roles that exist in the system. Convenience method for calling getRoles(Map) with empty map
     * @return
     * @throws ServiceException
     */
    public Role[] getRoles() throws ServiceException {
        return getRoles(Collections.emptyMap());
    }

    /**
     * Returns all roles that exist in the system. Convenience method for calling getRoles(Map) with empty map
     * @return
     * @throws ServiceException
     */
    public Role getRoleByName(final String name) {
        try {
            // TODO: maybe some caching for roles?
            Role[] roles = getRoles();
            for(Role role : roles) {
                if(role.getName().equals(name)) {
                    return role;
                }
            }
        }
        catch (Exception ex) {
            log.error(ex, "Error getting roles from user service");
        }
        return null;
    }

    /**
     * Return the user by username. This method should be overridden in concrete implementation. The
     * default implementation always throws an exception.
     * @param username
     * @return User user
     * @throws ServiceException
     */
    public User getUser(String username) throws ServiceException {
        throw new ServiceException("Not implemented");
    }

    /**
     * Return the user by id. This method should be overridden in concrete implementation. The
     * default implementation always throws an exception.
     * @param id
     * @return User user
     * @throws ServiceException
     */
    public User getUser(long id) throws ServiceException {
        throw new ServiceException("Not implemented");
    }

    /**
     * Return all users. This method should be overridden in concrete implementation. The
     * default implementation always throws an exception.
     * @return List<User> users
     * @throws ServiceException
     */
    public List<User> getUsers() throws ServiceException {
        throw new ServiceException("Not implemented");
    }

    /**
     * Return all users. This method should be overridden in concrete implementation. The
     * default implementation always throws an exception.
     * @return List<User> users
     * @throws ServiceException
     */
    public List<User> getUsersWithRoles() throws ServiceException {
        throw new ServiceException("Not implemented");
    }

    /**
     * Create a new user. This method should be overridden in concrete implementation. The
     * default implementation always throws an exception.
     * @param user User to be created
     * @return User created user
     * @throws ServiceException
     */
    public User createUser(User user) throws ServiceException {
        throw new ServiceException("Not implemented");
    }

    
    public User createUser(User user, String[] roleIds) throws ServiceException {
        throw new ServiceException("Not implemented");
    }
    
    /**
     * Modify a user. This method should be overridden in concrete implementation. The
     * default implementation always throws an exception.
     * @param user Modified user
     * @return Modified user
     * @throws ServiceException
     */
    public User modifyUser(User user) throws ServiceException {
        throw new ServiceException("Not implemented");
    }

    public User modifyUserwithRoles(User user, String[] roleIds) throws ServiceException {
        throw new ServiceException("Not implemented");
    }      
    
    /**
     * Delete a user. This method should be overridden in concrete implementation. The
     * default implementation always throws an exception.
     * @param id User id
     * @throws ServiceException
     */
    public void deleteUser(long id) throws ServiceException {
        throw new ServiceException("Not implemented");
    }

    /**
     * Set a user's password. This method should be overridden in concrete implementation. The
     * default implementation always throws an exception.
     * @param screenname User name
     * @param password User password
     * @throws ServiceException
     */
    public void setUserPassword(String screenname, String password) throws ServiceException {
        throw new ServiceException("Not implemented");
    }

    /**
     * Updates a user's password. This method should be overridden in concrete implementation. The
     * default implementation always throws an exception.
     * @param screenname User name
     * @param password User password
     * @throws ServiceException
     */
    public void updateUserPassword(String screenname, String password) throws ServiceException {
        throw new ServiceException("Not implemented");
    }
}
