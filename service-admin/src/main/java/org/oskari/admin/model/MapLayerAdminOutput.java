package org.oskari.admin.model;

import java.util.Map;

/**
 * Used as ouput data model for layers in the admin UI.
 */
public class MapLayerAdminOutput extends MapLayer {

    private String warn;
    private Map<String, Object> capabilities;

    public Map<String, Object> getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(Map<String, Object> capabilities) {
        this.capabilities = capabilities;
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
}
