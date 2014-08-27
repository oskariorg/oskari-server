package fi.nls.oskari.control.data;

import fi.mml.map.mapwindow.service.db.InspireThemeService;
import fi.mml.map.mapwindow.service.db.InspireThemeServiceIbatisImpl;
import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.*;
import fi.nls.oskari.domain.map.InspireTheme;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.RequestHelper;
import fi.nls.oskari.util.ResponseHelper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

import static fi.nls.oskari.control.ActionConstants.PARAM_ID;
import static fi.nls.oskari.control.ActionConstants.PARAM_NAME_PREFIX;


/**
 * CRUD for Inspire themes. Get is callable by anyone, other methods require admin user.
 */
@OskariActionRoute("InspireThemes")
public class InspireThemesHandler extends RestActionHandler {

    private InspireThemeService inspireThemeService;

    public void setInspireThemeService(final InspireThemeService service) {
        inspireThemeService = service;
    }

    public void init() {
        // setup service if it hasn't been initialized
        if(inspireThemeService == null) {
            setInspireThemeService(new InspireThemeServiceIbatisImpl());
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
            // find single theme
            final InspireTheme theme = inspireThemeService.find(id);
            ResponseHelper.writeResponse(params, theme.getAsJSON());
            return;
        }

        // find all themes
        final List<InspireTheme> inspireThemes = inspireThemeService.findAll();
        final JSONArray list = new JSONArray();
        for (InspireTheme theme : inspireThemes) {
            list.put(theme.getAsJSON());
        }
        final JSONObject result = new JSONObject();
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
        final InspireTheme theme = new InspireTheme();
        populateFromRequest(params, theme);
        final int id = inspireThemeService.insert(theme);
        // check insert by loading from DB
        final InspireTheme savedTheme = inspireThemeService.find(id);
        ResponseHelper.writeResponse(params, savedTheme.getAsJSON());
    }

    /**
     * Handles update
     * @param params
     * @throws ActionException
     */
    public void handlePost(ActionParameters params) throws ActionException {
        checkForAdminPermission(params);
        final int id = params.getRequiredParamInt(PARAM_ID);
        final InspireTheme theme = inspireThemeService.find(id);
        populateFromRequest(params, theme);
        inspireThemeService.update(theme);
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
        final InspireTheme theme = inspireThemeService.find(id);
        final List<Integer> maplayerIds = inspireThemeService.findMaplayersByTheme(id);
        if(!maplayerIds.isEmpty()) {
            // theme with maplayers under it can't be removed
            throw new ActionParamsException("Maplayers linked to theme", JSONHelper.createJSONObject("code", "not_empty"));
        }
        inspireThemeService.delete(id);
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

    private void populateFromRequest(ActionParameters params, InspireTheme theme) throws ActionException {
        final Map<String, String> parsed = RequestHelper.parsePrefixedParamsMap(params.getRequest(), PARAM_NAME_PREFIX);
        for(Map.Entry<String, String> entry : parsed.entrySet()) {
            // TODO: check against supported locales?
            theme.setName(entry.getKey(), entry.getValue());
        }
    }
}
