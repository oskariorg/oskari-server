package org.oskari.admin;

import org.oskari.data.model.MapLayer;

import java.util.Map;

public class MapLayerAdminOutput extends MapLayer {

    private Map<String, Object> capabilities;

    public Map<String, Object> getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(Map<String, Object> capabilities) {
        this.capabilities = capabilities;
    }
}
