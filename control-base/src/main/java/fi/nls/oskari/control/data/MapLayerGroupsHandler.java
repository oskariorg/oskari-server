package fi.nls.oskari.control.data;

import fi.mml.map.mapwindow.service.db.OskariMapLayerGroupService;
import fi.mml.map.mapwindow.service.db.OskariMapLayerGroupServiceIbatisImpl;
import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.*;
import fi.nls.oskari.domain.map.MaplayerGroup;
import fi.nls.oskari.domain.map.view.View;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.view.util.ViewHelper;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.ResponseHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
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
     * Handles listing and single theme find
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

        // find all themes
        final List<MaplayerGroup> maplayerGroups = oskariMapLayerGroupService.findAll();
        final JSONArray list = new JSONArray();
        for (MaplayerGroup maplayerGroup : maplayerGroups) {
            list.put(maplayerGroup.getAsJSON());
        }
        final JSONObject result = new JSONObject();
        // FIXME ispire need named to maplayergroups
        JSONHelper.putValue(result, "inspire", list);
        ResponseHelper.writeResponse(params, result);
    }

    /**
     * Handles insert
     * @param params
     * @throws ActionException
     */
    public void handlePut(ActionParameters params) throws ActionException {
        checkForAdminPermission(params);
        final MaplayerGroup maplayerGroup = new MaplayerGroup();
        populateFromRequest(params, maplayerGroup);
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
        checkForAdminPermission(params);
        final int id = params.getRequiredParamInt(PARAM_ID);
        final MaplayerGroup theme = oskariMapLayerGroupService.find(id);
        populateFromRequest(params, theme);
        oskariMapLayerGroupService.update(theme);
        ResponseHelper.writeResponse(params, theme.getAsJSON());
    }

    /**
     * Handles removal
     * @param params
     * @throws ActionException
     */
    public void handleDelete(ActionParameters params) throws ActionException {
        checkForAdminPermission(params);
        final int id = params.getRequiredParamInt(PARAM_ID);
        final MaplayerGroup theme = oskariMapLayerGroupService.find(id);
        final List<Integer> maplayerIds = oskariMapLayerGroupService.findMaplayersByGroup(id);
        if(!maplayerIds.isEmpty()) {
            // theme with maplayers under it can't be removed
            throw new ActionParamsException("Maplayers linked to theme", JSONHelper.createJSONObject("code", "not_empty"));
        }
        oskariMapLayerGroupService.delete(id);
        ResponseHelper.writeResponse(params, theme.getAsJSON());
    }

    /**
     * Commonly used with
     * @param params
     * @throws ActionException
     */
    private void checkForAdminPermission(ActionParameters params) throws ActionException {
        if(!params.getUser().isAdmin()) {
            throw new ActionDeniedException("Session expired");
        }
    }

    protected JSONObject getMapLayerGroup(HttpServletRequest req) throws ActionException {
        try (InputStream in = req.getInputStream()) {
            final byte[] json = IOHelper.readBytes(in);
            final String jsonString = new String(json, StandardCharsets.UTF_8);
            final JSONObject maplayerGroup = new JSONObject(jsonString);
            return maplayerGroup;
        } catch (IOException e) {
            log.warn(e);
            throw new ActionException("Failed to read request!");
        } catch (IllegalArgumentException | JSONException e) {
            log.warn(e);
            throw new ActionException("Invalid request!");
        }
    }
    private void populateFromRequest(ActionParameters params, MaplayerGroup maplayerGroup) throws ActionException {

        try{
            JSONObject mapLayerGroup = getMapLayerGroup(params.getRequest());
            JSONObject locales = mapLayerGroup.getJSONObject(KEY_LOCALES);
            int parentId = mapLayerGroup.optInt(KEY_PARENT_ID, -1);
            boolean selectable = mapLayerGroup.optBoolean(KEY_SELECTABLE, true);
            Iterator<?> keys = locales.keys();

            while( keys.hasNext() ) {
                String locale = (String)keys.next();
                String name = locales.getString(locale);
                maplayerGroup.setName(locale, name);
            }
            maplayerGroup.setParentId(parentId);
            maplayerGroup.setSelectable(selectable);
        } catch(JSONException ex) {
            log.error("Cannot populate maplayergroup from request", ex);
            throw new ActionException("Cannot populate maplayergroup from request");
        }
    }
}
