package org.oskari.user;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class UserJsonHelper {

    private static Logger LOG = LogFactory.getLogger(User.class);

    private static final String KEY_FIRSTNAME = "firstName";
    private static final String KEY_LASTNAME = "lastName";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_NICKNAME = "nickName";
    private static final String KEY_USERUUID = "userUUID";
    private static final String KEY_USERID = "userID";
    private final static String KEY_ROLES = "roles";
    private final static String KEY_ADMIN = "admin";
    private final static String KEY_CREATED = "created";
    private final static String KEY_LAST_LOGIN = "lastLogin";

    private final static String KEY_ROLE_ID = "id";
    private final static String KEY_ROLE_NAME = "name";

    public static JSONObject toJSON(User user) {

        try {
            JSONObject userData = new JSONObject();
            userData.put(KEY_FIRSTNAME, user.getFirstname());
            userData.put(KEY_LASTNAME, user.getLastname());
            userData.put(KEY_EMAIL, user.getEmail());
            userData.put(KEY_NICKNAME, user.getScreenname());
            userData.put(KEY_USERUUID, user.getUuid());
            userData.put(KEY_USERID, user.getId());
            userData.put(KEY_CREATED, user.getCreated());
            userData.put(KEY_LAST_LOGIN, user.getLastLogin());
            if (user.isAdmin()){
                userData.put(KEY_ADMIN, true);
            }
            JSONArray roles = new JSONArray();
            for (Role role: user.getRoles()) {
                roles.put(toJSON(role));
            }
            userData.put(KEY_ROLES, roles);
            return userData;
        } catch (JSONException jsonex) {
            LOG.warn("Unable to construct JSON user data:", user);
        }
        return null;
    }

    public static User toUser(JSONObject json) {
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
                user.addRole(toRole(roles.getJSONObject(i)));
            }
            return user;

        } catch (Exception ex) {
            LOG.error("Error parsing user from JSON:", json);
            return new GuestUser();
        }
    }

    public static JSONObject toJSON(Role role) {
        try {
            JSONObject roleData = new JSONObject();
            roleData.put(KEY_ROLE_ID, role.getId());
            roleData.put(KEY_ROLE_NAME, role.getName());

            return roleData;
        } catch (JSONException jsonex) {
            LOG.warn("Unable to construct JSON role data:", role);
        }
        return null;
    }

    public static Role toRole(JSONObject json) {

        try {
            Role role = new Role();
            role.setId(json.optInt(KEY_ROLE_ID));
            role.setName(json.optString(KEY_ROLE_NAME));

            return role;
        } catch (Exception jsonex) {
            LOG.warn("Unable to parse role from JSON:", json);
        }
        return null;
    }
}
