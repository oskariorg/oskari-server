package fi.nls.oskari.control.admin;

import fi.mml.portti.domain.permissions.Permissions;
import fi.mml.portti.service.db.permissions.PermissionsService;
import fi.mml.portti.service.db.permissions.PermissionsServiceIbatisImpl;
import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.*;
import fi.nls.oskari.domain.Role;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.domain.map.view.Bundle;
import fi.nls.oskari.domain.map.view.View;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.data.domain.OskariLayerResource;
import fi.nls.oskari.map.layer.OskariLayerService;
import fi.nls.oskari.map.layer.OskariLayerServiceIbatisImpl;
import fi.nls.oskari.map.view.ViewException;
import fi.nls.oskari.map.view.ViewService;
import fi.nls.oskari.map.view.ViewServiceIbatisImpl;
import fi.nls.oskari.permission.domain.Resource;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.service.UserService;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.ResponseHelper;
import fi.nls.oskari.view.modifier.ViewModifier;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static fi.nls.oskari.control.ActionConstants.*;

@OskariActionRoute("SystemViews")
public class SystemViewsHandler extends RestActionHandler {

    private Logger log = LogFactory.getLogger(SystemViewsHandler.class);
    private ViewService viewService;
    private OskariLayerService layerService;
    private PermissionsService permissionsService;

    private static final String ERROR_CODE_GUEST_NOT_AVAILABLE = "guest_not_available";
    // is the default in frontend code
    private final static String DEFAULT_SRS = "EPSG:3067";

    public void init() {
        viewService = new ViewServiceIbatisImpl();

        layerService = new OskariLayerServiceIbatisImpl();
        permissionsService = new PermissionsServiceIbatisImpl();
    }

    /**
     * Responds with json containing:
     * - global default viewId
     * - list of roles in the system
     * - roles will have viewId as role-based default view id if it's configured and is not the same as global default.
     * @param params
     * @throws ActionException
     */
    @Override
    public void handleGet(ActionParameters params) throws ActionException {

        final JSONObject response = new JSONObject();
        final JSONArray list = new JSONArray();

        final long globalDefaultViewId = viewService.getDefaultViewId();
        JSONHelper.putValue(response, "viewId", globalDefaultViewId);

        final Role[] roles;
        try {
            roles = UserService.getInstance().getRoles();
        } catch (ServiceException e) {
            throw new ActionException("Couldn't get roles listing", e);
        }
        for (Role role : roles) {
            final JSONObject json = new JSONObject();
            JSONHelper.putValue(json, PARAM_ID, role.getId());
            JSONHelper.putValue(json, "name", role.getName());

            final long viewId = viewService.getDefaultViewIdForRole(role.getName());
            if (viewId != globalDefaultViewId) {
                JSONHelper.putValue(json, "viewId", viewId);
            }
            list.put(json);
        }

        JSONHelper.putValue(response, "roles", list);
        JSONHelper.putValue(response, "timestamp", new Date());
        ResponseHelper.writeResponse(params, response);
    }

