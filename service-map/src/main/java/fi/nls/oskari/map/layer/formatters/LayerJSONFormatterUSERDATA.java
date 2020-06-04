package fi.nls.oskari.map.layer.formatters;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.domain.map.UserDataLayer;
import fi.nls.oskari.domain.map.wfs.WFSLayerOptions;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import org.json.JSONObject;

public class LayerJSONFormatterUSERDATA extends LayerJSONFormatterWFS {
    private static final boolean IS_SECURE = true;

    public JSONObject getJSON(OskariLayer baseLayer, UserDataLayer layer, String srs) {
        return this.getJSON(baseLayer, layer, srs, PropertyUtil.getDefaultLanguage());
    }

    public JSONObject getJSON(OskariLayer baseLayer, UserDataLayer layer, String srs, String lang) {
        // get layerJson from wfs baselayer
        JSONObject layerJson = getJSON(baseLayer, lang, IS_SECURE, srs);
        // Override base layer values
        JSONHelper.putValue(layerJson, KEY_TYPE, layer.getType());
        JSONHelper.putValue(layerJson, KEY_ID, layer.getPrefixedId());
        JSONHelper.putValue(layerJson, KEY_LOCALIZED_NAME, layer.getName());

        WFSLayerOptions wfsOpts  = layer.getWFSLayerOptions();
        wfsOpts.injectBaseLayerOptions(baseLayer.getOptions());
        JSONHelper.putValue(layerJson, KEY_OPTIONS, wfsOpts.getOptions());
        //init permissionz here??

        return layerJson;
    }
}
