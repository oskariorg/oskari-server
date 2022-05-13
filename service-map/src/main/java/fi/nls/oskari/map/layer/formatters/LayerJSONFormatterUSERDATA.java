package fi.nls.oskari.map.layer.formatters;

import fi.mml.map.mapwindow.util.OskariLayerWorker;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.domain.map.UserDataLayer;
import fi.nls.oskari.domain.map.wfs.WFSLayerOptions;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import org.json.JSONObject;
import org.oskari.permissions.model.PermissionType;

import java.util.Map;

public abstract class LayerJSONFormatterUSERDATA extends LayerJSONFormatterWFS {

    private static final boolean IS_SECURE = true;
    protected static final String KEY_PERMISSIONS = "permissions";
    protected static final String KEY_LOCALE = "locale";
    protected static final String KEY_CREATED = "created";

    public JSONObject getJSON(OskariLayer baseLayer, UserDataLayer layer, String srs) {
        return this.getJSON(baseLayer, layer, srs, PropertyUtil.getDefaultLanguage());
    }

    public JSONObject getJSON(OskariLayer baseLayer, UserDataLayer layer, String srs, String lang) {
        JSONObject layerJson = getJSON(baseLayer, lang, IS_SECURE, srs);

        // Override base layer values
        JSONHelper.putValue(layerJson, KEY_TYPE, layer.getType());
        JSONHelper.putValue(layerJson, KEY_ID, layer.getPrefixedId());
        JSONHelper.putValue(layerJson, KEY_LOCALE, layer.getLocale());

        // TODO: remove name when all userdata layers can handle locale
        // override default name only if userdatalayer has name
        // add all localized names to allow user to edit them
        String name = layer.getName();
        Map<String, String> localized = layer.getNames();
        if (!localized.isEmpty()) {
            JSONObject names = new JSONObject();
            localized.entrySet().forEach(entry -> JSONHelper.putValue(names, entry.getKey(), entry.getValue()));
            JSONHelper.putValue(layerJson, KEY_LOCALIZED_NAME, names);
        } else if (name != null && !name.isEmpty()) {
            JSONHelper.putValue(layerJson, KEY_LOCALIZED_NAME, name);
        }
        // FIXME: base layer should have correct data provider and title.
        layerJson.remove(KEY_SUBTITLE);
        layerJson.remove(KEY_DATA_PROVIDER);

        WFSLayerOptions wfsOpts = layer.getWFSLayerOptions();
        wfsOpts.injectBaseLayerOptions(baseLayer.getOptions());
        JSONHelper.putValue(layerJson, KEY_OPTIONS, wfsOpts.getOptions());

        return layerJson;
    }
    public static void setDefaultPermissions(JSONObject layerJson) {
        JSONObject permissions = new JSONObject();
        JSONHelper.putValue(permissions, PermissionType.PUBLISH.getJsonKey(), OskariLayerWorker.PUBLICATION_PERMISSION_OK);
        JSONHelper.putValue(permissions, PermissionType.DOWNLOAD.getJsonKey(), OskariLayerWorker.DOWNLOAD_PERMISSION_OK);
        JSONHelper.putValue(layerJson, KEY_PERMISSIONS, permissions);
    }
}
