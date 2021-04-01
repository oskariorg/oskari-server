package fi.nls.oskari.map.layer.formatters;

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

        return super.getJSON(baseLayer, category, srs, lang);
    }

}
