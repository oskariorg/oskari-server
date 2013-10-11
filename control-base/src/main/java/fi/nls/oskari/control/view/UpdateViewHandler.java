package fi.nls.oskari.control.view;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionDeniedException;
import fi.nls.oskari.log.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;

import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.domain.map.view.View;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.view.ViewService;
import fi.nls.oskari.map.view.ViewServiceIbatisImpl;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.RequestHelper;
import fi.nls.oskari.util.ResponseHelper;

@OskariActionRoute("UpdateView")
public class UpdateViewHandler extends ActionHandler {

    private ViewService vs = new ViewServiceIbatisImpl();
    private static final Logger log = LogFactory.getLogger(UpdateViewHandler.class);

    @Override
    public void handleAction(ActionParameters params) throws ActionException {

        final long viewId = ConversionHelper.getLong(params.getHttpParam("id"), -1);

        final User user = params.getUser();
        final View view = vs.getViewWithConf(viewId);

        if (!vs.hasPermissionToAlterView(view, user)) {
            throw new ActionDeniedException("User tried to rename view.");
        } else {
            final String name = RequestHelper.getString(
                    params.getRequest().getParameter("newName"), view.getName());

            final String description = RequestHelper.getString(
            		params.getRequest().getParameter("newDescription"), view.getDescription());
            
            log.debug("Renaming view to: " + name + " with description :" + description);
            view.setName(name);
            view.setDescription(description);
            vs.updateView(view);
    
            try {
                JSONObject resp = new JSONObject();
                resp.put("name", view.getName());
                resp.put("id", view.getId());
                resp.put("isPublic", view.isPublic());
                ResponseHelper.writeResponse(params, resp);
            } catch (JSONException jsonex) {
                throw new ActionException("User tried to rename view:" + 
                        log.getAsString(view) + "- User:" + log.getAsString(user));
            }
        }
    }
}
