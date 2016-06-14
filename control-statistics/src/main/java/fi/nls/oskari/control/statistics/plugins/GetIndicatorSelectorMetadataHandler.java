package fi.nls.oskari.control.statistics.plugins;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.control.statistics.GetIndicatorMetadataHandler;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.cache.JedisManager;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.util.ResponseHelper;

/**
 * This interface gives the relevant information for one indicator to the frontend.
 * This information can be subsequently used to query the actual indicator data.
 * 
 * - action_route=GetIndicatorSelectorMetadata
 * 
 * eg.
 * OSKARI_URL?action_route=GetIndicatorSelectorMetadata
 * Response is in JSON, and contains the indicator selector metadata for one indicator.
 * @deprecated - use GetIndicatorMetadataHandler instead
 */
@OskariActionRoute("GetIndicatorSelectorMetadata")
public class GetIndicatorSelectorMetadataHandler extends GetIndicatorMetadataHandler {
    private final static String PARAM_PLUGIN_ID = "plugin_id";
    private final static String PARAM_INDICATOR_ID = "indicator_id";

    @Override
    public void handleAction(ActionParameters ap) throws ActionException {
        final long pluginId = ap.getRequiredParamInt(PARAM_PLUGIN_ID);
        final String indicatorId = ap.getRequiredParam(PARAM_INDICATOR_ID);
        JSONObject response = getIndicatorMetadataJSON(ap.getUser(), pluginId, indicatorId);
        ResponseHelper.writeResponse(ap, response);
    }

    public JSONObject toJSON(StatisticalIndicator indicator) throws JSONException {
        JSONObject pluginIndicatorJSON = new JSONObject();
        Map<String, String> name = indicator.getLocalizedName();
        Map<String, String> description = indicator.getLocalizedDescription();
        Map<String, String> source = indicator.getLocalizedSource();
        List<StatisticalIndicatorLayer> layers = indicator.getLayers();
        StatisticalIndicatorSelectors selectors = indicator.getSelectors();

        pluginIndicatorJSON.put("name", name);
        pluginIndicatorJSON.put("description", description);
        pluginIndicatorJSON.put("source", source);
        pluginIndicatorJSON.put("public", indicator.isPublic());
        pluginIndicatorJSON.put("layers", toJSON(layers));
        pluginIndicatorJSON.put("selectors", toJSON(selectors));
        return pluginIndicatorJSON;
    }


    public JSONArray toJSON(List<StatisticalIndicatorLayer> layers) throws JSONException {
        JSONArray layersJSON = new JSONArray();
        for (StatisticalIndicatorLayer layer: layers) {
            JSONObject layerJSON = new JSONObject();
            layerJSON.put("type", layer.getIndicatorValueType().toString());
            layerJSON.put("layerId", layer.getOskariLayerId());
            layersJSON.put(layerJSON);
        }
        return layersJSON;
    }
}
