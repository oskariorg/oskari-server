package fi.nls.oskari.map.layer.formatters;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.domain.map.userlayer.UserLayer;
import fi.nls.oskari.domain.map.wfs.WFSLayerOptions;
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
    private static final String FEAUTURE_ID = "__fid";
    private static final String LOCALIZED_ID = "ID";
    private static final String KEY_NAME = "name";
    private static final String KEY_LOCALE = "locale";

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
        WFSLayerOptions wfsOptions = ulayer.getWFSLayerOptions();
        wfsOptions.injectBaseLayerOptions(layer.getOptions());
        JSONHelper.putValue(layerJson, "options", wfsOptions.getOptions());
        try {
            JSONHelper.putValue(layerJson, "propertyNames", getLocalizedPropertyNames(lang, ulayer.getFields()));
        } catch (IllegalArgumentException e) {
            log.warn("Couldn't put fields array to layerJson", e);
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

    private static JSONArray getLocalizedPropertyNames(final String lang, final JSONArray fields) {
        try {

            if (fields.length() == 0){
                return new JSONArray();
            }
            JSONArray arr =  new JSONArray();
            JSONObject id = new JSONObject();
            id.put(KEY_NAME, FEAUTURE_ID);
            id.put(KEY_LOCALE, LOCALIZED_ID);
            arr.put(id);
            for(int i = 0; i < fields.length(); i++){
                JSONObject obj = fields.getJSONObject(i);
                String name = obj.getString("name");
                // skip geometry
                if (DEFAULT_GEOMETRY_NAME.equals(name)) {
                    continue;
                }
                JSONObject locales = obj.optJSONObject("locales");
                String locale = name;
                if (locales != null) {
                    if (locales.has(lang)) {
                        locale = locales.getString(lang);
                    } else if (locales.has(DEFAULT_LOCALES_LANGUAGE)){
                        locale = locales.getString(DEFAULT_LOCALES_LANGUAGE);
                    }
                }
                JSONObject prop = new JSONObject();
                prop.put(KEY_NAME, name);
                prop.put(KEY_LOCALE, locale);
                arr.put(prop);
            }
            return arr;
        } catch (Exception e) {
            throw new IllegalArgumentException("Couldn't create locales JSONArray from fields");
        }
    }
}
