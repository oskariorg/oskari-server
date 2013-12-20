package fi.nls.oskari.control.layer;

import fi.mml.map.mapwindow.util.OskariLayerWorker;
import fi.mml.portti.service.db.permissions.PermissionsService;
import fi.mml.portti.service.db.permissions.PermissionsServiceIbatisImpl;
import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

/**
 * Get WMS map layers
 */
@OskariActionRoute("GetMapLayers")
public class GetMapLayersHandler extends ActionHandler {


    private PermissionsService permissionsService = new PermissionsServiceIbatisImpl();

    /** Logger */
    private static Logger log = LogFactory.getLogger(GetMapLayersHandler.class);

    final static String LANGUAGE_ATTRIBUTE = "lang";
    private static final String PARM_LAYER_ID = "layer_id";

    @Override
    public void handleAction(ActionParameters params) throws ActionException {

        final String layer_id = params.getHttpParam(PARM_LAYER_ID, "");
        final String lang = params.getHttpParam(LANGUAGE_ATTRIBUTE, params
                .getLocale().getLanguage());

        boolean showEmpty = permissionsService.hasAddLayerPermission(params.getUser());
        /*final JSONObject layers = MapLayerWorker.getListOfAllMapLayers(
                params.getUser(), lang, showEmpty); */

        log.debug("Getting layers");
        final JSONObject layers = OskariLayerWorker.getListOfAllMapLayers(params.getUser(), lang);
        log.debug("Got layers");
        //getListOfAllMapLayers(params.getUser(), lang, showEmpty);

        // FIXME: add needed layer handling for admin bundle
        /*
        // This makes no sense O.o
        JSONObject adminlayers = new JSONObject();
        if (params.getUser().isAdmin()) {
            adminlayers = makeMapLayersAdminJson(layer_id);
            log.debug("Got admin layers");
        }
*/
        log.debug("Writing Response");
        ResponseHelper.writeResponse(params, layers);
        /*
        if (layer_id.isEmpty()) {
            ResponseHelper.writeResponse(params, makeMergeLayerClassJson(
                    layers, adminlayers));
        } else {
            ResponseHelper.writeResponse(params, adminlayers);
        }
        */
    }
/*
    private JSONObject makeMapLayersAdminJson(final String layer_id)
            throws ActionException {

        final List<Layer> allMapLayers = new ArrayList<Layer>();
        if (layer_id.isEmpty())
            allMapLayers.addAll(mapLayerService.findAll());
        else {
            Layer mapLayer = mapLayerService.find(getLayerId(layer_id));
            allMapLayers.add(mapLayer);
        }

        final JSONObject mapJSON = new JSONObject();

        try {
            for (Layer ml : allMapLayers) {
                final JSONObject mapProperties = new JSONObject();

                mapProperties.put("wms_dcp_http", ml.getWms_dcp_http());
                mapProperties.put("resource_url_scheme_pattern", ml.getResource_url_scheme_pattern());
                mapProperties.put("layerType", ml.getType());
                mapProperties.put("wmsName", ml.getWmsName());

                JSONObject names = new JSONObject();
                JSONObject titles = new JSONObject();
                JSONArray locales = new JSONArray();

                Set<String> langs = new TreeSet<String>(ml.getLanguages());
                // make sure we have entries for all supported languages just to be nice...
                langs.addAll(Arrays.asList(PropertyUtil.getSupportedLanguages()));


                for (String lang : langs) {
                    names.put(lang, ml.getName(lang));
                    titles.put(lang, ml.getTitle(lang));
                    JSONObject locale = new JSONObject();
                    locale.put("lang", lang);
                    locale.put("name", ml.getName(lang));
                    locale.put("title", ml.getTitle(lang));
                    locales.put(locale);
                }

                mapProperties.put("name", names);
                mapProperties.put("title", titles);
                mapProperties.put("locales", locales);

                mapProperties.put("wms_parameter_layers", ml.getWms_parameter_layers());
                mapProperties.put("inspireTheme", ml.getInspireThemeId());
                mapProperties.put("tileMatrixSetId", ml.getTileMatrixSetId());
                mapProperties.put("legendImage", ml.getLegendImage());
                mapProperties.put("version", ml.getVersion());
                mapProperties.put("selection_style", ml.getSelection_style());
                mapProperties.put("style", ml.getStyle());
                mapProperties.put("dataUrl", ml.getDataUrl());

                mapProperties.put("epsg", ml.getEpsg());
                mapProperties.put("opacity", ml.getOpacity());
                mapProperties.put("gfiType", ml.getGfiType());
                mapProperties.put("metadataUrl", ml.getMetadataUrl());
                mapProperties.put("tileMatrixSetData", ml.getTileMatrixSetData());
                mapProperties.put("minScale", ml.getMinScale());
                mapProperties.put("maxScale", ml.getMaxScale());

                mapProperties.put("resource_url_scheme", ml.getResource_url_scheme());
                mapProperties.put("resource_daily_max_per_ip", ml.getResource_daily_max_per_ip());
                mapProperties.put("descriptionLink", ml.getDescriptionLink());
                mapProperties.put("xslt", ml.getXslt());
                mapProperties.put("wmsUrl", ml.getWmsUrl());
                mapProperties.put("orderNumber", ml.getOrdernumber());
                mapProperties.put("resource_url_client_pattern", ml.getResource_url_client_pattern());

                mapJSON.accumulate(String.valueOf(ml.getId()), mapProperties);

            }

            return mapJSON;

        } catch (JSONException e) {
            throw new ActionException("Map layer listing failed", e);
        }

    }
    */

