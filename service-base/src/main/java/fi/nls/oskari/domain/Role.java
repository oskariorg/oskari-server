package fi.nls.oskari.domain;

import fi.nls.oskari.util.PropertyUtil;

/**
 * Internal model for user role.
 * Admin role name can be configured with property "oskari.user.role.admin" in PropertyUtil
 * and defaults to "Administrator".
 */
public class Role {

    private static String ADMIN_ROLE = null;

    private long id;
    private String name;

    public static String getAdminRoleName() {
        if(ADMIN_ROLE == null) {
            // default to Administrator
            ADMIN_ROLE = PropertyUtil.get("oskari.user.role.admin", "Administrator");
        }
        return ADMIN_ROLE;
    }

    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    
}
