package fi.nls.oskari.domain.map.wfs;

import fi.nls.oskari.util.JSONHelper;
import org.json.JSONObject;

public class WFSLayerCapabilities {
    public static final String KEY_GEOMETRYFIELD = "geomName";

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
        return capabilities.optString(KEY_GEOMETRYFIELD);
    }
    public void setGeometryAttribute(String attr) {
        // CapabilitiesConstants.KEY_GEOM_NAME
        JSONHelper.putValue(capabilities, KEY_GEOMETRYFIELD, attr);
    }
}
