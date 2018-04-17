package fi.nls.oskari.control.data;

import fi.mml.map.mapwindow.service.db.OskariMapLayerGroupService;
import fi.mml.map.mapwindow.service.db.OskariMapLayerGroupServiceIbatisImpl;
import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.*;
import fi.nls.oskari.domain.map.MaplayerGroup;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.ResponseHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.List;

import static fi.nls.oskari.control.ActionConstants.PARAM_ID;


/**
 * CRUD for Maplayer groups. Get is callable by anyone, other methods require admin user.
 */
@OskariActionRoute("MapLayerGroups")
public class MapLayerGroupsHandler extends RestActionHandler {
    private static Logger log = LogFactory.getLogger(MapLayerGroupsHandler.class);

    private static final String KEY_LOCALES = "locales";
    private static final String KEY_PARENT_ID = "parentId";
    private static final String KEY_SELECTABLE = "selectable";

    private OskariMapLayerGroupService oskariMapLayerGroupService;

    public void setOskariMapLayerGroupService(final OskariMapLayerGroupService service) {
        oskariMapLayerGroupService = service;
    }

    public void init() {
        // setup service if it hasn't been initialized
        if(oskariMapLayerGroupService == null) {
            setOskariMapLayerGroupService(new OskariMapLayerGroupServiceIbatisImpl());
        }
    }

    /**
     * Handles listing and single maplayer group find
     * @param params
     * @throws ActionException
     */
    public void handleGet(ActionParameters params) throws ActionException {
        final int id = params.getHttpParam(PARAM_ID, -1);
        if(id != -1) {
            // find single group
            final MaplayerGroup maplayerGroup = oskariMapLayerGroupService.find(id);
            ResponseHelper.writeResponse(params, maplayerGroup.getAsJSON());
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

    /**
     * Handles insert
     * @param params
     * @throws ActionException
     */
    public void handlePut(ActionParameters params) throws ActionException {
        params.requireAdminUser();
        MaplayerGroup maplayerGroup = populateFromRequest(params.getPayLoadJSON());

        // Check at group depth is maximum 3
        if(!isAllowedGroupDepth(maplayerGroup)) {
            throw new ActionParamsException("Maximum group depth is 3");
        }

        final int id = oskariMapLayerGroupService.insert(maplayerGroup);
        // check insert by loading from DB
        final MaplayerGroup savedMapLayerGroup = oskariMapLayerGroupService.find(id);
        ResponseHelper.writeResponse(params, savedMapLayerGroup.getAsJSON());
    }

    /**
     * Handles update
     * @param params
     * @throws ActionException
     */
    public void handlePost(ActionParameters params) throws ActionException {
        params.requireAdminUser();
        MaplayerGroup maplayerGroup = populateFromRequest(params.getPayLoadJSON());
        if(maplayerGroup.getId() == -1) {
            // hierarchical admin apparently sends id as separate param
            maplayerGroup.setId(params.getRequiredParamInt(PARAM_ID));
        }
        oskariMapLayerGroupService.update(maplayerGroup);
        ResponseHelper.writeResponse(params, maplayerGroup.getAsJSON());
    }

    /**
     * Handles removal
     * @param params
     * @throws ActionException
     */
    public void handleDelete(ActionParameters params) throws ActionException {
        params.requireAdminUser();
        final int id = params.getRequiredParamInt(PARAM_ID);
        final MaplayerGroup maplayerGroup = oskariMapLayerGroupService.find(id);
        final List<Integer> maplayerIds = oskariMapLayerGroupService.findMaplayersByGroup(id);
        if(!maplayerIds.isEmpty()) {
            // maplayer group with maplayers under it can't be removed
            throw new ActionParamsException("Maplayers linked to maplayer group", JSONHelper.createJSONObject("code", "not_empty"));
        }
        oskariMapLayerGroupService.delete(id);
        ResponseHelper.writeResponse(params, maplayerGroup.getAsJSON());
    }

    /**
     * Calculate group depth
     * @param groupId group id
     * @param depth current depth
     * @return group depth
     */
    private int getGroupDepth(int groupId, int depth) {
        depth++;
        MaplayerGroup maplayerGroup = oskariMapLayerGroupService.find(groupId);
        if(maplayerGroup.getParentId() == -1) {
            return depth;
        }
        return getGroupDepth(maplayerGroup.getParentId(), depth);
    }

    /**
     * Has allowed group depth
     * @param maplayerGroup maplayer group
     * @return is allowed group depth
     */
    private boolean isAllowedGroupDepth(MaplayerGroup maplayerGroup) {
        if(maplayerGroup.getParentId() != -1 && getGroupDepth(maplayerGroup.getParentId(), 0) > 2) {
            return false;
        }
        return true;
    }

    private MaplayerGroup populateFromRequest(JSONObject mapLayerGroupJSON) throws ActionException {
        MaplayerGroup maplayerGroup = new MaplayerGroup();
        try{
            JSONObject locales = mapLayerGroupJSON.getJSONObject(KEY_LOCALES);
            // The classic admin sends id as part of the JSON payload (as string, but with number value...)
            maplayerGroup.setId(ConversionHelper.getInt(mapLayerGroupJSON.optString("id"), -1));
            maplayerGroup.setParentId(mapLayerGroupJSON.optInt(KEY_PARENT_ID, -1));
            maplayerGroup.setSelectable(mapLayerGroupJSON.optBoolean(KEY_SELECTABLE, true));
            Iterator<?> keys = locales.keys();

            while( keys.hasNext() ) {
                String locale = (String)keys.next();
                String name = locales.getString(locale);
                maplayerGroup.setName(locale, name);
            }
        } catch(JSONException ex) {
            throw new ActionException("Cannot populate maplayer group from request", ex);
        }

        return maplayerGroup;
    }
}
