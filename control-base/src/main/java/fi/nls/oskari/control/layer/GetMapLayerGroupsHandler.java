package fi.nls.oskari.control.layer;

import static fi.nls.oskari.control.ActionConstants.PARAM_FORCE_PROXY;
import static fi.nls.oskari.control.ActionConstants.PARAM_LANGUAGE;
import static fi.nls.oskari.control.ActionConstants.PARAM_SRS;
import static fi.nls.oskari.control.ActionConstants.PARAM_ID;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import fi.nls.oskari.cache.Cache;
import fi.nls.oskari.cache.CacheManager;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.layer.DataProviderService;
import fi.nls.oskari.map.layer.OskariLayerService;
import fi.nls.oskari.map.layer.formatters.LayerJSONFormatter;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.JSONHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.oskari.service.maplayer.OskariMapLayerGroupService;
import fi.mml.map.mapwindow.util.OskariLayerWorker;
import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import org.oskari.user.User;
import fi.nls.oskari.domain.map.MaplayerGroup;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.map.layer.group.link.OskariLayerGroupLink;
import fi.nls.oskari.map.layer.group.link.OskariLayerGroupLinkService;
import fi.nls.oskari.map.layer.group.link.OskariLayerGroupLinkServiceMybatisImpl;
import fi.nls.oskari.util.EnvHelper;
import org.oskari.service.util.ServiceFactory;

/**
 * Get all map layer groups registered in Oskari database
 */
@OskariActionRoute("GetHierarchicalMapLayerGroups")
public class GetMapLayerGroupsHandler extends ActionHandler {

    public static final String CACHE_NAME = "LayerList";
    private static final String KEY_GROUPS = "groups";
    private static final String KEY_PROVIDERS = "providers";
    private static final String KEY_LAYERS = "layers";
    private static final String KEY_ID = "id";
    private static final String KEY_ORDER_NUMBER = "orderNumber";
    private static final Logger LOG = LogFactory.getLogger(GetMapLayerGroupsHandler.class);


    private static final List<String> PROXY_LYR_TYPES = Arrays.asList(
            OskariLayer.TYPE_WMS,
            OskariLayer.TYPE_WMTS,
            OskariLayer.TYPE_ARCGIS93,
            OskariLayer.TYPE_VECTOR_TILE);

    private static final Cache<String> cache = CacheManager.getCache(CACHE_NAME);

    private OskariLayerService layerService;
    private OskariMapLayerGroupService groupService;
    private OskariLayerGroupLinkService linkService;
    private DataProviderService dataProviderService;

    public void setLayerService(OskariLayerService service) {
        this.layerService = service;
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
        if (layerService == null) {
            setLayerService(OskariComponentManager.getComponentOfType(OskariLayerService.class));
        }
        if (groupService == null) {
            setGroupService(ServiceFactory.getOskariMapLayerGroupService());
        }
        if (linkService == null) {
            setLinkService(new OskariLayerGroupLinkServiceMybatisImpl());
        }
        if (dataProviderService == null) {
            setDataProviderService(OskariComponentManager.getComponentOfType(DataProviderService.class));
        }
    }

    @Override
    public void handleAction(ActionParameters params) throws ActionException {
        final User user = params.getUser();
        final String lang = params.getHttpParam(PARAM_LANGUAGE, params.getLocale().getLanguage());
        final String crs = params.getHttpParam(PARAM_SRS);
        final String requestedIds = params.getHttpParam(PARAM_ID);
        final boolean forceProxy = params.getHttpParam(PARAM_FORCE_PROXY, false);
        String response = null;
        String cacheKey = getCacheKey(user, lang, crs, forceProxy);
        if (requestedIds == null) {
            // only use cache when the whole listing is requested
            // Note! Cache needs to be flushed externally on other routes when permissions/layers are changed
            response = cache.get(cacheKey);
        }
        if (response == null) {
            // NOTE, isSecure doesn't change in instances without restarting it so it's safe to skip on the cache key
            response = getList(user, lang, crs, requestedIds, forceProxy, EnvHelper.isSecure(params));
            if (requestedIds == null) {
                // only use cache when the whole listing is requested
                cache.put(cacheKey, response);
            }
        }
        try {
            params.getResponse().setCharacterEncoding("UTF-8");
            params.getResponse().setContentType("application/json;charset=UTF-8");
            params.getResponse().getWriter().print(response);
        } catch (IOException e) {
            LOG.info("Couldn't write answer:", e.getMessage());
            LOG.debug(e);
        }
    }

    public String getList(User user, String lang, String crs, String requestedIds, boolean forceProxy, boolean isSecure) throws ActionException {
        final boolean isPublished = false;

        Map<Integer, List<MaplayerGroup>> groupsByParentId = groupService.findAll().stream()
                .collect(Collectors.groupingBy(MaplayerGroup::getParentId));

        Map<Integer, List<OskariLayerGroupLink>> linksByGroupId = linkService.findAll().stream()
                .collect(Collectors.groupingBy(OskariLayerGroupLink::getGroupId));

        // Get all layers instead of using OskariLayerWorker.getLayersForUser() so we don't check permissions twice
        List<OskariLayer> layers = getLayers(requestedIds);
        if (forceProxy) {
            layers.stream()
                    .filter(layer -> PROXY_LYR_TYPES.contains(layer.getType()))
                    .forEach(layer -> layer.addAttribute("forceProxy", true));
        }

        int[] sortedLayerIds = layers.stream().mapToInt(OskariLayer::getId).toArray();
        Arrays.sort(sortedLayerIds);
        try {
            // getListOfMapLayers checks permissions
            JSONObject response = OskariLayerWorker.getListOfMapLayers(layers, user, lang, crs, isPublished, isSecure);
            response.put(KEY_GROUPS, getGroupJSON(groupsByParentId, linksByGroupId, sortedLayerIds, -1, lang));
            response.put(KEY_PROVIDERS, getProvidersJSON(lang, getProviderIds(response, user)));
            return response.toString();
        } catch (JSONException e) {
            throw new ActionException("Failed to add groups", e);
        }
    }

