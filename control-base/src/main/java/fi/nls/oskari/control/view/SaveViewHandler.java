package fi.nls.oskari.control.view;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.*;
import fi.nls.oskari.domain.map.view.View;
import fi.nls.oskari.domain.map.view.ViewTypes;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.view.ViewException;
import fi.nls.oskari.map.view.ViewService;
import fi.nls.oskari.map.view.ViewServiceIbatisImpl;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.ResponseHelper;
import org.apache.commons.lang.StringEscapeUtils;
import org.json.JSONException;
import org.json.JSONObject;


@OskariActionRoute("AddView")
public class SaveViewHandler extends ActionHandler {

    private final static Logger log = LogFactory.getLogger(SaveViewHandler.class);
    private static final ViewService viewService = new ViewServiceIbatisImpl();

    private final static String VIEW_NAME = "viewName";
    private final static String VIEW_DESCRIPTION = "viewDescription";
    private final static String PUBDOMAIN = "pubDomain";
    private final static String VIEW_DATA = "viewData";
    private final static String VIEW_IS_PUBLIC = "viewIsPublic";

    public void handleAction(final ActionParameters params) throws ActionException {

        if (params.getUser().isGuest()) {
            throw new ActionDeniedException("Session expired");
        }

        final View newView = this.addView(params);
        if (newView == null) {
            ResponseHelper.writeResponse(params, false);
            return;
        }

        try {
            JSONObject newViewJson = new JSONObject(newView.toString());
            ResponseHelper.writeResponse(params, newViewJson);
        } catch (JSONException je) {
            throw new ActionException("Couldn't convert view to JSON:" + newView.getId(), je);
        }
    }

    private synchronized View addView(final ActionParameters params) throws ActionException {

        // Current View
        final View currentView = getCurrentView(params);
        if (currentView == null) {
            return null;
        }

        // View data JSON
        final JSONObject viewJson = getViewJson(params);

        // Create new View
        final View newView = new View();

        try {
            final long newViewId = viewService.addView(currentView, viewJson);
            newView.setId(newViewId);
        } catch (ViewException e) {
            log.error("Error when trying add published view", e);
        }

        return newView;
    }


    private synchronized JSONObject getViewJson(final ActionParameters params) throws ActionException {

        // View data JSON
        JSONObject viewJson;
        try {
            String vjs = String.valueOf(params.getHttpParam(VIEW_DATA));
            vjs = StringEscapeUtils.unescapeHtml(vjs);
            viewJson = new JSONObject(vjs);

        } catch (Exception ex) {
            throw new ActionParamsException("[AddViewHandler] Could not get View data : " + ex.toString() + " -- " + params.getHttpParam(VIEW_DATA));
        }

        return viewJson;
    }

    private synchronized View getCurrentView(final ActionParameters params) throws ActionException {

        final String viewName = params.getHttpParam(VIEW_NAME);
        final String viewDescription = params.getHttpParam(VIEW_DESCRIPTION);
        if (viewName == null) {
            log.debug("No name for view, skipping");
            return null;
        }

        // Access flag
        final boolean isPublic = ConversionHelper.getBoolean(params.getHttpParam(VIEW_IS_PUBLIC), false);
        // Publication domain
        final String pubDomain = params.getHttpParam(PUBDOMAIN);

        // get default view id for the user
        final long currentViewId = viewService.getDefaultViewId(params.getUser());
        final View currentView = viewService.getViewWithConf(currentViewId);
        if (currentView == null) {
            throw new ActionParamsException("Could not fetch current view id:" + currentViewId);
        }

        currentView.setName(viewName);
        currentView.setDescription(viewDescription);
        currentView.setType(params.getHttpParam(ViewTypes.VIEW_TYPE, ViewTypes.USER));
        currentView.setCreator(params.getUser().getId());
        currentView.setIsPublic(isPublic);
        // application/page/devpath should be left as is in "template view"
        return currentView;
    }

}
