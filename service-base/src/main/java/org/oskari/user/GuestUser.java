package org.oskari.user;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.UserService;

/**
 * Extension of User for anonymous/guest user
 */
public class GuestUser extends User {

    private final static Logger log = LogFactory.getLogger(GuestUser.class);
    public GuestUser () {
        setFirstname("guest");
        setLastname("guest");
        setScreenname("guest");
    }
    /**
     * Always returns true
     * @return
     */
    public Boolean isGuest() {
        return true;
    }

    /**
     * Always returns false
     * @return
     */
    public Boolean isAdmin() {
        return false;
    }

    public static User getInstance() {
        try {
            UserService service = UserService.getInstance();
            return service.getGuestUser();
        }
        catch (Exception ex) {
            log.error(ex, "Couldn't get GuestUser from UserService - defaulting to blank class");
        }
        return new GuestUser();
    }
}