    private List<OskariLayer> getLayers(String requestedLayers) {
        if (requestedLayers == null || requestedLayers.isEmpty()) {
            // nothing requested/default -> return all
            return layerService.findAll();
        }
        // partial list requested
        List<Integer> idList = new ArrayList<>();
        for (String str: requestedLayers.split(",")) {
            int id = ConversionHelper.getInt(str, -1);
            if (id != -1) {
                idList.add(id);
            }
        }
        return layerService.findByIdList(idList);
    }

    /**
     * Get groups recursively
     */
    private JSONArray getGroupJSON(final Map<Integer, List<MaplayerGroup>> groupsByParentId,
            final Map<Integer, List<OskariLayerGroupLink>> linksByGroupId,
            final int[] sortedLayerIds,
            final int parentGroupId,
            final String language) throws JSONException {
        List<MaplayerGroup> groups = groupsByParentId.get(parentGroupId);
        if (groups == null || groups.isEmpty()) {
            return null;
        }

        JSONArray json = new JSONArray();
        groups.sort(Comparator.comparing(MaplayerGroup::getOrderNumber));
        for (MaplayerGroup group : groups) {
            int groupId = group.getId();
            JSONObject groupAsJson = group.getAsJSON(language);

            JSONArray subGroups = getGroupJSON(groupsByParentId, linksByGroupId, sortedLayerIds, groupId, language);
            if (subGroups != null) {
                groupAsJson.put(KEY_GROUPS, subGroups);
            }

            List<OskariLayerGroupLink> groupLinks = linksByGroupId.get(groupId);
            if (groupLinks != null && !groupLinks.isEmpty()) {

                List<OskariLayerGroupLink> groupLayers = groupLinks.stream()
                        .filter(l -> contains(sortedLayerIds, l.getLayerId()))
                        .sorted(Comparator.comparingInt(OskariLayerGroupLink::getOrderNumber))
                        .collect(Collectors.toList());
                if (!groupLayers.isEmpty()) {
                    groupAsJson.put(KEY_LAYERS, getLayersJSON(groupLayers));
                }
            }

            json.put(groupAsJson);
        }
        return json;
    }

    /**
     * Constructs a set of dataprovider ids that are used in the layers that will be returned to the user.
     * FIXME: This is terrible (looping through layers as json). We need to refactor how we write this stuff for the frontend...
     * @param response
     * @return
     */
    private Set<Integer> getProviderIds(JSONObject response, User user) {
        if (user != null && user.isAdmin()) {
            // don't filter providers based on layers, given them all for admin so editing works
            return null;
        }
        Set<Integer> providerIds = new HashSet<>();
        JSONArray layers = response.optJSONArray(OskariLayerWorker.KEY_LAYERS);
        for (int i = 0; i < layers.length(); i++) {
            providerIds.add(layers.optJSONObject(i).optInt(LayerJSONFormatter.KEY_DATA_PROVIDER_ID, -1));
        }
        return providerIds;
    }
    /**
     * Constructs an object that only has provider mapping for ids included in usedProviders parameter.
     * @param language
     * @param usedProviders
     * @return
     */
    private JSONObject getProvidersJSON(String language, Set<Integer> usedProviders) {
        JSONObject result = new JSONObject();
        dataProviderService.findAll().stream()
                .forEach(provider -> {
                    int id = provider.getId();
                    if (usedProviders != null && !usedProviders.contains(id)) {
                        // if provider filter is null the list is not filtered (==admin user)
                        return;
                    }
                    JSONObject json = new JSONObject();
                    JSONHelper.putValue(json, "name", provider.getName(language));
                    JSONHelper.putValue(json, "desc", provider.getDescription(language));
                    JSONHelper.putValue(result, Integer.toString(id), json);
                });
        return result;
    }

    private JSONArray getLayersJSON(List<OskariLayerGroupLink> groupLayers) throws JSONException {
        JSONArray groupLayersJSON = new JSONArray();

        for(OskariLayerGroupLink groupLayer: groupLayers) {
            JSONObject groupLayerJSON = new JSONObject();
            groupLayerJSON.put(KEY_ID, groupLayer.getLayerId());
            groupLayerJSON.put(KEY_ORDER_NUMBER, groupLayer.getOrderNumber());
            groupLayersJSON.put(groupLayerJSON);
        }

        return groupLayersJSON;
    }

    private boolean contains(int[] sortedLayerIds, int layerId) {
        return Arrays.binarySearch(sortedLayerIds, layerId) >= 0;
    }

    private String getCacheKey(User user, String lang, String crs, boolean forceProxy) {
        return "layers_"
                + lang + "_"
                + crs + "_"
                + forceProxy + "_"
                + getUserRolesKey(user);
    }

    private String getUserRolesKey(User user) {
        return user.getRoles().stream()
                .map(r -> Long.toString(r.getId()))
                .sorted()
                .collect(Collectors.joining("_"));
    }

}
