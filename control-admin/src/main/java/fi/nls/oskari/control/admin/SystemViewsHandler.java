package fi.nls.oskari.control.admin;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.*;
import fi.nls.oskari.domain.Role;
import fi.nls.oskari.domain.map.view.Bundle;
import fi.nls.oskari.domain.map.view.View;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.view.ViewException;
import fi.nls.oskari.map.view.ViewService;
import fi.nls.oskari.map.view.ViewServiceIbatisImpl;
import fi.nls.oskari.user.IbatisRoleService;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.ResponseHelper;
import fi.nls.oskari.view.modifier.ViewModifier;
import org.json.JSONArray;
import org.json.JSONObject;
import static fi.nls.oskari.control.ActionConstants.*;

import java.util.Date;
import java.util.List;

@OskariActionRoute("SystemViews")
public class SystemViewsHandler extends RestActionHandler {

    private Logger log = LogFactory.getLogger(SystemViewsHandler.class);
    private static ViewService viewService;
    private static IbatisRoleService roleService;

    public void init() {
        viewService = new ViewServiceIbatisImpl();
        roleService = new IbatisRoleService();
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

        final List<Role> roles = roleService.findAll();
        for (Role role : roles) {
            final JSONObject json = new JSONObject();
            JSONHelper.putValue(json, "id", role.getId());
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

        final JSONObject state = mapfull.getStateJSON();
        setupLocation(params, state);

        final JSONArray layers = JSONHelper.createJSONArray(params.getRequiredParam("selectedLayers"));
        final boolean ignoreLayerPermissionCheck = "true".equalsIgnoreCase(params.getHttpParam("force", "false"));

        try {
            viewService.updateBundleSettingsForView(view.getId(), mapfull);
        } catch (ViewException ex) {
            throw new ActionException("Error updating view settings", ex);
        }

        ResponseHelper.writeResponse(params, JSONHelper.createJSONObject("success", "true"));
    }

    private void setupLocation(final ActionParameters params, final JSONObject state) throws ActionException {
        final String srs = params.getRequiredParam("srs");
        if(state.optString("srs").equalsIgnoreCase(srs)) {
            // TODO: projection conversion if SRS is different
            throw new ActionParamsException("Views have different projections, not implemented yet");
        }

        final double north = ConversionHelper.getDouble(params.getRequiredParam("north"), -1);
        if(north != -1) {
            JSONHelper.putValue(state, "north", north);
        }
        final double east = ConversionHelper.getDouble(params.getRequiredParam("east"), -1);
        if(east != -1) {
            JSONHelper.putValue(state, "east", east);
        }
        JSONHelper.putValue(state, "zoom", params.getRequiredParamInt("zoom"));
    }

    @Override
    public void preProcess(ActionParameters params) throws ActionException {
        if (!params.getUser().isAdmin()) {
            throw new ActionDeniedException("Admin only");
        }
    }

}