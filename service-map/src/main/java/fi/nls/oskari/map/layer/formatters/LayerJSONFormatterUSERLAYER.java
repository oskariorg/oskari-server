package fi.nls.oskari.map.layer.formatters;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.domain.map.userlayer.UserLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * User layer to oskari layer json
 */
public class LayerJSONFormatterUSERLAYER extends LayerJSONFormatter {

    private static final String USERLAYER_RENDERING_URL = "userlayer.rendering.url";
    private static final String USERLAYER_RENDERING_ELEMENT = "userlayer.rendering.element";
    private static final String DEFAULT_GEOMETRY_NAME = "the_geom";
    private static final String DEFAULT_LOCALES_LANGUAGE = "en";

    private static final String PROPERTY_RENDERING_URL = PropertyUtil.getOptional(USERLAYER_RENDERING_URL);
    final String userlayerRenderingElement = PropertyUtil.get(USERLAYER_RENDERING_ELEMENT);

    private static Logger log = LogFactory.getLogger(LayerJSONFormatterUSERLAYER.class);

    /**
     * @param layer
     * @param lang
     * @param isSecure
     * @param ulayer   data in user_layer table
     * @return
     */
    public JSONObject getJSON(OskariLayer layer,
                              final String lang,
                              final boolean isSecure,
                              final String crs,
                              UserLayer ulayer) {
        // set geometry before parsing layerJson
        layer.setGeometry(ulayer.getWkt());
        final JSONObject layerJson = getBaseJSON(layer, lang, isSecure, crs);
        JSONHelper.putValue(layerJson, "isQueryable", true);
        JSONHelper.putValue(layerJson, "name", ulayer.getLayer_name());
        JSONHelper.putValue(layerJson, "description", ulayer.getLayer_desc());
        JSONHelper.putValue(layerJson, "source", ulayer.getLayer_source());
        JSONHelper.putValue(layerJson, "options",
                JSONHelper.createJSONObject("styles", ulayer.getStyle().getStyleForLayerOptions()));
        JSONArray fields = JSONHelper.createJSONArray(ulayer.getFields());
        try {
            JSONHelper.putValue(layerJson, "fields", getFieldsNames(fields));
        } catch (IllegalArgumentException e) {
            JSONHelper.putValue(layerJson, "fields", new JSONArray());
            log.warn("Couldn't put fields array to layerJson", e);
        }
        try {
            JSONHelper.putValue(layerJson, "fieldLocales", getLocalizedFields(lang, fields));
        } catch (IllegalArgumentException e) {
            // do nothing
        }
        // user layer rendering url - override DB url if property is defined
        JSONHelper.putValue(layerJson, "url", getUserLayerTileUrl());
        JSONHelper.putValue(layerJson, "renderingElement", userlayerRenderingElement);

        return layerJson;
    }

    private static String getUserLayerTileUrl() {
        if (PROPERTY_RENDERING_URL == null) {
            // action_route name points to fi.nls.oskari.control.layer.UserLayerTileHandler
            return PropertyUtil.get("oskari.ajax.url.prefix") + "action_route=UserLayerTile&id=";
        }
        return PROPERTY_RENDERING_URL + "&id=";
    }

    // creates JSONArray from fields names [{"name": "the_geom", "type":"MultiPolygon},..]
    private static JSONArray getFieldsNames(final JSONArray json) {
        try {
            JSONArray jsarray =  new JSONArray();
            for(int i = 0; i < json.length(); i++){
                JSONObject obj = json.getJSONObject(i);
                String name = obj.getString("name");
                // skip geometry
                if (DEFAULT_GEOMETRY_NAME.equals(name)) {
                    continue;
                }
                jsarray.put(name);
            }
            return jsarray;
        } catch (Exception e) {
            throw new IllegalArgumentException("Couldn't create JSONArray from fields");
        }
    }
    private static JSONArray getLocalizedFields(final String lang, final JSONArray json) {
        try {
            if (json.length() == 0){
                throw new IllegalArgumentException("No fields");
            }
            JSONArray jsarray =  new JSONArray();
            jsarray.put("ID");
            for(int i = 0; i < json.length(); i++){
                JSONObject obj = json.getJSONObject(i);
                String name = obj.getString("name");
                // skip geometry
                if (DEFAULT_GEOMETRY_NAME.equals(name)) {
                    continue;
                }
                // if get fails throws exception and locales are not added to layer
                JSONObject locales = obj.getJSONObject("locales");
                if (locales.has(lang)) {
                    name = locales.getString(lang);
                } else if (locales.has(DEFAULT_LOCALES_LANGUAGE)){
                    name = locales.getString(DEFAULT_LOCALES_LANGUAGE);
                }
                jsarray.put(name);
            }
            jsarray.put("X");
            jsarray.put("Y");
            return jsarray;
        } catch (Exception e) {
            throw new IllegalArgumentException("Couldn't create locales JSONArray from fields");
        }
    }
}
