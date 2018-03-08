package fi.nls.oskari.control.admin;

import fi.mml.map.mapwindow.util.OskariLayerWorker;
import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.control.RestActionHandler;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.search.channel.WFSChannelHandler;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.ResponseHelper;
import fi.nls.oskari.wfs.WFSSearchChannelsConfiguration;
import fi.nls.oskari.wfs.WFSSearchChannelsService;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static fi.nls.oskari.control.ActionConstants.PARAM_SRS;

@OskariActionRoute("SearchWFSChannel")
public class SearchWFSChannelActionHandler extends RestActionHandler {

    private static final Logger log = LogFactory.getLogger(SearchWFSChannelActionHandler.class);
    private static final String PARAM_ID = "id";
    private static final String PARAM_WFS_ID = "wfsLayerId";
    private static final String PARAM_LOCALE = "locale";
    private static final String PARAM_PARAMS_FOR_SEARCH = "paramsForSearch";
    private static final String PARAM_IS_DEFAULT = "isDefault";
    private static final String PARAM_CONFIG = "config";

    private WFSSearchChannelsService channelService;

    @Override
    public void init() {
        super.init();
        channelService = OskariComponentManager.getComponentOfType(WFSSearchChannelsService.class);
    }

    @Override
    public void preProcess(ActionParameters params)
            throws ActionException {
        super.preProcess(params);
        // Only admin user
        params.requireAdminUser();
    }

    @Override
    public void handleGet(ActionParameters params)
            throws ActionException {

        JSONObject response = new JSONObject();
        JSONArray channelsJSONArray = new JSONArray();
        try {
            for(WFSSearchChannelsConfiguration channel : channelService.findChannels()) {
                JSONObject channelJSON = channel.getAsJSONObject();
                List<String> layerIds = new ArrayList<String>();
                layerIds.add(String.valueOf(channel.getWFSLayerId()));
                JSONObject userLayers = OskariLayerWorker.getListOfMapLayersById(layerIds, params.getUser(), params.getLocale().getLanguage(), params.getHttpParam(PARAM_SRS));
                JSONArray layers = userLayers.getJSONArray(OskariLayerWorker.KEY_LAYERS);

                if(layers.length() > 0){
                    channelsJSONArray.put(channelJSON);
                }
            }
        } catch (Exception ex){
            log.error(org.apache.commons.lang.exception.ExceptionUtils.getStackTrace(ex));
            throw new ActionParamsException("Couldn't get WFS search channels");
        }
        JSONHelper.putValue(response, "channels", channelsJSONArray);
        JSONArray handlers = new JSONArray();
        Map<String, WFSChannelHandler> handlerMap =
                OskariComponentManager.getComponentsOfType(WFSChannelHandler.class);
        for(String id: handlerMap.keySet()) {
            handlers.put(id);
        }
        JSONHelper.putValue(response, "handlers", handlers);
        ResponseHelper.writeResponse(params, response);
    }

    @Override
    public void handleDelete(ActionParameters params)
            throws ActionException {
        int channelId = ConversionHelper.getInt(params.getRequiredParam(PARAM_ID), -1);

        try {
            JSONObject response = new JSONObject();
            channelService.delete(channelId);
            JSONHelper.putValue(response, "success", true);
            ResponseHelper.writeResponse(params, response);
        } catch (Exception ex) {
            log.error(org.apache.commons.lang.exception.ExceptionUtils.getStackTrace(ex));
            throw new ActionParamsException("Couldn't delete WFS search channel");
        }
    }

    @Override
    public void handlePost(ActionParameters params)
            throws ActionException {

        try {
            WFSSearchChannelsConfiguration conf = parseConfig(params);
            conf.setId(ConversionHelper.getInt(params.getRequiredParam(PARAM_ID), -1));

            JSONObject response = new JSONObject();
            channelService.update(conf);
            JSONHelper.putValue(response, "success", true);
            ResponseHelper.writeResponse(params, response);
        } catch (Exception ex) {
            log.error(org.apache.commons.lang.exception.ExceptionUtils.getStackTrace(ex));
            throw new ActionParamsException("Couldn't update WFS search channel");
        }
    }

    @Override
    public void handlePut(ActionParameters params)
            throws ActionException {

        try {
            WFSSearchChannelsConfiguration conf = parseConfig(params);
            long newId = channelService.insert(conf);
            JSONObject response = new JSONObject();
            JSONHelper.putValue(response, "success", newId > 0);
            ResponseHelper.writeResponse(params, response);
        } catch (Exception ex) {
            log.error(org.apache.commons.lang.exception.ExceptionUtils.getStackTrace(ex));
            throw new ActionParamsException("Couldn't add WFS search channel");
        }
    }

    private WFSSearchChannelsConfiguration parseConfig(ActionParameters params)
            throws Exception {

        WFSSearchChannelsConfiguration conf = new WFSSearchChannelsConfiguration();
        conf.setWFSLayerId(ConversionHelper.getInt(params.getRequiredParam(PARAM_WFS_ID), -1));
        conf.setIsDefault(ConversionHelper.getBoolean(params.getRequiredParam(PARAM_IS_DEFAULT), false));
        conf.setParamsForSearch(new JSONArray(params.getRequiredParam(PARAM_PARAMS_FOR_SEARCH)));
        conf.setLocale(new JSONObject(params.getRequiredParam(PARAM_LOCALE)));
        conf.setConfig(new JSONObject(params.getRequiredParam(PARAM_CONFIG)));
        return conf;
    }
}
