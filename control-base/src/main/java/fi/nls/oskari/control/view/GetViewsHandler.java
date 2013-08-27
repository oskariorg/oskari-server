package fi.nls.oskari.control.view;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.domain.map.view.Bundle;
import fi.nls.oskari.domain.map.view.View;
import fi.nls.oskari.domain.map.view.ViewTypes;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.view.ViewService;
import fi.nls.oskari.map.view.ViewServiceIbatisImpl;
import fi.nls.oskari.util.ResponseHelper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

@OskariActionRoute("GetViews")
public class GetViewsHandler extends ActionHandler {

    public static final String KEY_NAME = "name";
    public static final String KEY_DESCRIPTION = "description";
    public static final String KEY_ID = "id";
    public static final String KEY_LANG = "lang";
    public static final String KEY_ISPUBLIC = "isPublic";
    public static final String KEY_PUBDOMAIN = "pubDomain";
    public static final String KEY_STATE = "state";
    public static final String KEY_VIEWS = "views";
    public static final String KEY_CONFIG = "config";

    private ViewService viewService = null;

    private final static Logger log = LogFactory.getLogger(GetViewsHandler.class);

    public void setViewService(final ViewService service) {
        viewService = service;
    }

    public void init() {
        // setup service if it hasn't been initialized
        if (viewService == null) {
            setViewService(new ViewServiceIbatisImpl());
        }
    }

    public void handleAction(final ActionParameters params) throws ActionException {

        final String viewType = params.getHttpParam(ViewTypes.VIEW_TYPE, ViewTypes.USER);

        final long userId = params.getUser().getId();

        final List<View> views = viewService.getViewsForUser(userId);
        final JSONArray viewArray = new JSONArray();
        for (View view : views) {

            if (view.getType() == null) {
                view.setType(ViewTypes.USER);
            }

            if (viewType.indexOf(view.getType()) == -1) {
                continue;
            }

            final JSONObject viewJson = new JSONObject();
            try {
                viewJson.put(KEY_NAME, view.getName());
                viewJson.put(KEY_DESCRIPTION, view.getDescription());
                viewJson.put(KEY_LANG, view.getLang());
                viewJson.put(KEY_ID, view.getId());
                viewJson.put(KEY_ISPUBLIC, view.isPublic());
                viewJson.put(KEY_PUBDOMAIN, view.getPubDomain());
                final JSONObject stateAccu = new JSONObject();
                for (Bundle bundle : view.getBundles()) {

                    final JSONObject bundleNode = new JSONObject();
                    try {
                        bundleNode.put(KEY_STATE, new JSONObject(bundle.getState()));
                        bundleNode.put(KEY_CONFIG, new JSONObject(bundle.getConfig()));
                        stateAccu.put(bundle.getBundleinstance(), bundleNode);
                    } catch (Exception e) {
                        log.debug("Status " + bundle.getStartup());
                        log.debug("Config " + bundle.getConfig());
                    }


                }
                viewJson.put(KEY_STATE, stateAccu);
                viewArray.put(viewJson);

            } catch (Exception ex) {
                log.error("[GetViewsHandler] Failed to parse states "
                        + "for view:", view);
            }

        }

        try {
            JSONObject ret = new JSONObject();
            ret.put(KEY_VIEWS, viewArray);
            ResponseHelper.writeResponse(params, ret);
        } catch (Exception ex) {
            ResponseHelper.writeResponse(params, false);
            log.error(ex);
        }
    }
}
