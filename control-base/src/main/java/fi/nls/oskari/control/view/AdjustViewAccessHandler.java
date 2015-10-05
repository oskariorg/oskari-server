package fi.nls.oskari.control.view;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionDeniedException;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.domain.map.view.View;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.view.ViewService;
import fi.nls.oskari.map.view.ViewServiceIbatisImpl;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.ResponseHelper;
import org.json.JSONException;
import org.json.JSONObject;

@OskariActionRoute("AdjustViewAccess")
public class AdjustViewAccessHandler extends ActionHandler {

    private ViewService vs = new ViewServiceIbatisImpl();
    private static final Logger log = LogFactory.getLogger(AdjustViewAccessHandler.class);

    @Override
    public void handleAction(ActionParameters params) throws ActionException {

        final long viewId = ConversionHelper.getLong(params.getHttpParam("id"), -1);

        final User user = params.getUser();
        final View view = vs.getViewWithConf(viewId);

        if (!vs.hasPermissionToAlterView(view, user)) {
            throw new ActionDeniedException("User tried to adjust view access with id:" + viewId);
        } else {
            final boolean isPublic = ConversionHelper.getBoolean(
                    params.getHttpParam("isPublic"), false);

            view.setIsPublic(isPublic);
            
            if(isPublic) {
                log.debug("Making view public:", view);
            }
            else {
                log.debug("Making view private:", view);
            }
            
            vs.updateAccessFlag(view);
            try {
                JSONObject resp = new JSONObject();
                resp.put("name", view.getName());
                resp.put("id", view.getId());
                resp.put("uuid", view.getUuid());
                resp.put("isPublic", view.isPublic());
                ResponseHelper.writeResponse(params, resp);
            } catch (JSONException jsonex) {
                throw new ActionException("Exception while adjusting view access:" + log.getAsString(view), jsonex);
            }
        }
    }
}
