package fi.nls.oskari.domain;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.JSONHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Internal model for a user.
 */
public class User implements Serializable {

    private static Logger log = LogFactory.getLogger(User.class);
    private static final String KEY_FIRSTNAME = "firstName";
    private static final String KEY_LASTNAME = "lastName";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_NICKNAME = "nickName";
    private static final String KEY_USERUUID = "userUUID";
    private static final String KEY_USERID = "userID";
    private final static String KEY_ROLES = "roles";

    private long id = -1;
    private String lastname = "guest";
    private String firstname = "guest";
    private String screenname = "guest";
    private String email = "";
    private JSONObject attributes = new JSONObject();

    private String uuid = "";
    private Set<Role> roles = new LinkedHashSet<>();

    public void clearRoles() {
        roles = new LinkedHashSet<>();
    }

    /**
     * Returns attributes as string (for easier DB handling)
     * @return
     */
    public String getAttributes() {
        try {
            return attributes.toString(2);
        } catch (Exception ignored) {}
        return "{}";
    }
    /**
     * Set attributes as string (for easier DB handling)
     * @return
     */
    public void setAttributes(String attribs) {
        setAttributes(JSONHelper.createJSONObject(attribs));
    }

    /**
     * Adds additional attribute data for the user
     * @param key
     * @param value
     * @return
     */
    public boolean setAttribute(String key, String value) {
        return JSONHelper.putValue(attributes, key, value);
    }
    /**
     * Get additional attribute data for the user
     * @param key
     * @return value of the key or null
     */
    public Object getAttribute(String key) {
        try {
            return attributes.get(key);
        } catch (Exception ignored) {}
        return null;
    }

    public JSONObject getAttributesJSON() {
        return attributes;
    }

    public void setAttributes(JSONObject attribs) {
        if(attribs == null) {
            attribs = new JSONObject();
        }
        attributes = attribs;
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

    public JSONObject toJSON() {
        try {
            JSONObject userData = new JSONObject();
            userData.put(KEY_FIRSTNAME, getFirstname());
            userData.put(KEY_LASTNAME, getLastname());
            userData.put(KEY_EMAIL, getEmail());
            userData.put(KEY_NICKNAME, getScreenname());
            userData.put(KEY_USERUUID, getUuid());
            userData.put(KEY_USERID, getId());

            JSONArray roles = new JSONArray();
            for (Role role: getRoles()) {
                roles.put(role.toJSON());
            }
            userData.put(KEY_ROLES, roles);
            return userData;
        } catch (JSONException jsonex) {
            log.warn("Unable to construct JSON user data:", this);
        }
        return null;
    }

    public static User parse(JSONObject json) {
        try {
            User user = new User();
            user.setId(json.optInt(KEY_USERID));
            user.setFirstname(json.optString(KEY_FIRSTNAME));
            user.setLastname(json.optString(KEY_LASTNAME));
            user.setEmail(json.optString(KEY_EMAIL));
            user.setScreenname(json.optString(KEY_NICKNAME));
            user.setUuid(json.optString(KEY_USERUUID));
            JSONArray roles = json.optJSONArray(KEY_ROLES);
            for( int i = 0; i < roles.length(); ++i) {
                user.addRole(Role.parse(roles.getJSONObject(i)));
            }
            return user;

        } catch (Exception ex) {
            log.error("Error parsing user from JSON:", json);
            return new GuestUser();
        }
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
