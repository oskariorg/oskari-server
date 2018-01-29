package fi.nls.oskari.control.admin;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.oskari.capabilities.CapabilitiesUpdateResult;
import org.oskari.capabilities.CapabilitiesUpdateService;
import org.oskari.service.util.ServiceFactory;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionConstants;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.map.layer.OskariLayerService;
import fi.nls.oskari.map.view.ViewService;
import fi.nls.oskari.map.view.util.ViewHelper;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.service.capabilities.CapabilitiesCacheService;
import fi.nls.oskari.util.ResponseHelper;

/**
 * ActionRoute to update the capabilities of layers
 * Can be called with 'id' parameter where the value is a layer id.
 * If 'id' can not be parsed into an integer then this action route
 * will try to update the capabilities of ALL layers.
 *
 * Responds with a JSON object describing the result.
 * The result contains two keys, "success" and "error".
 *
 * "success" is an array of the ids of the layers whose
 * capabilities were succesfully updated.
 *
 * "error" is an object consisting of multiple
 * "{layerId}": "{errorMsg}" entries each describing what
 * went wrong with updating that particular layer.
 *
 * Both "success" and "error" might be empty
 */
@OskariActionRoute("UpdateCapabilities")
public class UpdateCapabilitiesHandler extends ActionHandler {

    private OskariLayerService layerService;
    private CapabilitiesCacheService capabilitiesCacheService;
    private CapabilitiesUpdateService capabilitiesUpdateService;
    private ViewService viewService;

    public UpdateCapabilitiesHandler() {
        // No-param constructor for @OskariActionRoute
    }

    public UpdateCapabilitiesHandler(OskariLayerService layerService,
            CapabilitiesCacheService capabilitiesCacheService,
            ViewService viewService) {
        // Full-param constructor if one is required
        this.layerService = layerService;
        this.capabilitiesCacheService = capabilitiesCacheService;
        this.viewService = viewService;
    }

    @Override
    public void init() {
        // Lazily populate the fields if they haven't been set by the full-param constructor
        if (layerService == null) {
            layerService = ServiceFactory.getMapLayerService();
        }
        if (capabilitiesCacheService == null) {
            capabilitiesCacheService = ServiceFactory.getCapabilitiesCacheService();
        }
        if (capabilitiesUpdateService == null) {
            capabilitiesUpdateService = new CapabilitiesUpdateService(
                    layerService, capabilitiesCacheService);
        }
        if (viewService == null) {
            viewService = ServiceFactory.getViewService();
        }
    }

    @Override
    public void handleAction(ActionParameters params) throws ActionException {
        params.requireAdminUser();

        int layerId = params.getHttpParam(ActionConstants.KEY_ID, -1);
        List<OskariLayer> layers = getLayersToUpdate(layerId, -1);
        Set<String> systemCRSs = getSystemCRSs();

        List<CapabilitiesUpdateResult> result =
                capabilitiesUpdateService.updateCapabilities(layers, systemCRSs);

        JSONObject response = createResponse(result);
        ResponseHelper.writeResponse(params, response);
    }

    private List<OskariLayer> getLayersToUpdate(int layerId, int notSpecified)
            throws ActionParamsException {
        if (layerId == notSpecified) {
            return layerService.findAll();
        }
        OskariLayer layer = layerService.find(layerId);
        if (layer == null) {
            throw new ActionParamsException("Unknown layer id:" + layerId);
        }
        return Collections.singletonList(layer);
    }

    private Set<String> getSystemCRSs() throws ActionException {
        try {
            return ViewHelper.getSystemCRSs(viewService);
        } catch (ServiceException e) {
            throw new ActionException("Failed to get systemCRSs", e);
        }
    }

    private JSONObject createResponse(List<CapabilitiesUpdateResult> result)
            throws ActionException {
        JSONArray success = new JSONArray();
        JSONObject errors = new JSONObject();

        for (CapabilitiesUpdateResult r : result) {
            if (r.getErrorMessage() == null) {
                success.add(r.getLayerId());
            } else {
                errors.put(r.getLayerId(), r.getErrorMessage());
            }
        }

        JSONObject response = new JSONObject();
        response.put("success", success);
        response.put("error", errors);
        return response;
    }
}
