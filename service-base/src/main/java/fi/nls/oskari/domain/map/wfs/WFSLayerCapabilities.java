package fi.nls.oskari.domain.map.wfs;

import org.json.JSONObject;

public class WFSLayerCapabilities {
    public static final String KEY_GEOMETRYFIELD = "geomName";
    private static final String KEY_TYPE_SPECIFIC = "typeSpecific";

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
        JSONObject typeSpec = capabilities.optJSONObject(KEY_TYPE_SPECIFIC);
        if (typeSpec == null) {
            return capabilities.optString(KEY_GEOMETRYFIELD, null);
        }
        return typeSpec.optString(KEY_GEOMETRYFIELD, null);
    }
}
