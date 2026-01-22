package org.oskari.control.layer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.json.JSONObject;
import org.oskari.control.layer.model.DataProviderOutput;
import org.oskari.control.layer.model.LayerGroupOutput;
import org.oskari.control.layer.model.LayerLinkOutput;
import org.oskari.control.layer.model.LayerListResponse;
import org.oskari.domain.map.LayerOutput;
import org.oskari.service.maplayer.LayerProvider;
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
import fi.nls.oskari.map.layer.DataProviderService;
import fi.nls.oskari.map.layer.group.link.OskariLayerGroupLink;
import fi.nls.oskari.map.layer.group.link.OskariLayerGroupLinkService;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.util.ResponseHelper;

@OskariActionRoute("LayerList")
public class LayerListHandler extends RestActionHandler {

    private Collection<LayerProvider> layerProviders;
    private OskariMapLayerGroupService groupService;
    private OskariLayerGroupLinkService linkService;
    private DataProviderService dataProviderService;

    public void setLayerProviders(Collection<LayerProvider> layerProviders) {
        this.layerProviders = layerProviders;
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

    @Override
    public void init() {
        // setup services if they haven't been initialized
        if (layerProviders == null) {
            Map<String, LayerProvider> components = OskariComponentManager.getComponentsOfType(LayerProvider.class);
            setLayerProviders(components.values());
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
    }

    @Override
    public void handleAction(ActionParameters params) throws ActionException {
        User user = params.getUser();
        String language = params.getLocale().getLanguage();
        LayerListResponse response = getLayerList(user, language);
        ResponseHelper.writeJsonResponse(params, response);
    }

    protected LayerListResponse getLayerList(User user, String language) {
        List<LayerOutput> layers = getLayers(user, language);

        LayerListResponse response = new LayerListResponse();
        response.layers = layers;
        response.groups = getLayerGroups(layers, language, user.isAdmin());
        response.providers = getProviders(layers, language, user.isAdmin());

        return response;
    }

    private List<LayerOutput> getLayers(User user, String language) {
        return layerProviders.stream()
            .map(service -> service.listLayers(user, language))
            .flatMap(List::stream)
            .collect(Collectors.toList());
    }

    private List<LayerGroupOutput> getLayerGroups(List<LayerOutput> layers, String language, boolean isAdmin) {
        Map<Integer, List<OskariLayerGroupLink>> linksByGroupId = linkService.findAll().stream()
                .collect(Collectors.groupingBy(OskariLayerGroupLink::getGroupId));

        Predicate<OskariLayerGroupLink> linkFilter = __ -> true;
        if (!isAdmin) {
            Set<Integer> layerIds = layers.stream()
                .filter(l -> isInteger(l.id))
                .map(l -> Integer.parseInt(l.id))
                .collect(Collectors.toSet());
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

    private static boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (Exception e) {
            return false;
        }
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

    private Map<Integer, DataProviderOutput> getProviders(List<LayerOutput> layers, String language, boolean isAdmin) {
        Predicate<DataProvider> filterFn = __ -> true;
        if (!isAdmin) {
            // For non-admins return only the providers that are referenced by some layer
            // we're about to return
            Set<Integer> providerIds = layers.stream()
                .filter(l -> l.dataproviderId != null)
                .map(l -> l.dataproviderId)
                .collect(Collectors.toSet());
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
