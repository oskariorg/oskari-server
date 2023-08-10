package fi.nls.oskari.map.layer.formatters;

import fi.nls.oskari.domain.map.UserDataLayer;
import fi.nls.oskari.domain.map.wfs.WFSLayerAttributes;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.WFSConversionHelper;
import org.json.JSONArray;
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

    @Override
    protected JSONArray getProperties (UserDataLayer layer, WFSLayerAttributes wfsAttr, String lang) {
        JSONArray props = new JSONArray();
        JSONObject format = wfsAttr.getFieldFormatMetadata().orElse(new JSONObject());
        JSONObject locale = wfsAttr.getLocalization(lang).orElse(new JSONObject());
        wfsAttr.getSelectedAttributes(lang).stream().forEach(name -> {
            JSONObject prop = JSONHelper.createJSONObject("name", name);
            JSONHelper.putValue(prop, "type", "string");
            JSONHelper.putValue(prop, "rawType", "string");
            JSONHelper.putValue(prop, "label", locale.optString(name, null));
            JSONHelper.putValue(prop, "format", JSONHelper.getJSONObject(format, name));
            props.put(prop);
        });
        return props;
    }
    @Override
    protected String getGeometryType() {
        return WFSConversionHelper.TYPE_COLLECTION;
    }
}
