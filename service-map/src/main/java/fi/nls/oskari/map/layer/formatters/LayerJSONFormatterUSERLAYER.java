package fi.nls.oskari.map.layer.formatters;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.domain.map.userlayer.UserLayer;
import fi.nls.oskari.domain.map.wfs.WFSSLDStyle;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.wfs.WFSLayerConfigurationService;
import fi.nls.oskari.wfs.WFSLayerConfigurationServiceIbatisImpl;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

/**
 * User layer to oskari layer json
 */
public class LayerJSONFormatterUSERLAYER extends LayerJSONFormatter {

    private static final String USERLAYER_RENDERING_URL = "userlayer.rendering.url";
    private static final String USERLAYER_RENDERING_ELEMENT = "userlayer.rendering.element";

    private static final String PROPERTY_RENDERING_URL = PropertyUtil.getOptional(USERLAYER_RENDERING_URL);
    final String userlayerRenderingElement = PropertyUtil.get(USERLAYER_RENDERING_ELEMENT);

    private static Logger log = LogFactory.getLogger(LayerJSONFormatterUSERLAYER.class);

    /**
     *
     * @param layer
     * @param lang
     * @param isSecure
     * @param ulayer     data in user_layer table
     * @return
     */
    public JSONObject getJSON(OskariLayer layer,
                                     final String lang,
                                     final boolean isSecure,
                                     UserLayer ulayer) {

        final JSONObject layerJson = getBaseJSON(layer, lang, isSecure);

        JSONHelper.putValue(layerJson, "isQueryable", true);
        JSONHelper.putValue(layerJson, "name",ulayer.getLayer_name());
        JSONHelper.putValue(layerJson, "description",ulayer.getLayer_desc());
        JSONHelper.putValue(layerJson, "source",ulayer.getLayer_source());
        JSONHelper.putValue(layerJson, "fields",JSONHelper.createJSONArrayJsonKeys(JSONHelper.createJSONObject(ulayer.getFields())));
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
}
