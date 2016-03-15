package fi.nls.oskari.control.view;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.*;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.domain.map.view.Bundle;
import fi.nls.oskari.domain.map.view.View;
import fi.nls.oskari.domain.map.view.ViewTypes;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.view.ViewException;
import fi.nls.oskari.map.view.ViewService;
import fi.nls.oskari.map.view.ViewServiceIbatisImpl;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.ResponseHelper;
import org.apache.commons.lang.StringEscapeUtils;
import org.json.JSONObject;


@OskariActionRoute("AddView")
public class SaveViewHandler extends ActionHandler {

    private final static Logger log = LogFactory.getLogger(SaveViewHandler.class);
    private static final ViewService viewService = new ViewServiceIbatisImpl();

    private final static String VIEW_NAME = "viewName";
    private final static String VIEW_DESCRIPTION = "viewDescription";
    private final static String VIEW_DATA = "viewData";
    private final static String IS_DEFAULT = "isDefault";

    public void handleAction(final ActionParameters params) throws ActionException {

        if (params.getUser().isGuest()) {
            throw new ActionDeniedException("Session expired");
        }

        // Cloned view based on user based default view
        final View view = getBaseView(params);
        final User user = params.getUser();

        // Merge client parameters to view
        mergeViewStates(view, getViewJson(params));
        try {

            //set is_default to false for all other this user's views.
            if (view.isDefault()) {
                log.debug("Add View: Reset the user's default views: "+user.getId());
                viewService.resetUsersDefaultViews(user.getId());
            }

            final long newViewId = viewService.addView(view);
            view.setId(newViewId);
        } catch (ViewException e) {
            throw new ActionException("Error when trying add published view", e);
        }

        ResponseHelper.writeResponse(params, JSONHelper.createJSONObject(view.toString()));
    }

    private void mergeViewStates(final View view, final JSONObject viewJson) {
        for(Bundle bundle : view.getBundles()) {
            final JSONObject userState = viewJson.optJSONObject(bundle.getName());
            if(userState != null) {
                // merge the state gotten from browser
                // overriding the template state on view
                JSONObject resultState = JSONHelper.merge(bundle.getStateJSON(), userState.optJSONObject("state"));
                bundle.setState(resultState.toString());
            }
        }
    }


    private JSONObject getViewJson(final ActionParameters params) throws ActionException {
        // View data JSON
        final String data = params.getRequiredParam(VIEW_DATA);
        try {
            return new JSONObject(StringEscapeUtils.unescapeHtml(data));
        } catch (Exception ex) {
            throw new ActionParamsException("[AddViewHandler] Could not get View data : " + ex.toString() + " -- " + params.getHttpParam(VIEW_DATA));
        }
    }

    private View getBaseView(final ActionParameters params) throws ActionException {

        final String viewName = params.getHttpParam(VIEW_NAME);
        final String viewDescription = params.getHttpParam(VIEW_DESCRIPTION);
        final boolean isDefault = params.getHttpParam(IS_DEFAULT, false);
        if (viewName == null) {
            throw new ActionParamsException("Parameter missing:" + VIEW_NAME);
        }

        // get default view id for the user
        final long currentViewId = viewService.getDefaultViewId(params.getUser());
        final View currentView = viewService.getViewWithConf(currentViewId);
        if (currentView == null) {
            throw new ActionParamsException("Could not fetch current view id:" + currentViewId);
        }

        // clone so we are not overwriting the template!
        final View view = currentView.cloneBasicInfo();

        // always save as personal view
        view.setType(ViewTypes.USER);
        view.setIsPublic(false);
        view.setCreator(params.getUser().getId());

        view.setName(viewName);
        view.setDescription(viewDescription);

        view.setIsDefault(isDefault);
        // application/page/devpath should be left as is in "template view"
        return view;
    }

}