    /**
     * Loads view with id and modifies mapfull state using these parameters:
     * - id = view id
     * - east, north, selectedLayers, srs, zoom = mapfull state variables
     * - force = if true update even if selectedLayers have layers that Guest users don't have permission for
     * @param params
     * @throws ActionException
     */
    @Override
    public void handlePost(final ActionParameters params) throws ActionException {
        final int id = params.getRequiredParamInt(PARAM_ID);
        final View view = viewService.getViewWithConf(id);
        if(view == null) {
            throw new ActionParamsException("Invalid view id " + id);
        }
        final Bundle mapfull = view.getBundleByName(ViewModifier.BUNDLE_MAPFULL);
        if(mapfull == null) {
            throw new ActionParamsException("View (" + id + ") doesn't include bundle: " + ViewModifier.BUNDLE_MAPFULL);
        }

        // validate SRS
        final String viewSrs = getViewSRS(mapfull.getConfigJSON());
        if(!params.getRequiredParam(PARAM_SRS).equalsIgnoreCase(viewSrs)) {
            // TODO: projection conversion if SRS is different
            throw new ActionParamsException("Views have different projections, not implemented yet");
        }

        final JSONObject state = mapfull.getStateJSON();
        setupLocation(params, state);

        final JSONArray layers = JSONHelper.createJSONArray(params.getRequiredParam("selectedLayers"));

        final boolean ignoreLayerPermissionCheck = "true".equalsIgnoreCase(params.getHttpParam("force", "false"));
        if(ignoreLayerPermissionCheck) {
            JSONHelper.putValue(state, "selectedLayers", layers);
        }
        else {
            final List<String> layerIdList = new ArrayList<String>();
            for(int i = 0; i < layers.length(); ++i) {
                JSONObject layer = layers.optJSONObject(i);
                layerIdList.add(layer.optString(PARAM_ID));
            }

            final List<OskariLayer> notAvailableForGuest = getLayersNotAvailableForGuest(layerIdList);
            if(!notAvailableForGuest.isEmpty()) {
                final JSONArray list = new JSONArray();
                for (OskariLayer layer :notAvailableForGuest) {
                    list.put(layer.getId());
                }
                // TODO: Respond with error and end execution
                JSONObject info = JSONHelper.createJSONObject("selectedLayers", list);
                JSONHelper.putValue(info, "code", ERROR_CODE_GUEST_NOT_AVAILABLE);
                throw new ActionParamsException("Contains layers not available for guests", info);
            }
            JSONHelper.putValue(state, "selectedLayers", layers);
        }

        try {
            viewService.updateBundleSettingsForView(view.getId(), mapfull);
        } catch (ViewException ex) {
            throw new ActionException("Error updating view settings", ex);
        }

        ResponseHelper.writeResponse(params, JSONHelper.createJSONObject("success", "true"));
    }
    private String getViewSRS(final JSONObject config)  {
        // config = {"mapOptions":{"srsName":"EPSG:3067", ...}, ...}
        JSONObject mapOptions = config.optJSONObject("mapOptions");

        if(mapOptions == null) {
            return DEFAULT_SRS;
        }
        String optSrs = mapOptions.optString("srsName");
        if(optSrs == null) {
            return DEFAULT_SRS;
        }
        return optSrs;
    }

    private void setupLocation(final ActionParameters params, final JSONObject state) throws ActionException {

        final double north = ConversionHelper.getDouble(params.getRequiredParam(ViewModifier.KEY_NORTH), -1);
        if(north != -1) {
            JSONHelper.putValue(state, ViewModifier.KEY_NORTH, north);
        }
        final double east = ConversionHelper.getDouble(params.getRequiredParam(ViewModifier.KEY_EAST), -1);
        if(east != -1) {
            JSONHelper.putValue(state, ViewModifier.KEY_EAST, east);
        }
        JSONHelper.putValue(state, ViewModifier.KEY_ZOOM, params.getRequiredParamInt(ViewModifier.KEY_ZOOM));
    }

    private List<OskariLayer> getLayersNotAvailableForGuest(final List<String> layerIdList) throws ActionException {
        final List<OskariLayer> layers = layerService.find(layerIdList);
        final List<OskariLayer> notAvailable = new ArrayList<OskariLayer>();
        try {
            User guest = UserService.getInstance().getGuestUser();
            for(OskariLayer layer : layers) {
                final Resource resource = permissionsService.findResource(new OskariLayerResource(layer));
                final boolean hasPermssion = resource.hasPermission(guest, Permissions.PERMISSION_TYPE_VIEW_LAYER);
                if(!hasPermssion) {
                    notAvailable.add(layer);
                }
            }
        } catch (ServiceException ex) {
            throw new ActionException("Couldn't get guest user", ex);
        }

        return notAvailable;
    }

    @Override
    public void preProcess(ActionParameters params) throws ActionException {
        if (!params.getUser().isAdmin()) {
            throw new ActionDeniedException("Admin only");
        }
    }

}
