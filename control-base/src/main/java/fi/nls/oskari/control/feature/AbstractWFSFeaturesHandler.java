package fi.nls.oskari.control.feature;

import fi.nls.oskari.control.*;
import fi.nls.oskari.control.layer.PermissionHelper;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.service.OskariComponentManager;

import org.oskari.service.user.UserLayerService;    
import org.oskari.service.util.ServiceFactory;
import org.oskari.service.wfs.client.CachingOskariWFSClient;
import org.oskari.service.wfs.client.OskariFeaturesClient;
import org.oskari.service.wfs.client.OskariWFSClient;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public abstract class AbstractWFSFeaturesHandler extends ActionHandler {

    protected static final String ERR_INVALID_ID = "Invalid id";
    protected static final String ERR_LAYER_TYPE_NOT_WFS = "Specified layer is not a WFS layer";

    protected OskariFeaturesClient features;
    protected PermissionHelper permissionHelper;
    protected Collection<UserLayerService> userContentProcessors;

    protected void setPermissionHelper(PermissionHelper permissionHelper) {
        this.permissionHelper = permissionHelper;
    }

    @Override
    public void init() {
        if (features == null) {
            features = new OskariFeaturesClient(createWFSClient());
        }
        if (permissionHelper == null) {
            permissionHelper = new PermissionHelper(
                    ServiceFactory.getMapLayerService(),
                    ServiceFactory.getPermissionsService());
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
            throw new ActionParamsException(ERR_LAYER_TYPE_NOT_WFS);
        }
        return layer;
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