    private JSONObject makeMergeLayerClassJson(JSONObject layers,
            JSONObject adminlayers) throws ActionException {

        try {
            if (layers == null)
                return null;
            Iterator<?> keys = layers.keys();

            while (keys.hasNext()) {
                String key = (String) keys.next();
                if (layers.get(key) instanceof JSONArray) {
                    JSONArray layarr = layers.getJSONArray(key);
                    for (int i = 0; i < layarr.length(); i++) {
                        JSONObject lay = layarr.getJSONObject(i);
                        JSONObject adminjs = getAdminJsonLayer(lay
                                .getString("id"), adminlayers);
                        lay.put("admin", adminjs);
                        // Loop sublayers
                        if (lay.has("subLayer")) {
                            if (lay.get("subLayer") instanceof JSONArray) {
                                JSONArray sublayarr = lay
                                        .getJSONArray("subLayer");
                                for (int j = 0; j < sublayarr.length(); j++) {
                                    JSONObject slay = sublayarr
                                            .getJSONObject(j);
                                    JSONObject sadminjs = getAdminJsonLayer(
                                            slay.getString("id"), adminlayers);
                                    slay.put("admin", sadminjs);

                                }
                            }
                        }
                    }

                }
            }

        } catch (Exception e) {

            throw new ActionException("Layers/adminlayers merge failed", e);
        }
        return layers;
    }

    private JSONObject getAdminJsonLayer(String id, JSONObject adminlayers)
            throws ActionException {
        JSONObject adminJSON = new JSONObject();
        try {
            if (adminlayers == null)
                return null;
            Iterator<?> keys = adminlayers.keys();

            while (keys.hasNext()) {
                String key = (String) keys.next();
                if (adminlayers.get(key) instanceof JSONObject) {

                    JSONObject adminlay = adminlayers.getJSONObject(key);
                    if (key.equals(id))
                        return adminlay;

                }
            }

        } catch (Exception e) {

            throw new ActionException("Layers/adminlayers merge failed", e);
        }
        return adminJSON;
    }

    private int getLayerId(String layerId) {
        // 
        return ConversionHelper.getInt(layerId, 0);
    }
}
