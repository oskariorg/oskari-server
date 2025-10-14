package org.oskari.control.layer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.json.JSONObject;
import org.oskari.capabilities.MetadataHelper;
import org.oskari.capabilities.ogc.LayerCapabilitiesOGC;
import org.oskari.control.layer.model.DataProviderOutput;
import org.oskari.control.layer.model.LayerGroupOutput;
import org.oskari.control.layer.model.LayerLinkOutput;
import org.oskari.control.layer.model.LayerListResponse;
import org.oskari.control.layer.model.LayerOutput;
import org.oskari.map.myfeatures.service.MyFeaturesService;
import org.oskari.maplayer.util.OskariLayerUtil;
import org.oskari.permissions.PermissionService;
import org.oskari.service.maplayer.OskariMapLayerGroupService;
import org.oskari.service.util.ServiceFactory;
import org.oskari.user.User;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.RestActionHandler;
import fi.nls.oskari.domain.map.DataProvider;
import fi.nls.oskari.domain.map.JSONLocalized;
import fi.nls.oskari.domain.map.MaplayerGroup;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.domain.map.myfeatures.MyFeaturesLayer;
import fi.nls.oskari.map.layer.DataProviderService;
import fi.nls.oskari.map.layer.OskariLayerService;
import fi.nls.oskari.map.layer.group.link.OskariLayerGroupLink;
import fi.nls.oskari.map.layer.group.link.OskariLayerGroupLinkService;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.service.capabilities.CapabilitiesConstants;
import fi.nls.oskari.util.ResponseHelper;

@OskariActionRoute("LayerList")
public class LayerListHandler extends RestActionHandler {

    private OskariLayerService mapLayerService;
    private PermissionService permissionService;
    private OskariMapLayerGroupService groupService;
    private OskariLayerGroupLinkService linkService;
    private DataProviderService dataProviderService;
    private MyFeaturesService myFeaturesService;

    public void setMapLayerService(OskariLayerService mapLayerService) {
        this.mapLayerService = mapLayerService;
    }

