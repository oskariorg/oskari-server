package fi.nls.oskari.control.layer;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.service.capabilities.CapabilitiesCacheService;
import fi.nls.oskari.service.capabilities.OskariLayerCapabilities;
import fi.nls.oskari.util.ResponseHelper;

import javax.servlet.http.HttpServletResponse;

import org.oskari.service.util.ServiceFactory;

import static fi.nls.oskari.control.ActionConstants.KEY_ID;

/**
 * Returns the Capabilities for layer in XML format if the user has permissions for requested layer.
 */
@OskariActionRoute("GetLayerCapabilities")
public class GetLayerCapabilitiesHandler extends ActionHandler {

    private final CapabilitiesCacheService capabilitiesService = OskariComponentManager.getComponentOfType(CapabilitiesCacheService.class);
    private PermissionHelper permissionHelper;

    public void init() {
        if(permissionHelper == null) {
            permissionHelper = new PermissionHelper(ServiceFactory.getMapLayerService(), ServiceFactory.getPermissionsService());
        }
    }

    public void setPermissionHelper(final PermissionHelper helper) {
        permissionHelper = helper;
    }

    /**
     * Action handler
     *
     * @param params Parameters
     * @throws ActionException
     */
    public void handleAction(final ActionParameters params)
            throws ActionException {

        // Resolve layer
        final String layerId = params.getRequiredParam(KEY_ID);
        final OskariLayer layer = permissionHelper.getLayer(layerId, params.getUser());

        try {
            OskariLayerCapabilities caps = capabilitiesService.getCapabilities(layer);
            final HttpServletResponse response = params.getResponse();
            response.setContentType("text/xml");
            ResponseHelper.writeResponse(params, caps.getData());
        } catch (ServiceException ex) {
            throw new ActionException("Error reading capabilities", ex);
        }
    }
}
