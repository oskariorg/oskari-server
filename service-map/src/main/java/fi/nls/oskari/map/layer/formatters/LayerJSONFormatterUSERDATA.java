package fi.nls.oskari.map.layer.formatters;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.domain.map.UserDataLayer;
import fi.nls.oskari.domain.map.wfs.WFSLayerOptions;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import org.json.JSONObject;

public abstract class LayerJSONFormatterUSERDATA extends LayerJSONFormatterWFS {

    private static final boolean IS_SECURE = true;

    public JSONObject getJSON(OskariLayer baseLayer, UserDataLayer layer, String srs) {
        return this.getJSON(baseLayer, layer, srs, PropertyUtil.getDefaultLanguage());
    }

    public JSONObject getJSON(OskariLayer baseLayer, UserDataLayer layer, String srs, String lang) {
        JSONObject layerJson = getJSON(baseLayer, lang, IS_SECURE, srs);

        // Override base layer values
        JSONHelper.putValue(layerJson, KEY_TYPE, layer.getType());
        JSONHelper.putValue(layerJson, KEY_ID, layer.getPrefixedId());
        String name = layer.getName();
        // override default name only if userdatalayer has name
        if (name != null && !name.isEmpty()) {
            JSONHelper.putValue(layerJson, KEY_LOCALIZED_NAME, name);
        }
        // FIXME: base layer should have correct data provider and title.
        layerJson.remove(KEY_SUBTITLE);
        layerJson.remove(KEY_DATA_PROVIDER);

        WFSLayerOptions wfsOpts = layer.getWFSLayerOptions();
        wfsOpts.injectBaseLayerOptions(baseLayer.getOptions());
        JSONHelper.putValue(layerJson, KEY_OPTIONS, wfsOpts.getOptions());

        //init permissionz here??

        return layerJson;
    }

}
