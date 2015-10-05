package fi.nls.oskari.user;

import fi.nls.oskari.domain.Role;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.service.UserService;
import fi.nls.oskari.util.JSONHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StandaloneUserService extends UserService {

    private final static String JSKEY_USER = "user";
    private final static String JSKEY_PASS = "pass";
    private final static String JSKEY_UUID = "uuid";
    private final static String JSKEY_ROLES = "roles";
    private final static String JSKEY_USERS = "users";
    private final static String JSKEY_FIRSTNAME = "firstName";
    private final static String JSKEY_LASTNAME = "lastName";

    private final static String JSKEY_ID = "id";
    private final static String JSKEY_NAME = "name";

    private final static String FILE_JSON_USER = "/users/user.json";
    private final static String FILE_JSON_ROLE = "/users/role.json";

    // role id is used to map permissions to user, this should match the id in permissions db for guests
    private static Role GUEST_ROLE = null;

    private Map<Long, String> roles = new HashMap<Long, String>();

    public User getGuestUser() {
        final User user = super.getGuestUser();
        user.addRole(GUEST_ROLE);
        return user;
    }

    @Override
    public User login(String username, String password) throws ServiceException {

        User user = this.getUser(username, password);
        return user;
    }

    @Override
    public void init() throws ServiceException {
        JSONObject jsroles = null;
        try {
            jsroles = JSONHelper.createJSONObject(this
                    .getJsonFile(FILE_JSON_ROLE));

            if (jsroles != null) {
                JSONArray roles = jsroles.optJSONArray(JSKEY_ROLES);

                for (int i = 0; i < roles.length(); i++) {
                    JSONObject jsrole = roles.getJSONObject(i);
                    long id = jsrole.optLong(JSKEY_ID, 0);
                    String name = jsrole.optString(JSKEY_NAME);
                    this.roles.put(id, name);
                    if(jsrole.optBoolean("isGuest")) {
                        Role role = new Role();
                        role.setId(id);
                        role.setName(name);
                        GUEST_ROLE = role;
                    }
                }
            }

        } catch (Exception e) {
            throw new ServiceException("Role parameters missing." + e);
        }

    }

    public User getUser(String username, String password)
            throws ServiceException {

        User user = null;
        try {
            JSONObject jsusers = JSONHelper.createJSONObject(this.getJsonFile(FILE_JSON_USER));
            JSONArray users = jsusers.optJSONArray(JSKEY_USERS);

            for (int i = 0; i < users.length(); i++) {
                JSONObject jsuser = users.getJSONObject(i);
                if (username.equals(jsuser.optString(JSKEY_USER))
                        && password.equals(jsuser.optString(JSKEY_PASS))) {
                    // Credentials ok
                    user = oskariUserFromJson(jsuser);
                }
            }
        } catch (Exception e) {
            throw new ServiceException("User parameters missing." + e);
        }

        return user;
    }

    private String getJsonFile(String jsfile) throws ServiceException {
        String jsString = null;
        try {
            InputStream fis = this.getClass().getResourceAsStream(jsfile);

            StringBuilder inputStringBuilder = new StringBuilder();
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(fis, "UTF-8"));
            String line = bufferedReader.readLine();
            while (line != null) {
                inputStringBuilder.append(line);
                line = bufferedReader.readLine();
            }
            jsString = inputStringBuilder.toString();

        } catch (Exception e) {
            throw new ServiceException("User/Role parameters json file missing." + e);
        }
        return jsString;
    }

    @Override
    public Role[] getRoles(Map<Object, Object> platformSpecificParams)
            throws ServiceException {
        final List<Role> roles = new ArrayList<Role>();
        try {
            for (Map.Entry<Long, String> entry : this.roles.entrySet()) {
                Long key = entry.getKey();
                String value = entry.getValue();
                Role role = new Role();
                role.setId(key);
                role.setName(value);
                roles.add(role);
            }

        } catch (Exception e) {
            throw new ServiceException("Something went wrong with getting all roles", e);
        }

        return roles.toArray(new Role[0]);
    }

    @Override
    public User getUser(String username) throws ServiceException {
        User user = null;
        try {
            JSONObject jsusers = JSONHelper.createJSONObject(this.getJsonFile(FILE_JSON_USER));
            JSONArray users = jsusers.optJSONArray(JSKEY_USERS);

            for (int i = 0; i < users.length(); i++) {
                JSONObject jsuser = users.getJSONObject(i);
                if (username.equals(jsuser.optString(JSKEY_USER))) {
                    user = oskariUserFromJson(jsuser);
                }
            }
        } catch (JSONException e) {
            throw new ServiceException("JSON parsing failed: " + e);
        }

        return user;
    }

    private User oskariUserFromJson(JSONObject jsuser) {
        User user = new User();
        user.setId(jsuser.optLong(JSKEY_ID, 0));
        user.setFirstname(jsuser.optString(JSKEY_FIRSTNAME));
        user.setLastname(jsuser.optString(JSKEY_LASTNAME));
        user.setUuid(jsuser.optString(JSKEY_UUID));
        // Roles
        JSONArray roles = jsuser.optJSONArray(JSKEY_ROLES);
        for (int k = 0; k < roles.length(); k++) {
            long id = roles.optLong(k);
            user.addRole(id, this.roles.get(id));
        }
        return user;
    }
    
    
}
