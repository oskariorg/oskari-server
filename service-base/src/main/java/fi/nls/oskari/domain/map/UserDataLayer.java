package fi.nls.oskari.domain.map;

import fi.nls.oskari.domain.map.wfs.WFSLayerOptions;
import org.json.JSONObject;

/**
 * Common model for layers consisting of user created data.
 */
public class UserDataLayer {
    private String uuid;
    private String publisher_name;
    private UserDataStyle style;
    private WFSLayerOptions options;

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

    public void setOptions (JSONObject options) {
        this.options = new WFSLayerOptions(options);
    }
    public JSONObject getOptions () {
        return options.getOptions();
    }
    public WFSLayerOptions getWFSLayerOptions () {
        if (options == null) {
            options = new WFSLayerOptions();
        }
        return options;
    }
    public void mapPropertiesToStyle(JSONObject properties) {
        //TODO remove
    }

}
