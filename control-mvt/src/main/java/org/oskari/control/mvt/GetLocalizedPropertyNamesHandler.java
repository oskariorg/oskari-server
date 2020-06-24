package org.oskari.control.mvt;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionConstants;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.feature.AbstractWFSFeaturesHandler;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.domain.map.wfs.WFSLayerAttributes;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.util.ResponseHelper;
import org.json.JSONArray;
import org.json.JSONObject;
import org.oskari.service.user.UserLayerService;

import java.util.List;
import java.util.Optional;

@OskariActionRoute("GetLocalizedPropertyNames")
public class GetLocalizedPropertyNamesHandler extends AbstractWFSFeaturesHandler {

    @Override
    public void init() {
        super.init();
    }

    @Override
    public void handleAction(ActionParameters params) throws ActionException {
        final String layerId = params.getRequiredParam(ActionConstants.PARAM_ID);
        Optional<UserLayerService> contentProcessor = getUserContentProsessor(layerId);
        OskariLayer layer = findLayer(layerId, params.getUser(), contentProcessor);

        String language = params.getHttpParam(ActionConstants.PARAM_LANGUAGE, PropertyUtil.getDefaultLanguage());
        WFSLayerAttributes attributes = new WFSLayerAttributes(layer.getAttributes());
        List<String> filter = attributes.getSelectedAttributes(language);
        JSONArray response = new JSONArray();
        if (filter.isEmpty()) {
            // TODO: change API so layer can have locale without filter!
            ResponseHelper.writeResponse(params, response);
            return;
        }
        JSONObject locale = attributes.getLocalization(language).orElse(attributes.getLocalization().orElse(new JSONObject()));
        attributes.getSelectedAttributes(language).stream().forEach(attr -> {
            JSONObject resp = new JSONObject();
            JSONHelper.putValue(resp, "name", attr);
            JSONHelper.putValue(resp, "locale", locale.optString(attr, attr));
            response.put(resp);
        });
        ResponseHelper.writeResponse(params, response);
    }
}
