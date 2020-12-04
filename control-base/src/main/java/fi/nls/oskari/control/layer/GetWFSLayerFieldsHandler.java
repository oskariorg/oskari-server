package fi.nls.oskari.control.layer;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.control.RestActionHandler;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.map.layer.OskariLayerService;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.service.ServiceRuntimeException;
import fi.nls.oskari.util.ResponseHelper;
import fi.nls.oskari.util.WFSGetLayerFields;
import org.json.JSONException;
import org.json.JSONObject;
import org.oskari.permissions.PermissionService;

/**
 * An action route that returns fields information for WFS layers
 */
@OskariActionRoute("GetWFSLayerFields")
public class GetWFSLayerFieldsHandler extends RestActionHandler {
    private static final String PARAM_LAYER_ID = "layer_id";
    private PermissionHelper permissionHelper;

    @Override
    public void init() {
        try {
            final OskariLayerService layerService = OskariComponentManager.getComponentOfType(OskariLayerService.class);
            final PermissionService permissionService = OskariComponentManager.getComponentOfType(PermissionService.class);
            permissionHelper = new PermissionHelper(layerService, permissionService);
        } catch (Exception e) {
            throw new ServiceRuntimeException("Exception occurred while initializing map layer service", e);
        }
    }

    @Override
    public void handleAction(ActionParameters params) throws ActionException {
        final int layerId = params.getRequiredParamInt(PARAM_LAYER_ID);
        final User user = params.getUser();
        final OskariLayer layer = getLayer(layerId, user);
        final JSONObject response = getLayerFields(layer);
        ResponseHelper.writeResponse(params, response);
    }

    private JSONObject getLayerFields(OskariLayer layer) throws ActionException {
        try {
            JSONObject fields = WFSGetLayerFields.getLayerFields(layer);
            JSONObject locale = getFieldsLocale(layer);
            fields.putOpt("locale", locale);
            return fields;
        } catch (ServiceException ex) {
            throw new ActionException("Error getting layer fields", ex);
        } catch (JSONException ex) {
            throw new ActionException("Invalid attribute locale", ex);
        }
    }

    private JSONObject getFieldsLocale(OskariLayer layer) {
        JSONObject data = layer.getAttributes().optJSONObject("data");
        return data != null ? data.optJSONObject("locale") : null;
    }

    private OskariLayer getLayer(int layerId, User user) throws ActionException {
        final OskariLayer layer = permissionHelper.getLayer(layerId, user);
        if (!OskariLayer.TYPE_WFS.equals(layer.getType())) {
            throw new ActionParamsException("Only WFS supported. Wrong layer type: " + layer.getType());
        }
        return layer;
    }
}
