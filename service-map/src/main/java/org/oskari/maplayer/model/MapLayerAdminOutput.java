package org.oskari.maplayer.model;

import java.util.*;

/**
 * Used as ouput data model for layers in the admin UI.
 */
public class MapLayerAdminOutput extends MapLayer {

    private String warn;
    private Map<String, Object> capabilities;
    private Date created;
    private Date updated;
    private Date capabilities_last_updated;

    public Map<String, Object> getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(Map<String, Object> capabilities) {
        this.capabilities = capabilities;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getUpdated() {
        return updated;
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
    }

    /**
     * Possible warning message that capabilities coulnt' be updated
     * @return
     */
    public String getWarn() {
        return warn;
    }

    public void setWarn(String warn) {
        this.warn = warn;
    }

    public Date getCapabilities_last_updated() {
        return capabilities_last_updated;
    }

    public void setCapabilities_last_updated(Date capabilities_last_updated) {
        this.capabilities_last_updated = capabilities_last_updated;
    }
}
