package fi.nls.oskari.service;

import fi.nls.oskari.domain.GuestUser;
import fi.nls.oskari.domain.Role;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.util.PropertyUtil;

import java.util.Collections;
import java.util.Map;

/**
 * Common interface for managing users.
 * TODO: this interface is still under development and new methods will propably be added when needed.
 */
public abstract class UserService {
    /**
     * Returns a concrete implementation of UserService. Class to be returned is defined with property "oskari.user.service".
     * @return
     * @throws ServiceException if class cannot be
     */
    public static UserService getInstance() throws ServiceException {
        final String className = PropertyUtil.getOptional("oskari.user.service");
        if(className == null) {
            throw new ServiceException("User service implementation not defined, add 'oskari.user.service' property with a value of fully qualified classname extending " + UserService.class.getCanonicalName());
        }
        try {
            UserService service = (UserService)Class.forName(className).newInstance();
            service.init();
            return service;
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
     * Returns all roles that exist in the system
     * @param platformSpecificParams optional platform specific parameters needed to get/filter roles. If implementation doesnt need any an empty map can be used.
     * @return all roles from the system
     * @throws ServiceException if anything goes wrong internally.
     */
    public abstract Role[] getRoles(Map<Object, Object> platformSpecificParams) throws ServiceException;

    /**
     * Returns all roles that exist in the system. Convenience method for calling getRoles(Map) with empty map
     * @return
     * @throws ServiceException
     */
    public Role[] getRoles() throws ServiceException {
        return getRoles(Collections.emptyMap());
    }

    /**
     * Return the user by username. This method should be overridden in concrete implementation. The
     * default implementation always throws an exception.
     * @param username
     * @return
     * @throws ServiceException
     */
    public User getUser(String username) throws ServiceException {
        throw new ServiceException("Not implemented");
    }
}
