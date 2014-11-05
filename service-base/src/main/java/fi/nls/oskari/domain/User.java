package fi.nls.oskari.domain;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Internal model for a user.
 */
public class User {

    private long id = -1;
    private String lastname = "guest";
    private String firstname = "guest";
    private String screenname = "guest";
    private String email = "";

    private String uuid = "";
    private Set<Role> roles = new HashSet<Role>();

    public void clearRoles() {
        roles = new HashSet<Role>();
    }

    public void addRole(final Role role) {
        if(role != null) {
            roles.add(role);
        }
    }

    public void addRole(final long id, final String name) {
        Role role = new Role();
        role.setId(id);
        role.setName(name);
        addRole(role);
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }    
    
    public Set<Role> getRoles() {
        return roles;
    }
    
    public boolean hasRole(String pRoleName) {
        for (Role r : getRoles()) {
            if (r.getName().equals(pRoleName)
                    || r.getName().equalsIgnoreCase(Role.getAdminRoleName())) {
                return true;
            }
        }
        return false;
    }

    public boolean hasRoleWithId(long pRoleId) {
        if(isAdmin()) {
            return true;
        }
        for (Role r : getRoles()) {
            if (r.getId() == pRoleId) {
                return true;
            }
        }
        return false;
    }

    public boolean hasAnyRoleIn(String[] pRoleName) {
        if(pRoleName == null) {
            return false;
        }
        final List<String> rolesToCheck = Arrays.asList(pRoleName);
        for (Role r : getRoles()) {
            if (rolesToCheck.contains(r.getName())
                    || r.getName().equalsIgnoreCase(Role.getAdminRoleName())) {
                return true;
            }
        }
        return false;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getFullName() {
        return firstname + ' ' + lastname;
    }

    public void setFirstname(String name) {
        this.firstname = name;
    }

    public void setLastname(String name) {
        this.lastname = name;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Boolean isGuest() {
        // TODO: maybe some logic here?
        return false;
    }

    public Boolean isAdmin() {
        return hasRole(Role.getAdminRoleName());
    }

    public String getScreenname() {
        return screenname;
    }

    public void setScreenname(String screenname) {
        this.screenname = screenname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLastname() {
        return lastname;
    }

    public String getFirstname() {
        return firstname;
    }

}
