package org.oskari.control.mvt;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.*;
import fi.nls.oskari.control.layer.PermissionHelper;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.domain.map.wfs.WFSLayerConfiguration;
import fi.nls.oskari.service.ServiceRuntimeException;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.util.ResponseHelper;
import fi.nls.oskari.wfs.WFSLayerConfigurationService;
import org.json.JSONException;
import org.json.JSONObject;
import org.oskari.service.util.ServiceFactory;

import java.util.*;

@OskariActionRoute("GetLocalizedPropertyNames")
public class GetLocalizedPropertyNames extends ActionHandler {

    private PermissionHelper permissionHelper;
    private WFSLayerConfigurationService wfsLayerService;

    @Override
    public void init() {
        this.permissionHelper = new PermissionHelper(
                ServiceFactory.getMapLayerService(),
                ServiceFactory.getPermissionsService());
        this.wfsLayerService = ServiceFactory.getWfsLayerService();
    }

    @Override
    public void handleAction(ActionParameters params) throws ActionException {
        final int layerId = params.getRequiredParamInt(ActionConstants.PARAM_ID);
        String language = params.getHttpParam(ActionConstants.PARAM_LANGUAGE, PropertyUtil.getDefaultLanguage());
        JSONObject response = new JSONObject();

        checkLayerPermissions(layerId, params.getUser());
        WFSLayerConfiguration layerConf = wfsLayerService.findConfiguration(layerId);
        if (layerConf == null) {
            ResponseHelper.writeResponse(params, response);
            return;
        }

        List<String> propNames = layerConf.getSelectedFeatureParams(language);
        List<String> localizedPropNames = layerConf.getFeatureParamsLocales(language);
        if (localizedPropNames.isEmpty() && !propNames.isEmpty() && language != PropertyUtil.getDefaultLanguage()) {
            localizedPropNames = layerConf.getFeatureParamsLocales(PropertyUtil.getDefaultLanguage());
        }
        for (int i = 0; i < propNames.size() && i < localizedPropNames.size(); i++) {
            try {
                response.put(propNames.get(i), localizedPropNames.get(i));
            } catch (JSONException ex) {
                throw new ServiceRuntimeException("Unexpected JSONException occurred");
            }
        }
        ResponseHelper.writeResponse(params, response);
    }

    private void checkLayerPermissions(int layerId, User user) throws ActionException {
        OskariLayer layer = permissionHelper.getLayer(layerId, user);
        if (!OskariLayer.TYPE_WFS.equals(layer.getType())) {
            throw new ActionParamsException("Specified layer is not a WFS layer");
        }
    }
}
