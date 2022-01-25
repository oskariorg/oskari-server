package fi.nls.oskari.map.layer.formatters;

import fi.nls.oskari.util.JSONHelper;
import org.json.JSONObject;

import fi.nls.oskari.domain.map.MyPlaceCategory;
import fi.nls.oskari.domain.map.OskariLayer;

/**
 * MyPlaces category to Oskari layer JSON
 */
public class LayerJSONFormatterMYPLACES extends LayerJSONFormatterUSERDATA {

    private static final String KEY_IS_DEFAULT = "isDefault";

    public JSONObject getJSON(OskariLayer baseLayer, MyPlaceCategory category, String srs, String lang) {
        category.getWFSLayerOptions().setProperty(KEY_IS_DEFAULT, category.isDefault());
        final JSONObject layerJson = super.getJSON(baseLayer, category, srs, lang);

        // If user doesn't have category, default category is added with empty locale
        // Categories isn't always handled by MyPlaces bundle so default localized names are stored in baselayer
        if (category.getNames().isEmpty()) {
            // use locale from baselayer
            JSONHelper.putValue(layerJson, KEY_LOCALE, baseLayer.getLocale());
        }
        return layerJson;
    }

}
