package org.oskari.maplayer.util;

import java.util.List;
import java.util.stream.Collectors;

import org.oskari.permissions.PermissionService;
import org.oskari.permissions.model.PermissionSet;
import org.oskari.permissions.model.PermissionType;
import org.oskari.permissions.model.Resource;
import org.oskari.permissions.model.ResourceType;
import org.oskari.user.User;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.layer.OskariLayerService;

public class OskariLayerUtil {

    private static final Logger LOG = LogFactory.getLogger(OskariLayerUtil.class);

    public static List<OskariLayer> getLayersForUser(
            OskariLayerService mapLayerService,
            PermissionService permissionService,
            User user, boolean isPublished) {
        long start = System.currentTimeMillis();
        List<OskariLayer> layers = mapLayerService.findAll();
        LOG.info("Layers read in", System.currentTimeMillis() - start, "ms");
        start = System.currentTimeMillis();
        List<Resource> resources = permissionService.findResourcesByUser(user, ResourceType.maplayer);
        LOG.info("Permissions read in", System.currentTimeMillis() - start, "ms");
        return filterLayersWithResources(layers, new PermissionSet(resources), user, isPublished);
    }

    public static List<OskariLayer> filterLayersWithResources(List<OskariLayer> layers, PermissionSet permissionSet,
            User user, boolean isPublished) {
        PermissionType forViewing = isPublished ? PermissionType.VIEW_PUBLISHED : PermissionType.VIEW_LAYER;
        return layers.stream()
                .filter(layer -> !layer.isInternal())
                .filter(layer -> layer.isSublayer() ||
                        permissionSet.get(ResourceType.maplayer, getPermissionKey(layer))
                                .map(r -> r.hasPermission(user, forViewing))
                                .orElse(false))
                .collect(Collectors.toList());
    }

    public static String getPermissionKey(OskariLayer layer) {
        return Integer.toString(layer.getId());
    }

}
