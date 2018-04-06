package fi.nls.oskari.control.layer;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionDeniedException;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.layer.OskariLayerService;
import fi.nls.oskari.map.layer.OskariLayerServiceIbatisImpl;
import fi.nls.oskari.util.ResponseHelper;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.List;

/**
 * Get WMSservices out of portti_maplayer table
 * Not used anywhere currently
 */
@OskariActionRoute("GetWmsServices")
public class GetWmsServicesHandler extends ActionHandler {

    private OskariLayerService mapLayerService = new OskariLayerServiceIbatisImpl();
    private static final Logger log = LogFactory.getLogger(SaveLayerHandler.class);

    private static final String KEY_WMSURL = "wmsUrl";

    public void handleAction(ActionParameters params) throws ActionException {

        if(!params.getUser().isAdmin()) {
            throw new ActionDeniedException("Unauthorized user tried to get wmsservices");
        }
        try {

            ResponseHelper.writeResponse(params, makeServicesJson(
                    makeMapLayersJson(), KEY_WMSURL));

        } catch (Exception e) {
            throw new ActionException("Couldn't request WMS services", e);
        }
    }

    private JSONObject makeServicesJson(JSONObject layers, final String key) {
        if (key == null || key.isEmpty() || layers == null) {
            return new JSONObject();
        }

        final JSONObject servicesJSON = new JSONObject();
        try {
            final JSONObject serveJSON = findPropertiesJson(key, layers, new JSONObject());
            // populating object with properties like {<count> : [<skey>]}
            // seems a bit weird
            // TODO: check this ^
            final Iterator<String> keys = serveJSON.keys();
            for(int count = 1; keys.hasNext(); count++) {
                final String skey = keys.next();
                servicesJSON.accumulate(String.valueOf(count), skey);
            }
        } catch (Exception e) {
            log.warn(e, "JSON Capabilities parse failed");
        }
        return servicesJSON;
    }

    private JSONObject findPropertiesJson(String mykey, JSONObject js,
                                                JSONObject jsproperties) {
        JSONObject jssub = null;

        try {
            if (js == null)
                return null;
            Iterator<?> keys = js.keys();

            while (keys.hasNext()) {
                String key = (String) keys.next();
                if (js.get(key) instanceof JSONObject) {

                    JSONObject jssub2 = js.getJSONObject(key);
                    jssub2 = findPropertiesJson(mykey, jssub2, jsproperties);

                } else {
                    if (mykey.toUpperCase().equals(key.toUpperCase())) {
                        jsproperties.accumulate(js.get(key).toString(), key);
                    }
                }
            }
            jssub = jsproperties;
        } catch (JSONException e) {
            log.warn(e, "JSON parse failed");
        }
        return jssub;

    }

    private JSONObject makeMapLayersJson() {

        final List<OskariLayer> allMapLayers = mapLayerService.findAll();

        final JSONObject mapJSON = new JSONObject();

        try {

            for (OskariLayer ml : allMapLayers) {
                if(!OskariLayer.TYPE_WMS.equals(ml.getType())) {
                    continue;
                }
                JSONObject mapProperties = new JSONObject();

                List<String> languages = ml.getLanguages();
                // TODO: maybe use fi.nls.oskari.map.layer.formatters.LayerJSONFormatter?
                for (String lang : languages) {
                    mapProperties.put("name" + Character.toUpperCase(lang.charAt(0)) + lang.substring(1), ml.getName(lang));
                    mapProperties.put("title" + Character.toUpperCase(lang.charAt(0)) + lang.substring(1), ml.getTitle(lang));
                }

                mapProperties.put("wmsName", ml.getName());
                mapProperties.put("wmsUrl", ml.getUrl());
                mapProperties.put("opacity", ml.getOpacity());
                mapProperties.put("style", ml.getStyle());
                mapProperties.put("minScale", ml.getMinScale());
                mapProperties.put("maxScale", ml.getMaxScale());

//                mapProperties.put("descriptionLink", ml.getDescriptionLink());
                mapProperties.put("legendImage", ml.getLegendImage());

                mapProperties.put("maplayerGroups", ml.getMaplayerGroup().getId());
                mapProperties.put("dataUrl", ml.getMetadataId());
//                mapProperties.put("metadataUrl", ml.getMetadataUrl());
//                mapProperties.put("orderNumber", ml.getOrdernumber());

                mapProperties.put("layerType", ml.getType());
/*
                mapProperties.put("wms_dcp_http", ml.getWms_dcp_http());
                mapProperties.put("wms_parameter_layers", ml
                        .getWms_parameter_layers());
                mapProperties.put("resource_url_scheme", ml
                        .getResource_url_scheme());
                mapProperties.put("resource_url_scheme_pattern", ml
                        .getResource_url_scheme_pattern());
                mapProperties.put("resource_url_client_pattern", ml
                        .getResource_url_client_pattern());
                mapProperties.put("resource_daily_max_per_ip", ml
                        .getResource_daily_max_per_ip());
*/
                mapProperties.put("xslt", ml.getGfiXslt());
                mapProperties.put("gfiType", ml.getGfiType());
                mapProperties.put("gfiContent", ml.getGfiContent());

                mapProperties.put("realtime", ml.getRealtime());
                mapProperties.put("refreshRate", ml.getRefreshRate());
                /*
                mapProperties.put("selection_style", ml.getSelection_style());
                mapProperties.put("version", ml.getVersion());
                mapProperties.put("epsg", ml.getEpsg());
*/
                mapJSON.accumulate(String.valueOf(ml.getId()), mapProperties);

            }

            return mapJSON;

        } catch (JSONException e) {
            log.warn(e, "JSON create failed - maplayers");
        }
        return mapJSON;
    }

}