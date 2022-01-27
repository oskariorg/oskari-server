package fi.nls.oskari.domain.map;

import fi.nls.oskari.domain.map.wfs.WFSLayerOptions;
import fi.nls.oskari.util.PropertyUtil;
import org.json.JSONObject;

/**
 * Common model for layers consisting of user created data.
 */
public abstract class UserDataLayer extends JSONLocalizedName {

    private long id;
    private String name; // use name from locale
    private String uuid;
    private String publisher_name;
    private WFSLayerOptions options;

    public abstract String getType();
    
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPrefixedId() {
        return getType() + "_" + getId();
    }
    @Deprecated
    public String getName() {
        return name;
    }
    @Deprecated
    public void setName(String name) {
        this.name = name;
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
    public void setOptions(JSONObject options) {
        this.options = new WFSLayerOptions(options);
    }

    public JSONObject getOptions() {
        return getWFSLayerOptions().getOptions();
    }

    public WFSLayerOptions getWFSLayerOptions() {
        if (options == null) {
            options = new WFSLayerOptions();
        }
        return options;
    }
}
