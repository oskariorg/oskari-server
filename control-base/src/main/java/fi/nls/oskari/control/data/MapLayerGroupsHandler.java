package fi.nls.oskari.control.data;

import static fi.nls.oskari.control.ActionConstants.KEY_NAME;
import static fi.nls.oskari.control.ActionConstants.PARAM_ID;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;
import org.oskari.log.AuditLog;
import org.oskari.service.util.ServiceFactory;

import org.oskari.service.maplayer.OskariMapLayerGroupService;
import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.control.RestActionHandler;
import fi.nls.oskari.domain.map.MaplayerGroup;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.map.layer.OskariLayerService;
import fi.nls.oskari.map.layer.group.link.OskariLayerGroupLinkService;
import fi.nls.oskari.map.layer.group.link.OskariLayerGroupLinkServiceMybatisImpl;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.util.ResponseHelper;
import fi.nls.oskari.cache.CacheManager;
import fi.nls.oskari.control.layer.GetMapLayerGroupsHandler;

/**
 * CRUD for Maplayer groups. Get is callable by anyone, other methods require
 * admin user.
 */
@OskariActionRoute("MapLayerGroups")
public class MapLayerGroupsHandler extends RestActionHandler {

	private static final String KEY_LOCALES = "locales";
	private static final String KEY_PARENT_ID = "parentId";
	private static final String KEY_SELECTABLE = "selectable";
	private static final String KEY_ORDER = "orderNumber";
	private static final String PARAM_DELETE_LAYERS = "deleteLayers";
	private static final int PARAM_LAYERLIST_HIERARCHY_MAXDEPTH = PropertyUtil.getOptional("layerlist.hierarchy.maxdepth", 2);

	private OskariMapLayerGroupService oskariMapLayerGroupService;
	private OskariLayerGroupLinkService linkService;
	private OskariLayerService mapLayerService = ServiceFactory.getMapLayerService();

	public void setOskariMapLayerGroupService(final OskariMapLayerGroupService service) {
		oskariMapLayerGroupService = service;
	}

	public void setLinkService(OskariLayerGroupLinkService linkService) {
		this.linkService = linkService;
	}

	public void init() {
		// setup service if it hasn't been initialized
		if (oskariMapLayerGroupService == null) {
			setOskariMapLayerGroupService(ServiceFactory.getOskariMapLayerGroupService());
		}
		if (linkService == null) {
			setLinkService(new OskariLayerGroupLinkServiceMybatisImpl());
		}
	}

	private void flushLayerListCache() {
        CacheManager.getCache(GetMapLayerGroupsHandler.CACHE_NAME).flush(true);
    }

	/**
	 * Handles listing and single maplayer group find
	 * 
	 * @param params
	 * @throws ActionException
	 */
	public void handleGet(ActionParameters params) throws ActionException {
		final int id = params.getHttpParam(PARAM_ID, -1);
		if (id != -1) {
			// find single group
			ResponseHelper.writeResponse(params, getById(id).getAsJSON());
			return;
		}

		// find all maplayerGroups
		final List<MaplayerGroup> maplayerGroups = oskariMapLayerGroupService.findAll();
		final JSONArray list = new JSONArray();
		for (MaplayerGroup maplayerGroup : maplayerGroups) {
			list.put(maplayerGroup.getAsJSON());
		}
		final JSONObject result = new JSONObject();
		JSONHelper.putValue(result, "mapLayerGroups", list);
		ResponseHelper.writeResponse(params, result);
	}

	private MaplayerGroup getById(int id) throws ActionParamsException {
		MaplayerGroup d = oskariMapLayerGroupService.find(id);
		if (d == null) {
			throw new ActionParamsException("No group for id=" + id);
		}
		return d;
	}
	/**
	 * Handles update
	 * 
	 * @param params
	 * @throws ActionException
	 */
	public void handlePut(ActionParameters params) throws ActionException {
		int id = params.getRequiredParamInt(PARAM_ID);
		params.requireAdminUser();
		MaplayerGroup group = getById(id);

		populateFromRequest(group, params.getPayLoadJSON());

		// Check at group depth is maximum 3
		if (!isAllowedGroupDepth(group)) {
			throw new ActionParamsException("Maximum subgroup depth is " + PARAM_LAYERLIST_HIERARCHY_MAXDEPTH);
		}
		oskariMapLayerGroupService.update(group);
		AuditLog.user(params.getClientIp(), params.getUser()).withParam("id", group.getId())
				.withParam("name", group.getName(PropertyUtil.getDefaultLanguage()))
				.updated(AuditLog.ResourceType.MAPLAYER_GROUP);

		flushLayerListCache();
		ResponseHelper.writeResponse(params, getById(id).getAsJSON());
	}

	/**
	 * Handles insert
	 * 
	 * @param params
	 * @throws ActionException
	 */
	public void handlePost(ActionParameters params) throws ActionException {
		params.requireAdminUser();
		MaplayerGroup group = new MaplayerGroup();
		populateFromRequest(group, params.getPayLoadJSON());

		final int id = oskariMapLayerGroupService.insert(group);
		AuditLog.user(params.getClientIp(), params.getUser()).withParam("id", id)
				.withParam("name", group.getName(PropertyUtil.getDefaultLanguage()))
				.added(AuditLog.ResourceType.MAPLAYER_GROUP);

		flushLayerListCache();

		ResponseHelper.writeResponse(params, getById(id).getAsJSON());
	}

