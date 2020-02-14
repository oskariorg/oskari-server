package fi.nls.oskari.domain.map.wfs;

import fi.nls.oskari.util.JSONHelper;
import org.json.JSONObject;

public class WFSLayerCapabilities {
    private JSONObject capabilities;
    // input is capabilities from oskari_maplayer
    public WFSLayerCapabilities(JSONObject wfsCapabilities) {
        if(wfsCapabilities == null) {
            capabilities = new JSONObject();
            return;
        }
        capabilities = wfsCapabilities;
    }

    public String getGeometryAttribute() {
        // CapabilitiesConstants.KEY_GEOM_NAME
        return capabilities.optString("geomName");
    }
    public void setGeometryAttribute(String attr) {
        // CapabilitiesConstants.KEY_GEOM_NAME
        JSONHelper.putValue(capabilities, "geomName", attr);
    }
}
