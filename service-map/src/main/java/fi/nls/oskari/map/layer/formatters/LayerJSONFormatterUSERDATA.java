package fi.nls.oskari.map.layer.formatters;

import fi.mml.map.mapwindow.util.OskariLayerWorker;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.domain.map.UserDataLayer;
import fi.nls.oskari.domain.map.style.VectorStyle;
import fi.nls.oskari.domain.map.wfs.WFSLayerAttributes;
import fi.nls.oskari.domain.map.wfs.WFSLayerOptions;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.util.WFSConversionHelper;
import org.json.JSONArray;
import org.json.JSONObject;
import org.oskari.permissions.model.PermissionType;

import static fi.nls.oskari.service.capabilities.CapabilitiesConstants.KEY_ISQUERYABLE;

public abstract class LayerJSONFormatterUSERDATA extends LayerJSONFormatter {

    private static final boolean IS_SECURE = true;
    protected static final String KEY_PERMISSIONS = "permissions";
    protected static final String KEY_LOCALE = "locale";
    protected static final JSONObject DEAULT_GEOMETRY_PROPERTY =
            JSONHelper.createJSONObject("{\"name\":\"the_geom\", \"type\": \"geometry\", \"rawType\": \"GeometryPropertyType\"}");

    public JSONObject getJSON(OskariLayer baseLayer, UserDataLayer layer, String srs) {
        return this.getJSON(baseLayer, layer, srs, PropertyUtil.getDefaultLanguage());
    }

    public JSONObject getJSON(OskariLayer baseLayer, UserDataLayer layer, String srs, String lang) {
        JSONObject layerJson = getBaseJSON(baseLayer, lang, IS_SECURE, srs);
        JSONHelper.putValue(layerJson, KEY_ISQUERYABLE, true);

        // Override base layer values
        JSONHelper.putValue(layerJson, KEY_TYPE, layer.getType());
        JSONHelper.putValue(layerJson, KEY_ID, layer.getPrefixedId());
        JSONHelper.putValue(layerJson, KEY_LOCALE, layer.getLocale());

        // FIXME: base layer should have correct data provider and title.
        layerJson.remove(KEY_SUBTITLE);
        layerJson.remove(KEY_DATA_PROVIDER);
        layerJson.remove(KEY_DATA_PROVIDER_ID);

        // getBaseJSON adds these but model builder isn't using them. Frontend uses DescribeLayer response
        layerJson.remove(KEY_OPTIONS);
        layerJson.remove(KEY_ATTRIBUTES);

        WFSLayerAttributes wfsAttr = new WFSLayerAttributes(baseLayer.getAttributes());
        WFSLayerOptions wfsOpts = layer.getWFSLayerOptions();
        wfsOpts.injectBaseLayerOptions(baseLayer.getOptions());

        // Gather values from options and attributes like DescribeLayerHandler
        // see: org.oskari.control.layer.model.LayerExtendedOutput
        // For now user can't edit labels or other localized values => return only lang related values
        JSONObject describeLayer = new JSONObject();
        // has only one 'default' named style
        JSONObject vectorStyleLike = JSONHelper.createJSONObject("id", "default");
        JSONHelper.putValue(vectorStyleLike, "style", wfsOpts.getDefaultStyle());
        JSONHelper.putValue(vectorStyleLike, "type", VectorStyle.TYPE_OSKARI);
        JSONHelper.put(describeLayer, "styles", JSONHelper.createJSONArray(vectorStyleLike));

        JSONHelper.putValue(describeLayer, "hover", wfsOpts.getHover());

        JSONArray properties = getProperties(layer, wfsAttr, lang);
        JSONHelper.put(describeLayer, "properties", properties);

        JSONObject controlData = getControlData(layer, wfsOpts);
        JSONHelper.putValue(controlData,"styleType", getStyleType(properties));
        JSONHelper.putValue(describeLayer, "controlData", controlData);

        JSONHelper.putValue(describeLayer, "coverage", getCoverage(layer, srs));

        JSONHelper.putValue(layerJson, "describeLayer", describeLayer);

        return layerJson;
    }
    protected String getCoverage (UserDataLayer layer, String srs) {
        return null;
    }
    protected JSONArray getProperties (UserDataLayer layer, WFSLayerAttributes wfsAttr, String lang) {
        return new JSONArray();
    }

    protected JSONObject getControlData (UserDataLayer layer, WFSLayerOptions wfsOpts) {
        JSONObject controlData = new JSONObject();
        JSONHelper.putValue(controlData, WFSLayerOptions.KEY_RENDER_MODE, wfsOpts.getRenderMode());
        JSONHelper.putValue(controlData, WFSLayerOptions.KEY_CLUSTER, wfsOpts.getClusteringDistance());
        return controlData;
    }
    protected String getStyleType(JSONArray properties) {
        for (int i = 0; i < properties.length(); i++) {
            JSONObject prop = JSONHelper.getJSONObject(properties, i);
            String type = JSONHelper.optString(prop, "type");
            if (WFSConversionHelper.GEOMETRY.equals(type)) {
                String raw = JSONHelper.optString(prop, "rawType");
                return WFSConversionHelper.getStyleType(raw);
            }
        }
        return WFSConversionHelper.UNKNOWN;
    }

    public static void setDefaultPermissions(JSONObject layerJson) {
        JSONObject permissions = new JSONObject();
        JSONHelper.putValue(permissions, PermissionType.PUBLISH.getJsonKey(), OskariLayerWorker.PUBLICATION_PERMISSION_OK);
        JSONHelper.putValue(permissions, PermissionType.DOWNLOAD.getJsonKey(), OskariLayerWorker.DOWNLOAD_PERMISSION_OK);
        JSONHelper.putValue(layerJson, KEY_PERMISSIONS, permissions);
    }
}
