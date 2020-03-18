package fi.nls.oskari.control.layer;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionConstants;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.service.capabilities.CapabilitiesCacheService;
import fi.nls.oskari.service.capabilities.OskariLayerCapabilities;
import fi.nls.oskari.util.ResponseHelper;

import javax.servlet.http.HttpServletResponse;

import org.oskari.permissions.PermissionService;
import org.oskari.service.util.ServiceFactory;

import java.nio.charset.StandardCharsets;

/**
 * Returns the Capabilities for layer in XML format if the user has permissions for requested layer.
 */
@OskariActionRoute("GetLayerCapabilities")
public class GetLayerCapabilitiesHandler extends ActionHandler {

    private static final Logger LOG = LogFactory.getLogger(GetLayerCapabilitiesHandler.class);
    private CapabilitiesCacheService capabilitiesService;
    private PermissionHelper permissionHelper;

    public void init() {
        if (permissionHelper == null) {
            permissionHelper = new PermissionHelper(
                    ServiceFactory.getMapLayerService(),
                    OskariComponentManager.getComponentOfType(PermissionService.class));
        }
        if (capabilitiesService == null) {
            capabilitiesService = ServiceFactory.getCapabilitiesCacheService();
        }
    }

    public void setCapabilitiesCacheService(CapabilitiesCacheService capabilitiesService) {
        this.capabilitiesService = capabilitiesService;
    }

    public void setPermissionHelper(final PermissionHelper helper) {
        permissionHelper = helper;
    }

    @Override
    public void handleAction(final ActionParameters params)
            throws ActionException {
        final int layerId = params.getRequiredParamInt(ActionConstants.KEY_ID);
        final OskariLayer layer = permissionHelper.getLayer(layerId, params.getUser());
        final String data = getCapabilities(layer);
        ResponseHelper.writeResponse(params, HttpServletResponse.SC_OK,
                    "text/xml", data.getBytes(StandardCharsets.UTF_8));
    }

    private String getCapabilities(OskariLayer layer) throws ActionException {
        try {
            OskariLayerCapabilities caps = capabilitiesService.getCapabilities(layer);
            // Do not cache this. We don't check whether or not we can parse this response
            if (caps.getId() == null) {
                LOG.info("Capabilities for layer", layer.getId(), "are not cached. Update them using the admin UI.");
            }
            return caps.getData();
        } catch (ServiceException ex) {
            throw new ActionException("Error reading capabilities", ex);
        }
    }

}
