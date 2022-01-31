package fi.nls.oskari.map.layer.formatters;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.domain.map.userlayer.UserLayer;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.WFSConversionHelper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

import static fi.nls.oskari.service.capabilities.CapabilitiesConstants.KEY_LAYER_COVERAGE;


/**
 * User layer to oskari layer json
 */
public class LayerJSONFormatterUSERLAYER extends LayerJSONFormatterUSERDATA {

    private static final String DEFAULT_GEOMETRY_NAME = "the_geom";

    public JSONObject getJSON(final OskariLayer baseLayer, UserLayer ulayer, String srs, String lang) {
        final JSONObject layerJson = super.getJSON(baseLayer, ulayer, srs, lang);
        JSONHelper.putValue(layerJson, KEY_LAYER_COVERAGE, getLayerCoverageWKT(ulayer.getWkt(), srs));
        JSONObject baseAttributes = JSONHelper.getJSONObject(layerJson, "attributes");
        JSONObject layerAttributes = parseAttributes(ulayer.getFields());
        // baseAttributes comes from baseLayer merge layer attributes
        JSONHelper.putValue(layerJson, "attributes", JSONHelper.merge(baseAttributes, layerAttributes));
        return layerJson;
    }
    // parse fields like WFSLayerAttributes
    private static JSONObject parseAttributes(final JSONArray fields) {
        JSONObject attributes = new JSONObject();
        if (fields == null || fields.length() == 0){
            return attributes;
        }
        JSONObject data = new JSONObject();
        JSONObject types = new JSONObject();
        Map<String, JSONObject> locale = new HashMap<>();
        JSONArray filteredFields = new JSONArray();
        for(int i = 0; i < fields.length(); i++){
            JSONObject field = JSONHelper.getJSONObject(fields, i);
            String name = JSONHelper.optString(field, "name");
            String type = JSONHelper.optString(field, "type");
            if (DEFAULT_GEOMETRY_NAME.equals(name)) {
                JSONHelper.putValue(data, "geometryName", name);
                JSONHelper.putValue(data, "geometryType", type);
                continue;
            }
            // add name to filtered fields to order/filter fields
            filteredFields.put(name);
            // locales
            Map<String,String> locales = JSONHelper.getObjectAsMap(field.optJSONObject("locales"));
            locales.keySet().forEach(lang -> {
                locale.putIfAbsent(lang, new JSONObject());
                JSONHelper.putValue(locale.get(lang), name, locales.get(lang));
            });
            // attribute types, like WFSGetLayerFields
            JSONHelper.putValue(types, name, WFSConversionHelper.getSimpleType(type.toLowerCase()));
        }

        JSONHelper.putValue(attributes, "data", data);
        JSONHelper.putValue(data, "filter", filteredFields);
        JSONHelper.putValue(data, "types", types);
        JSONObject loc = new JSONObject();
        locale.keySet().forEach(lang -> JSONHelper.putValue(loc, lang, locale.get(lang)));
        JSONHelper.putValue(data, "locale", loc);
        return attributes;
    }
}
