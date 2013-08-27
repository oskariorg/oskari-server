package fi.nls.oskari.domain;

/**
 * Extension of User for unknown/guest user
 */
public class GuestUser extends User {

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
}
