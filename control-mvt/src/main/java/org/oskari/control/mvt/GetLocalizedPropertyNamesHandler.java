package org.oskari.control.mvt;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.*;
import fi.nls.oskari.control.feature.AbstractWFSFeaturesHandler;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.domain.map.wfs.WFSLayerConfiguration;
import fi.nls.oskari.service.ServiceRuntimeException;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.util.ResponseHelper;
import fi.nls.oskari.wfs.WFSLayerConfigurationService;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;
import org.oskari.service.user.UserLayerService;
import org.oskari.service.util.ServiceFactory;

import java.util.*;

@OskariActionRoute("GetLocalizedPropertyNames")
public class GetLocalizedPropertyNamesHandler extends AbstractWFSFeaturesHandler {

    private WFSLayerConfigurationService wfsLayerService;

    @Override
    public void init() {
        super.init();
        this.wfsLayerService = ServiceFactory.getWfsLayerService();
    }

    @Override
    public void handleAction(ActionParameters params) throws ActionException {
        final String layerId = params.getRequiredParam(ActionConstants.PARAM_ID);
        Optional<UserLayerService> contentProcessor = getUserContentProsessor(layerId);
        OskariLayer layer = findLayer(layerId, params.getUser(), contentProcessor);
        WFSLayerConfiguration layerConf = wfsLayerService.findConfiguration(layer.getId());
        JSONArray response = new JSONArray();
        if (layerConf == null) {
            ResponseHelper.writeResponse(params, response);
            return;
        }

        String language = params.getHttpParam(ActionConstants.PARAM_LANGUAGE, PropertyUtil.getDefaultLanguage());
        List<String> propNames = layerConf.getSelectedFeatureParams(language);
        List<String> localizedPropNames = layerConf.getFeatureParamsLocales(language);
        if (localizedPropNames.isEmpty() && !propNames.isEmpty() && language != PropertyUtil.getDefaultLanguage()) {
            localizedPropNames = layerConf.getFeatureParamsLocales(PropertyUtil.getDefaultLanguage());
        }
        for (int i = 0; i < propNames.size() && i < localizedPropNames.size(); i++) {
            try {
                JSONObject prop = new JSONObject();
                prop.put("name", propNames.get(i));
                prop.put("locale", localizedPropNames.get(i));
                response.put(prop);
            } catch (JSONException ex) {
                throw new ServiceRuntimeException("Unexpected JSONException occurred");
            }
        }
        ResponseHelper.writeResponse(params, response);
    }
}
