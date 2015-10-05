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
import org.json.JSONObject;

@OskariActionRoute("DeleteView")
public class DeleteViewHandler extends ActionHandler {

    private ViewService vs = new ViewServiceIbatisImpl();
    private static final Logger log = LogFactory.getLogger(DeleteViewHandler.class);

    @Override
    public void handleAction(ActionParameters params) throws ActionException {

        final long viewId = ConversionHelper.getLong(params.getHttpParam("id"), -1);

        final User user = params.getUser();
        final long userId = user.getId();
        final View view = vs.getViewWithConf(viewId);
        
        if (!vs.hasPermissionToAlterView(view, user)) {
            throw new ActionDeniedException("User tried to remove view.");
        }
        else {
            log.debug("Deleting view:", view);
            try {
                vs.deleteViewById(viewId);
                JSONObject resp = new JSONObject();
                if (viewId >= 0) {
                    resp.put("id", viewId);
                }
                if (userId >= 0) {
                    resp.put("userId", userId);
                }
                ResponseHelper.writeResponse(params, resp);
            } catch (Exception ex) {
                throw new ActionException("Exception while deleting a view:" + log.getAsString(view), ex);
            }
        }
    }
    
}
