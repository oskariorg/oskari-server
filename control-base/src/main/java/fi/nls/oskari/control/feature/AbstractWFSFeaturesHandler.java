package fi.nls.oskari.control.feature;

import fi.nls.oskari.control.*;
import fi.nls.oskari.control.layer.PermissionHelper;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.service.OskariComponentManager;

import org.oskari.permissions.PermissionService;
import org.oskari.service.user.UserLayerService;
import org.oskari.service.util.ServiceFactory;
import org.oskari.service.wfs.client.CachingOskariWFSClient;
import org.oskari.service.wfs.client.OskariFeatureClient;
import org.oskari.service.wfs.client.OskariWFSClient;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * Base class for ActionHandlers that want to deal with vector layers
 */
public abstract class AbstractWFSFeaturesHandler extends ActionHandler {

    protected static final String ERR_INVALID_ID = "Invalid id";
    protected static final String ERR_LAYER_TYPE_NOT_WFS = "Specified layer is not a WFS layer";

    protected OskariFeatureClient featureClient;
    protected PermissionHelper permissionHelper;
    protected Collection<UserLayerService> userContentProcessors;

    protected void setPermissionHelper(PermissionHelper permissionHelper) {
        this.permissionHelper = permissionHelper;
    }

    @Override
    public void init() {
        if (featureClient == null) {
            featureClient = new OskariFeatureClient(createWFSClient());
        }
        if (permissionHelper == null) {
            permissionHelper = new PermissionHelper(
                    ServiceFactory.getMapLayerService(),
                    OskariComponentManager.getComponentOfType(PermissionService.class));
        };
        Map<String, UserLayerService> components = OskariComponentManager.getComponentsOfType(UserLayerService.class);
        this.userContentProcessors = components.values();
    }

    protected OskariWFSClient createWFSClient() {
        return new CachingOskariWFSClient();
    }

    protected Optional<UserLayerService> getUserContentProsessor(String layerId) {
        return userContentProcessors.stream()
                .filter(proc -> proc.isUserContentLayer(layerId))
                .findAny();
    }

    protected OskariLayer findLayer(String id, User user, Optional<UserLayerService> processor) throws ActionException {
        int layerId = getLayerId(id, processor);
        OskariLayer layer = permissionHelper.getLayer(layerId, user);
        if (!OskariLayer.TYPE_WFS.equals(layer.getType())) {
            return layer;
        }
        if (processor.isPresent() && !processor.get().hasViewPermission(id, user)) {
            throw new ActionDeniedException("User doesn't have permissions for requested layer");
        }
        return layer;
    }

    protected void requireWFSLayer(OskariLayer layer) throws ActionParamsException {
        if (!OskariLayer.TYPE_WFS.equals(layer.getType())) {
            throw new ActionParamsException(ERR_LAYER_TYPE_NOT_WFS);
        }
    }
    
    private int getLayerId(String id, Optional<UserLayerService> processor) throws ActionParamsException {
        try {
            return processor.map(UserLayerService::getBaselayerId)
                    .orElseGet(() -> Integer.parseInt(id));
        } catch (NumberFormatException e) {
            throw new ActionParamsException(ERR_INVALID_ID);
        }
    }
    
}
