package fi.nls.oskari.control.view.modifier.param;

import fi.nls.oskari.annotation.OskariViewModifier;
import fi.nls.oskari.view.modifier.ParamHandler;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.layer.externalid.OskariLayerExternalIdService;
import fi.nls.oskari.map.layer.externalid.OskariLayerExternalIdServiceMybatisImpl;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.view.modifier.ModifierException;
import fi.nls.oskari.view.modifier.ModifierParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@OskariViewModifier("mapLayers")
public class LayersParamHandler extends ParamHandler {

    private static final Logger log = LogFactory.getLogger(LayersParamHandler.class);
    private static final String KEY_OPACITY = "opacity";
    private static final String KEY_STYLE = "style";
    private static final String KEY_ID = "id";
    private static final String KEY_LAYERS = "layers";
    private static final String KEY_SEL_LAYERS = "selectedLayers";

    private static final String PREFIX_MYPLACES = "myplaces_";
    private static final OskariLayerExternalIdService EXTERNAL_ID_SERVICE = new OskariLayerExternalIdServiceMybatisImpl();
    
    public boolean handleParam(final ModifierParams params) throws ModifierException {
        if(params.getParamValue() == null) {
            return false;
        }

        final String[] layers = params.getParamValue().split(",");
        final JSONArray layersJson = new JSONArray();
        
        for (String layerString : layers) {
            final String[] layerProps = layerString.split(" ");
            final JSONObject layer =  getLayerJson(layerProps, params.getReferer());
            if(layer != null) {
                layersJson.put(layer);
            }
        }

        try {
            final JSONObject mapfullConfig = getBundleConfig(params.getConfig(), BUNDLE_MAPFULL);
            final JSONObject mapfullState = getBundleState(params.getConfig(), BUNDLE_MAPFULL);
            // TODO: maybe check if config had more layers than state
            mapfullState.put(KEY_SEL_LAYERS, layersJson);
            mapfullConfig.put(KEY_LAYERS, layersJson);
        } catch (JSONException je) {
            throw new ModifierException("Could not replace layer arrays");
        }
        return false;
    }
    
    public static JSONObject getLayerJson(final String[] layerParam, final String referer) throws ModifierException {
        String layerId = layerParam[0];

        // Check if the layerId is the externalId of a maplayer
        if (ConversionHelper.getInt(layerId, -1) == -1) {
            Integer layerIdExt = EXTERNAL_ID_SERVICE.findByExternalId(layerId);
            if (layerIdExt != null) {
                // If it is, use the maplayer id instead
                layerId = layerIdExt.toString();
            }
        }

        // Skipping myplaces_.* as they get created in JS
        if (layerId.startsWith(PREFIX_MYPLACES)) {
            // FIXME: handle same way as UNRESTRICTED_USAGE_DOMAINS in GetAppSetupHandler?
            if (!(referer.endsWith("paikkatietoikkuna.fi")
                    || referer.endsWith("nls.fi"))) {
                // not paikkatietoikkuna or nls -> skip myplaces layer
                // otherwise continue so links to published layers work
                // etc
                return null;
            }
        }
        final String layerOpacity = layerParam.length > 1 ? layerParam[1]
                : "100";
        final String layerStyle = layerParam.length > 2 ? layerParam[2]
                : null;

        final JSONObject layer = new JSONObject();
        try {
            layer.put(KEY_ID, layerId);
            layer.put(KEY_OPACITY, layerOpacity);
            if (layerStyle != null) {
                layer.put(KEY_STYLE, layerStyle);
            }
            return layer;
        } catch (JSONException je) {
            log.warn("Could not create layer JSON from params:", layerParam);
            throw new ModifierException("Could not populate layer JSON");
        }
    }
    
}
