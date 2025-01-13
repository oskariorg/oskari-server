package fi.nls.oskari.util;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.service.ServiceException;
import org.json.JSONObject;
import org.oskari.capabilities.CapabilitiesService;
import org.oskari.capabilities.ogc.LayerCapabilitiesWFS;

// TODO: move this code to the handler (not used anywhere else)
// consider changing the format to be more aligned with layer.capabilities AND moving this logic to DescribeLayer route?
public class WFSGetLayerFields {
    private static final String KEY_TYPES = "types";
    private static final String KEY_GEOMETRY_NAME = "geometryName";
    private static final String KEY_GEOMETRY_TYPE = "geometryType";

    /**
     * Return fields information for the WFS layer
     * <p>
     * The result is constructed as:
     * {
     * "types": {
     * "field-1": STRING,
     * "field-2": NUMBER,
     * ...
     * "field-n": BOOLEAN
     * },
     * "geometryField": "geometry"
     * }
     * <p>
     * The field type can be one of the following values:
     * - string
     * - number
     * - boolean
     * - unknown
     */
    public static JSONObject getLayerFields(OskariLayer layer) throws ServiceException {
        if (!OskariLayer.TYPE_WFS.equals(layer.getType())) {
            throw new ServiceException("Unsupported type: " + layer.getType());
        }
        LayerCapabilitiesWFS caps = CapabilitiesService.fromJSON(layer.getCapabilities().toString(), layer.getType());

        JSONObject types = new JSONObject();
        caps.getFeatureProperties().stream().forEach(prop -> {
            JSONHelper.putValue(types, prop.name, WFSConversionHelper.getSimpleType(prop.type));
        });
        JSONObject result = new JSONObject();
        JSONHelper.putValue(result, KEY_TYPES, types);
        JSONHelper.putValue(result, KEY_GEOMETRY_NAME, caps.getGeometryField());
        JSONHelper.putValue(result, KEY_GEOMETRY_TYPE, caps.getFeatureProperty(caps.getGeometryField()).type);
        return result;
    }


}
