package org.oskari.user;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.*;

/**
 * Internal model for a user.
 */
public class User implements Serializable {


    private long id = -1;
    private String lastname = "";
    private String firstname = "";
    private String screenname = "";
    private String email = "";
    private Map<String, String> attributes = new HashMap<>();
    private OffsetDateTime created;
    private OffsetDateTime lastLogin;

    private String uuid = "";
    private Set<Role> roles = new LinkedHashSet<>();

    public void clearRoles() {
        roles = new LinkedHashSet<>();
    }

    /**
     * Returns attributes as string (for easier DB handling)
     * @return
     */
    public Map<String, String> getAttributes() {
        return attributes;
    }
    /**
     * Set attributes as string (for easier DB handling)
     * @return
     */
    public void setAttributes(Map<String, String> attribs) {
        if (attribs == null) {
            attributes.clear();
        } else {
            attributes = attribs;
        }
    }

    /**
     * Adds additional attribute data for the user
     * @param key
     * @param value
     */
    public void setAttribute(String key, String value) {
        attributes.put(key, value);
    }
    /**
     * Get additional attribute data for the user
     * @param key
     * @return value of the key or null
     */
    public String getAttribute(String key) {
        return attributes.get(key);
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
                    || r.getName().equalsIgnoreCase(Role.getAdminRole().getName())) {
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
                    || r.getName().equalsIgnoreCase(Role.getAdminRole().getName())) {
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
        return hasRole(Role.getAdminRole().getName());
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

    public OffsetDateTime getCreated() {
        return created;
    }

    public void setCreated(OffsetDateTime created) {
        this.created = created;
    }

    public OffsetDateTime getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(OffsetDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        User user = (User) o;

        if (getId() != user.getId()) {
            return false;
        }
        return equalOrNull(getEmail(), user.getEmail()) && equalOrNull(getUuid(), user.getUuid());

    }
    private boolean equalOrNull(final String actual, final String expected) {
        if(actual == null && expected == null) {
            return true;
        }
        if(actual != null) {
            return actual.equals(expected);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = (int) (getId() ^ (getId() >>> 32));
        if(getEmail() != null) {
            result = 31 * result + getEmail().hashCode();
        }
        if(getUuid() != null) {
            result = 31 * result + getUuid().hashCode();
        }
        return result;
    }
}
