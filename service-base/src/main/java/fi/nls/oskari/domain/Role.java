package fi.nls.oskari.domain;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.service.UserService;
import fi.nls.oskari.util.PropertyUtil;

/**
 * Internal model for user role.
 * Admin role name can be configured with property "oskari.user.role.admin" in PropertyUtil
 * and defaults to "Administrator".
 */
public class Role {

    private static final Logger log = LogFactory.getLogger(Role.class);
    private static Role ADMIN_ROLE = null;
    public static final String DEFAULT_ADMIN_ROLE_NAME = "Administrator";

    private long id;
    private String name;

    /**
     * Deprecated - Use getAdminRole() instead.
     * @return
     */
    @Deprecated
    public static String getAdminRoleName() {
        ADMIN_ROLE = getAdminRole();
        if(ADMIN_ROLE == null) {
            return DEFAULT_ADMIN_ROLE_NAME;
        }
        return getAdminRole().getName();
    }

    public static Role getAdminRole() {
        if(ADMIN_ROLE == null) {
            // default to Administrator
            final String rolename = PropertyUtil.get("oskari.user.role.admin", DEFAULT_ADMIN_ROLE_NAME);
            try {
                final Role[] roles = UserService.getInstance().getRoles();
                for(Role role : roles) {
                    if(rolename.equals(role.getName())) {
                        ADMIN_ROLE = role;
                        break;
                    }
                }
            } catch (ServiceException ex) {
                log.error(ex, "Error getting roles from user service");
            }
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

    public boolean isAdminRole() {
        return (getAdminRoleName().equals(getName()));
    }
    
}
