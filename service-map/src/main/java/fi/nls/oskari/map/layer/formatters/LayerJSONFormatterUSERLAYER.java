package fi.nls.oskari.map.layer.formatters;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.domain.map.UserDataLayer;
import fi.nls.oskari.domain.map.userlayer.UserLayer;
import fi.nls.oskari.domain.map.wfs.WFSLayerAttributes;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.WFSConversionHelper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

/**
 * User layer to oskari layer json
 */
public class LayerJSONFormatterUSERLAYER extends LayerJSONFormatterUSERDATA {

    private static final String DEFAULT_GEOMETRY_NAME = "the_geom";

    public JSONObject getJSON(final OskariLayer baseLayer, UserLayer ulayer, String srs, String lang) {
        final JSONObject layerJson = super.getJSON(baseLayer, ulayer, srs, lang);
        JSONHelper.putValue(layerJson, "created", ulayer.getCreated());
        return layerJson;
    }
    @Override
    protected String getCoverage (UserDataLayer layer, String srs) {
        UserLayer uLayer = (UserLayer) layer;
        return getLayerCoverageWKT(uLayer.getWkt(), srs);
    }

    @Override
    protected JSONArray getProperties (UserDataLayer layer, WFSLayerAttributes wfsAttr, String lang) {
        UserLayer uLayer = (UserLayer) layer;
        JSONArray fields = uLayer.getFields(); // {locales: {}, name, type }
        // parse label from locale
        for(int i = 0; i < fields.length(); i++) {
            JSONObject prop = JSONHelper.getJSONObject(fields, i);
            JSONObject locales = prop.optJSONObject("locales");
            if (locales != null) {
                // For now UserLayerDataService.parseFields() adds only "en" localization
                JSONHelper.putValue(prop, "label", locales.optString("en"));
                prop.remove("locales");
            }
        }
        return fields;
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
