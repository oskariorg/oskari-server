package fi.nls.oskari.domain.map;

import org.json.JSONException;
import org.json.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;

import java.io.IOException;

/**
 * Common model for layers consisting of user created data.
 */
public class UserDataLayer {
    private static final ObjectMapper OM;
    static {
        OM = new ObjectMapper();
        OM.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
    private String uuid;
    private String publisher_name;
    private UserDataStyle style;

    public UserDataLayer () {
        style = new UserDataStyle();
    }

    public String getUuid() {
        return uuid;
    }
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getPublisher_name() {
        return publisher_name;
    }

    public void setPublisher_name(String publisher_name) {
        this.publisher_name = publisher_name;
    }

    public boolean isPublished() {
        return publisher_name != null && !publisher_name.isEmpty();
    }

    public boolean isOwnedBy(final String uuid) {
        if(uuid == null || getUuid() == null) {
            return false;
        }
        return getUuid().equals(uuid);
    }
    public void setStyle (UserDataStyle style) {
        this.style = style;
    }

    public UserDataStyle getStyle () {
        return style;
    }
    public void mapPropertiesToStyle (String properties) throws JSONException {
        try {
            style = OM.readValue(properties, UserDataStyle.class);
        } catch (IOException e) {
            throw new JSONException(e.getMessage());
        }
    }
    public void mapPropertiesToStyle (JSONObject properties) throws JSONException {
        mapPropertiesToStyle(properties.toString());
    }
}