	/**
	 * Handles removal
	 * 
	 * @param params
	 * @throws ActionException
	 */
	public void handleDelete(ActionParameters params) throws ActionException {
		params.requireAdminUser();
		final int groupId = params.getRequiredParamInt(PARAM_ID);
		final Object deleteLayers = params.getHttpParam(PARAM_DELETE_LAYERS);

		final MaplayerGroup maplayerGroup = getById(groupId);

		if (deleteLayers != null) {
			handleDelete(params, maplayerGroup, deleteLayers);
		} else {
			handleDeleteLegacy(params, maplayerGroup);
		}
	}

	/**
	 * Delete handling used with new layer admin since version 1.55.0. Removes map
	 * layers linked to the group from system when deleteLayers parameter is true.
	 * With value false, layers remain in the system but links to the group are
	 * removed.
	 * 
	 * @param params
	 * @param maplayerGroup
	 * @param deleteLayersObj
	 * @throws ActionParamsException
	 */
	private void handleDelete(ActionParameters params, MaplayerGroup maplayerGroup, Object deleteLayersObj)
			throws ActionParamsException {
		boolean deleteLayers;
		try {
			deleteLayers = Boolean.parseBoolean((String) deleteLayersObj);
		} catch (Exception e) {
			throw new ActionParamsException(
					"DeleteLayers parameter " + deleteLayersObj + " could not be parsed to boolean!");
		}

		List<OskariLayer> layers = mapLayerService.findByGroupId(maplayerGroup.getId());

		List<String> layerNamesToBeDeleted;

		if (deleteLayers) {
			layers.forEach(layer -> mapLayerService.delete(layer.getId()));
			layerNamesToBeDeleted = layers.stream().map(OskariLayer::getName).collect(Collectors.toList());
		} else {
			linkService.deleteLinksByGroupId(maplayerGroup.getId());
			layerNamesToBeDeleted = new ArrayList<String>();
		}

		oskariMapLayerGroupService.delete(maplayerGroup);
		flushLayerListCache();
		AuditLog.user(params.getClientIp(), params.getUser()).withParam("id", maplayerGroup.getId())
				.withParam("name", maplayerGroup.getName(PropertyUtil.getDefaultLanguage()))
				.withMsg("map layers " + layerNamesToBeDeleted + " deleted with map layer group")
				.deleted(AuditLog.ResourceType.MAPLAYER_GROUP);

		ResponseHelper.writeResponse(params, maplayerGroup.getAsJSON());
	}

	/**
	 * Delete handling used with old layer admin prior version 1.55.0. Throws
	 * exception if the map layer group has linked map layers.
	 * 
	 * @param params
	 * @param maplayerGroup
	 * @throws ActionParamsException
	 */
	private void handleDeleteLegacy(ActionParameters params, MaplayerGroup maplayerGroup) throws ActionParamsException {
		if (linkService.hasLinks(maplayerGroup.getId())) {
			// maplayer group with maplayers under it can't be removed
			throw new ActionParamsException("Maplayers linked to maplayer group",
					JSONHelper.createJSONObject("code", "not_empty"));
		}
		oskariMapLayerGroupService.delete(maplayerGroup);
		flushLayerListCache();
		AuditLog.user(params.getClientIp(), params.getUser()).withParam("id", maplayerGroup.getId())
				.withParam("name", maplayerGroup.getName(PropertyUtil.getDefaultLanguage()))
				.deleted(AuditLog.ResourceType.MAPLAYER_GROUP);

		ResponseHelper.writeResponse(params, maplayerGroup.getAsJSON());
	}

	/**
	 * Calculate group depth
	 * 
	 * @param groupId group id
	 * @param depth   current depth
	 * @return group depth
	 */
	private int getGroupDepth(int groupId, int depth) {
		depth++;
		MaplayerGroup maplayerGroup = oskariMapLayerGroupService.find(groupId);
		if (maplayerGroup.getParentId() == -1) {
			return depth;
		}
		return getGroupDepth(maplayerGroup.getParentId(), depth);
	}

	/**
	 * Has allowed group depth
	 * 
	 * @param maplayerGroup maplayer group
	 * @return is allowed group depth
	 */
	private boolean isAllowedGroupDepth(MaplayerGroup maplayerGroup) {
		if (maplayerGroup.getParentId() != -1 && getGroupDepth(maplayerGroup.getParentId(), 0) > PARAM_LAYERLIST_HIERARCHY_MAXDEPTH) {
			return false;
		}
		return true;
	}

	private void populateFromRequest(MaplayerGroup group, JSONObject payload) throws ActionException {
		if (payload == null) {
			throw new ActionParamsException("No payload for group");
		}
		JSONObject locales = payload.optJSONObject(KEY_LOCALES);
		validateLocales(locales);
		group.setLocale(locales);
		group.setParentId(payload.optInt(KEY_PARENT_ID, group.getParentId()));
		group.setSelectable(payload.optBoolean(KEY_SELECTABLE, group.isSelectable()));
		group.setOrderNumber(payload.optInt(KEY_ORDER, group.getOrderNumber()));
	}

	private void validateLocales(JSONObject locales) throws ActionParamsException {
		if (locales == null) {
			throw new ActionParamsException("No locales for group");
		}
		JSONObject defaultLang = locales.optJSONObject(PropertyUtil.getDefaultLanguage());
		if (defaultLang == null) {
			throw new ActionParamsException("No locale for default lang: " + PropertyUtil.getDefaultLanguage());
		}
		String name = defaultLang.optString(KEY_NAME);
		if (name == null || name.trim().isEmpty()) {
			throw new ActionParamsException("No name for default lang: " + PropertyUtil.getDefaultLanguage());
		}
	}
}
