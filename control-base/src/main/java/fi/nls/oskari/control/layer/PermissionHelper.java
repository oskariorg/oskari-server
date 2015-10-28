package fi.nls.oskari.control.layer;

import fi.mml.portti.domain.permissions.Permissions;
import fi.mml.portti.service.db.permissions.PermissionsService;
import fi.nls.oskari.cache.Cache;
import fi.nls.oskari.cache.CacheManager;
import fi.nls.oskari.control.ActionDeniedException;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.data.domain.OskariLayerResource;
import fi.nls.oskari.map.layer.OskariLayerService;
import fi.nls.oskari.permission.domain.Resource;

/**
 * Created by SMAKINEN on 27.8.2015.
 */
public class PermissionHelper {

    private static final Logger LOG = LogFactory.getLogger(PermissionHelper.class);
    private static final String RESOURCE_CACHE_NAME = "permission_resources";
    private static final String LAYER_CACHE_NAME = "layer_resources";
    private final Cache<Resource> resourceCache = CacheManager.getCache(PermissionHelper.class.getName() + RESOURCE_CACHE_NAME);
    private final Cache<OskariLayer> layerCache = CacheManager.getCache(PermissionHelper.class.getName() + LAYER_CACHE_NAME);
    private OskariLayerService layerService;
    private PermissionsService permissionsService;

    public PermissionHelper(OskariLayerService layerService, PermissionsService permissionsService) {
        this.layerService = layerService;
        this.permissionsService = permissionsService;
    }

    /**
     * Returns the layer if user has permission for it. Otherwise throws an exception.
     * @param layerId
     * @param user
     * @return
     * @throws ActionException
     */
    public OskariLayer getLayer(final String layerId, final User user) throws ActionException {

        final OskariLayer layer = getLayer(layerId);
        if (layer == null) {
            throw new ActionParamsException("Layer not found for id: " + layerId);
        }

        // Check permissions
        final Resource resource = getResource(layer);
        final boolean hasPermission =
                resource.hasPermission(user, Permissions.PERMISSION_TYPE_VIEW_LAYER) ||
                        resource.hasPermission(user, Permissions.PERMISSION_TYPE_VIEW_PUBLISHED);

        if (!hasPermission) {
            throw new ActionDeniedException("User doesn't have permissions for requested layer");
        }
        return layer;
    }

    /**
     * Returns layer from cache
     * @param id Layer id
     * @return layer
     */
    private OskariLayer getLayer(final String id) {
        OskariLayer layer = layerCache.get(id);
        if (layer != null) {
            return layer;
        }
        layer = layerService.find(id);
        if (layer != null) {
            LOG.debug("Caching a layer with id ", id);
            layerCache.put(id, layer);
        }
        return layer;
    }

    /**
     * Gets resource from cache
     * @return resource
     */
    private Resource getResource(final OskariLayer layer) {

        final Resource layerResource = new OskariLayerResource(layer);
        Resource resource = resourceCache.get(layerResource.getMapping());
        if (resource != null) {
            return resource;
        }
        resource = permissionsService.findResource(layerResource);
        if (resource != null && !resource.getPermissions().isEmpty()) {
            LOG.debug("Caching a layer permission resource", resource, "Permissions", resource.getPermissions());
            resourceCache.put(layerResource.getMapping(),resource);
        } else {
            LOG.warn("Trying to cache layer with no resources");
        }
        return resource;
    }
}
