package fi.nls.oskari.control.feature;

import fi.nls.oskari.control.*;
import fi.nls.oskari.control.layer.PermissionHelper;
import org.oskari.user.User;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.service.OskariComponentManager;

import org.oskari.permissions.PermissionService;
import org.oskari.service.user.LayerAccessHandler;
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
    protected Collection<LayerAccessHandler> layerAccessHandlers;

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

        Map<String, LayerAccessHandler> handlerComponents = OskariComponentManager.getComponentsOfType(LayerAccessHandler.class);
        this.layerAccessHandlers = handlerComponents.values();
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
        return processor.isPresent()
                ? findUserLayer(id, user, processor.get())
                : findMapLayer(id, user);
    }

    private OskariLayer findUserLayer(String id, User user, UserLayerService processor) throws ActionException {
        if (!processor.hasViewPermission(id, user)) {
            throw new ActionDeniedException("User doesn't have permissions for requested layer");
        }
        return processor.getOskariLayer(id);
    }

    protected OskariLayer findMapLayer(String id, User user) throws ActionException {
        int layerId;
        try {
            layerId = Integer.parseInt(id);
        } catch (NumberFormatException e) {
            throw new ActionParamsException(ERR_INVALID_ID);
        }
        OskariLayer layer = permissionHelper.getLayer(layerId, user);
        requireWFSLayer(layer);
        return layer;
    }

    /**
     * @deprecated this method is included in {@link #findLayer(String, User, Optional)}
     * and will be marked private in a future release
     */
    @Deprecated
    protected void requireWFSLayer(OskariLayer layer) throws ActionParamsException {
        if (!OskariLayer.TYPE_WFS.equals(layer.getType())) {
            throw new ActionParamsException(ERR_LAYER_TYPE_NOT_WFS);
        }
    }

}
