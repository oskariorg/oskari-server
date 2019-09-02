package fi.nls.oskari.control.layer;

import org.oskari.permissions.PermissionService;
import org.oskari.permissions.model.OskariLayerResource;
import org.oskari.permissions.model.PermissionType;
import org.oskari.permissions.model.Resource;

import fi.nls.oskari.cache.Cache;
import fi.nls.oskari.cache.CacheManager;
import fi.nls.oskari.control.ActionDeniedException;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.layer.OskariLayerService;
import org.oskari.permissions.model.ResourceType;

import java.util.Optional;

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
    private PermissionService permissionsService;

    public PermissionHelper(OskariLayerService layerService, PermissionService permissionsService) {
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
    public OskariLayer getLayer(final int layerId, final User user) throws ActionException {

        final OskariLayer layer = getLayer(layerId);
        if (layer == null) {
            throw new ActionParamsException("Layer not found for id: " + layerId);
        }
        if(layer.isInternal()) {
            // myplaces etc don't have resources
            return layer;
        }

        // Check permissions
        final Resource resource = getResource(layer);
        final boolean hasPermission =
                resource.hasPermission(user, PermissionType.VIEW_LAYER) ||
                        resource.hasPermission(user, PermissionType.VIEW_PUBLISHED);

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
    private OskariLayer getLayer(final int id) {
        String cacheKey = Integer.toString(id);
        OskariLayer layer = layerCache.get(cacheKey);
        if (layer != null) {
            return layer;
        }
        layer = layerService.find(id);
        if (layer != null) {
            LOG.debug("Caching a layer with id ", id);
            layerCache.put(cacheKey, layer);
        }
        return layer;
    }

    /**
     * Gets resource from cache
     * @return resource
     */
    private Resource getResource(final OskariLayer layer) {

        String mapping = Integer.toString(layer.getId());
        Resource resource = resourceCache.get(mapping);
        if (resource != null) {
            return resource;
        }
        Optional<Resource> dbRes = permissionsService.findResource(ResourceType.maplayer, mapping);
        if (!dbRes.isPresent()) {
            LOG.warn("Permissions not found for layer:", layer.getId());
        } else {
            resource = dbRes.get();
            LOG.debug("Caching a layer permission resource", resource, "Permissions", resource.getPermissions());
            resourceCache.put(mapping, resource);
        }

        return resource;
    }
}