    public void setPermissionService(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    public void setGroupService(OskariMapLayerGroupService groupService) {
        this.groupService = groupService;
    }

    public void setLinkService(OskariLayerGroupLinkService linkService) {
        this.linkService = linkService;
    }

    public void setDataProviderService(DataProviderService service) {
        this.dataProviderService = service;
    }

    public void setMyFeaturesService(MyFeaturesService myFeaturesService) {
        this.myFeaturesService = myFeaturesService;
    }

    @Override
    public void init() {
        // setup services if they haven't been initialized
        if (mapLayerService == null) {
            setMapLayerService(ServiceFactory.getMapLayerService());
        }
        if (permissionService == null) {
            setPermissionService(ServiceFactory.getPermissionsService());
        }
        if (groupService == null) {
            setGroupService(ServiceFactory.getOskariMapLayerGroupService());
        }
        if (linkService == null) {
            setLinkService(ServiceFactory.getOskariLayerGroupLinkService());
        }
        if (dataProviderService == null) {
            setDataProviderService(ServiceFactory.getDataProviderService());
        }
        if (myFeaturesService == null) {
            setMyFeaturesService(OskariComponentManager.getComponentOfType(MyFeaturesService.class));
        }
    }

    @Override
    public void handleAction(ActionParameters params) throws ActionException {
        User user = params.getUser();
        String language = params.getLocale().getLanguage();
        LayerListResponse response = getLayerList(user, language);
        ResponseHelper.writeJsonResponse(params, response);
    }

    protected LayerListResponse getLayerList(User user, String language) {
        List<OskariLayer> mapLayers = getLayers(user);
        List<MyFeaturesLayer> myFeaturesLayers = myFeaturesService.getLayersByOwnerUuid(user.getUuid());

        List<LayerOutput> layers = Stream.concat(
                mapLayers.stream().map(l -> mapLayer(l, language)),
                myFeaturesLayers.stream().map(l -> mapMyFeaturesLayer(l, language))).collect(Collectors.toList());

        LayerListResponse response = new LayerListResponse();
        response.layers = layers;
        response.groups = getLayerGroups(mapLayers, language, user.isAdmin());
        response.providers = getProviders(mapLayers, language, user.isAdmin());

        return response;
    }

    private List<OskariLayer> getLayers(User user) {
        return OskariLayerUtil.getLayersForUser(mapLayerService, permissionService, user, false);
    }

    private static LayerOutput mapLayer(OskariLayer layer, String language) {
        LayerOutput out = new LayerOutput();
        out.id = Integer.toString(layer.getId());
        out.type = layer.getType();
        out.name = layer.getName(language);
        out.metadataUuid = getMetadataUuid(layer);
        out.dataproviderId = layer.getDataproviderId();
        out.created = layer.getCreated();
        out.updated = layer.getUpdated();
        return out;
    }

    private static LayerOutput mapMyFeaturesLayer(MyFeaturesLayer layer, String language) {
        LayerOutput out = new LayerOutput();
        out.id = layer.getId().toString();
        out.type = layer.getType();
        out.name = layer.getName(language);
        out.metadataUuid = null;
        out.dataproviderId = null;
        out.created = layer.getCreated() != null ? new Date(layer.getCreated().toEpochMilli()) : null;
        out.updated = layer.getUpdated() != null ? new Date(layer.getUpdated().toEpochMilli()) : null;
        return out;
    }

    private static String getMetadataUuid(OskariLayer layer) {
        String fixed = MetadataHelper.getIdFromMetadataUrl(layer.getMetadataId());
        if (fixed != null) {
            return fixed;
        }
        String olderMetadataCaps = layer.getCapabilities().optString(CapabilitiesConstants.KEY_METADATA, null);
        if (olderMetadataCaps != null && !olderMetadataCaps.trim().isEmpty()) {
            return olderMetadataCaps;
        }
        return layer.getCapabilities().optString(LayerCapabilitiesOGC.METADATA_UUID, null);
    }

    private List<LayerGroupOutput> getLayerGroups(List<OskariLayer> layers, String language, boolean isAdmin) {
        Map<Integer, List<OskariLayerGroupLink>> linksByGroupId = linkService.findAll().stream()
                .collect(Collectors.groupingBy(OskariLayerGroupLink::getGroupId));

        Predicate<OskariLayerGroupLink> linkFilter = __ -> true;
        if (!isAdmin) {
            Set<Integer> layerIds = layers.stream().map(OskariLayer::getId).collect(Collectors.toSet());
            linkFilter = link -> layerIds.contains(link.getLayerId());
        }

        List<MaplayerGroup> groups = groupService.findAll();

        List<LayerGroupOutput> out = new ArrayList<>();

        // Add groups that contain layers
        for (MaplayerGroup g : groups) {
            List<OskariLayerGroupLink> links = linksByGroupId.getOrDefault(g.getId(), Collections.emptyList());
            links = links
                    .stream()
                    .filter(linkFilter)
                    .collect(Collectors.toList());
            // if admin then always include the group in result, otherwise only if layer is
            // used
            if (isAdmin || !links.isEmpty()) {
                LayerGroupOutput gOut = toLayerGroupOutput(g, links, language);
                out.add(gOut);
            }
        }

        // Optimization: No need to do the next part if admin as all groups were already
        // included
        if (isAdmin) {
            return out;
        }

        final int NULL_PARENT_ID = -1;

        // Add groups that didn't contain layers directly, but are parents of groups
        // that got selected earlier
        Set<Integer> selectedGroupIds = out.stream().map(x -> x.id).collect(Collectors.toSet());
        Set<Integer> missingGroupIds = out.stream()
                .filter(x -> x.parentId != NULL_PARENT_ID && !selectedGroupIds.contains(x.parentId))
                .map(x -> x.parentId)
                .collect(Collectors.toSet());

        // Recursively process one level of groups at a time
        // Example: Group A -> Group B -> Group C
        // Group A and B do not have any layers themselves, so they aren't part of `out`
        // (yet)
        // Group C is included in `out`
        // First iteration: Parent of Group C is missing -> Add Group B
        // Second iteration: Parent of group B is missing -> Add Group A
        // Third iteration: Nothing is missing -> Time to stop
        while (!missingGroupIds.isEmpty()) {
            Set<Integer> newMissingGroupIds = new HashSet<>();
            for (MaplayerGroup g : groups) {
                if (!missingGroupIds.remove(g.getId())) {
                    continue;
                }
                LayerGroupOutput gOut = toLayerGroupOutput(g, Collections.emptyList(), language);
                out.add(gOut);
                selectedGroupIds.add(g.getId());
                if (g.getParentId() != NULL_PARENT_ID && !selectedGroupIds.contains(g.getParentId())) {
                    newMissingGroupIds.add(g.getParentId());
                }
            }
            missingGroupIds.addAll(newMissingGroupIds);
        }

        return out;
    }

    private static LayerGroupOutput toLayerGroupOutput(MaplayerGroup g, List<OskariLayerGroupLink> layers,
            String language) {
        JSONObject gg = g.getAsJSON(language);
        LayerGroupOutput out = new LayerGroupOutput();
        out.id = g.getId();
        out.orderNumber = g.getOrderNumber();
        out.parentId = g.getParentId();
        out.selectable = g.isSelectable();
        out.name = gg.optString(JSONLocalized.LOCALE_NAME);
        out.desc = gg.optString(JSONLocalized.LOCALE_DESCRIPTION);
        out.layers = layers.stream().map(LayerListHandler::toLayerLinkOutput).collect(Collectors.toList());
        return out;
    }

    private static LayerLinkOutput toLayerLinkOutput(OskariLayerGroupLink link) {
        LayerLinkOutput out = new LayerLinkOutput();
        out.id = link.getLayerId();
        out.orderNumber = link.getOrderNumber();
        return out;
    }

    private Map<Integer, DataProviderOutput> getProviders(List<OskariLayer> layers, String language, boolean isAdmin) {
        Predicate<DataProvider> filterFn = __ -> true;
        if (!isAdmin) {
            // For non-admins return only the providers that are referenced by some layer
            // we're about to return
            Set<Integer> providerIds = layers.stream().map(OskariLayer::getDataproviderId).collect(Collectors.toSet());
            filterFn = p -> providerIds.contains(p.getId());
        }
        return dataProviderService.findAll().stream()
                .filter(filterFn)
                .map(p -> toDataProviderOutput(p, language))
                .collect(Collectors.toMap(x -> x.id, x -> x));
    }

    private static DataProviderOutput toDataProviderOutput(DataProvider d, String language) {
        DataProviderOutput out = new DataProviderOutput();
        out.id = d.getId();
        out.name = d.getName(language);
        out.desc = d.getDescription(language);
        return out;
    }

}
