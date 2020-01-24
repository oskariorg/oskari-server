package org.oskari.admin;

import fi.mml.map.mapwindow.service.db.OskariMapLayerGroupService;
import fi.nls.oskari.domain.map.MaplayerGroup;
import fi.nls.oskari.map.layer.group.link.OskariLayerGroupLink;
import fi.nls.oskari.map.layer.group.link.OskariLayerGroupLinkService;
import fi.nls.oskari.service.OskariComponentManager;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class MapLayerGroupsHelper {

    public static List<MaplayerGroup> findGroups(Set<String> groups) {
        if (groups == null) {
            return Collections.emptyList();
        }
        OskariMapLayerGroupService service = getLayerGroupService();
        return groups.stream()
                .map(name -> service.findByName(name))
                .filter(g -> g != null)
                .collect(Collectors.toList());
    }

    public static void setGroupsForLayer(int layerId, List<Integer> groups) {
        OskariLayerGroupLinkService service = getLayerGroupLinkService();
        // remove existing groups
        service.deleteLinksByLayerId(layerId);

        if (groups == null) {
            return;
        }
        // insert new ones
        List<OskariLayerGroupLink> links = groups.stream()
                .map(groupId -> new OskariLayerGroupLink(layerId, groupId))
                .collect(Collectors.toList());
        service.insertAll(links);
    }

    private static OskariLayerGroupLinkService getLayerGroupLinkService() {
        return OskariComponentManager.getComponentOfType(OskariLayerGroupLinkService.class);
    }
    private static OskariMapLayerGroupService getLayerGroupService() {
        return OskariComponentManager.getComponentOfType(OskariMapLayerGroupService.class);
    }
}
