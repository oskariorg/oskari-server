package org.oskari.admin;

import java.util.Map;

/**
 * Used as ouput data model for layers in the admin UI.
 */
public class MapLayerAdminOutput extends MapLayer {

    private Map<String, Object> capabilities;

    public Map<String, Object> getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(Map<String, Object> capabilities) {
        this.capabilities = capabilities;
    }
}
