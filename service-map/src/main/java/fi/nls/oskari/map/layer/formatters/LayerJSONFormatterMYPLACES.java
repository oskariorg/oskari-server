package fi.nls.oskari.map.layer.formatters;

import fi.nls.oskari.domain.map.wfs.WFSLayerOptions;
import org.json.JSONObject;

import fi.nls.oskari.domain.map.MyPlaceCategory;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.util.JSONHelper;

import java.util.Map;

/**
 * User layer to oskari layer json
 */
public class LayerJSONFormatterMYPLACES extends LayerJSONFormatterUSERDATA {
    private static final String KEY_IS_DEFAULT = "isDefault";

    public JSONObject getJSON(OskariLayer baseLayer, MyPlaceCategory category, String srs) {
        category.getWFSLayerOptions().setProperty(KEY_IS_DEFAULT, category.isDefault());

        return super.getJSON(baseLayer, category, srs);
    }

}
