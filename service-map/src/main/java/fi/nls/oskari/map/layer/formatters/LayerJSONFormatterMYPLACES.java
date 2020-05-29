package fi.nls.oskari.map.layer.formatters;

import org.json.JSONObject;

import fi.nls.oskari.domain.map.MyPlaceCategory;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.util.JSONHelper;

/**
 * User layer to oskari layer json
 */
public class LayerJSONFormatterMYPLACES extends LayerJSONFormatter {

    private static final String MYPLACES_ID_PREFIX = "myplaces_";
    private static final String KEY_IS_DEFAULT = "isDefault";

    public JSONObject getJSON(OskariLayer layer, String lang, boolean isSecure, String crs, MyPlaceCategory category) {
        layer.setType(OskariLayer.TYPE_MYPLACES);
        layer.setName(OskariLayer.TYPE_MYPLACES);
        layer.setName(lang, category.getCategory_name());
        layer.setTitle(lang, category.getCategory_name());
        injectCustomOptions(layer.getOptions(), category);

        JSONObject layerJson = getBaseJSON(layer, lang, isSecure, crs);

        // Override the "id" field as OskariLayer.id is an int so we can't do that beforehand
        JSONHelper.putValue(layerJson, KEY_ID, MYPLACES_ID_PREFIX + category.getId());

        return layerJson;
    }

    private void injectCustomOptions(JSONObject options, MyPlaceCategory category) {
        category.getWFSLayerOptions().injectBaseLayerOptions(options);
        JSONHelper.putValue(options, KEY_IS_DEFAULT, category.isDefault());
    }

}
