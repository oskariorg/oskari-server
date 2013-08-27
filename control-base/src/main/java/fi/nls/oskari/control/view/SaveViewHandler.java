package fi.nls.oskari.control.view;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionDeniedException;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
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

    private static final long FULL_MAP_VIEW_TEMPLATE_ID = 1;
    private static final String KEY_UPDATE_VIEW_ID = "Id";

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


    private synchronized JSONObject getViewJson(final ActionParameters params) {

        // View data JSON
        JSONObject viewJson;
        try {
            String vjs = String.valueOf(params.getHttpParam(VIEW_DATA));
            vjs = StringEscapeUtils.unescapeHtml(vjs);
            viewJson = new JSONObject(vjs);

        } catch (Exception ex) {
            throw new RuntimeException("[AddViewHandler] Could not get View data");
        }

        return viewJson;
    }

    private synchronized View getCurrentView(final ActionParameters params) throws ActionException {

        // Old id
        long oldId = -1;
        long currentViewId = -1;

        final String viewName = params.getHttpParam(VIEW_NAME);
        final String viewDescription = params.getHttpParam(VIEW_DESCRIPTION);
        if (viewName == null) {
            log.debug("No name for view, skipping");
            return null;
        }

        // Access flag

        final boolean isPublic = ConversionHelper.getBoolean(params.getHttpParam(VIEW_IS_PUBLIC), false);
        /*String pubString = params.getHttpParam(VIEW_IS_PUBLIC);
        isPublic = pubString == null ? false : Boolean.valueOf(pubString);
          */

        // Publication domain
        final String pubDomain = params.getHttpParam(PUBDOMAIN);

        View currentView;
        try {
            // FIXME: checking on additional params but getting it from http params? wat?
            if (params.getAdditionalParam(KEY_UPDATE_VIEW_ID) != null) {
                currentViewId = Long.parseLong(params.getHttpParam(KEY_UPDATE_VIEW_ID));
            } else {
                //currentViewId = PublishHandler.PUBLISHED_VIEW_TEMPLATE_ID;
                currentViewId = FULL_MAP_VIEW_TEMPLATE_ID;
            }
            currentView = viewService.getViewWithConf(currentViewId);

        } catch (Exception ex) {
            throw new ActionException("Could not fetch current view id:" + currentViewId);
        }


        if (pubDomain != null) {
            currentView.setPubDomain(pubDomain);
        }

        currentView.setOldId(oldId);
        currentView.setName(viewName);
        currentView.setDescription(viewDescription);
        currentView.setType(params.getHttpParam(ViewTypes.VIEW_TYPE, ViewTypes.USER));
        currentView.setCreator(params.getUser().getId());
        currentView.setIsPublic(isPublic);

        // TODO: from properties view.template.
        currentView.setApplication("full-map");
        currentView.setPage("view");
        currentView.setDevelopmentPath("/applications/paikkatietoikkuna.fi");

        return currentView;
    }

}
