package fi.nls.oskari.control.view;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.*;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.domain.map.view.View;
import fi.nls.oskari.domain.map.view.ViewTypes;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.view.ViewService;
import fi.nls.oskari.map.view.AppSetupServiceMybatisImpl;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.ResponseHelper;
import fi.nls.oskari.utils.AuditLog;
import org.json.JSONObject;

@OskariActionRoute("DeleteView")
public class DeleteViewHandler extends RestActionHandler {

    private ViewService vs = new AppSetupServiceMybatisImpl();
    private static final Logger log = LogFactory.getLogger(DeleteViewHandler.class);

    @Override
    public void handlePost(ActionParameters params) throws ActionException {

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
                AuditLog audit = AuditLog.user(params.getClientIp(), params.getUser())
                        .withParam("uuid", view.getUuid())
                        .withParam("name", view.getName());
                if (ViewTypes.USER.equals(view.getType())) {
                    audit.deleted(AuditLog.ResourceType.USER_VIEW);
                } else {
                    audit.withParam("domain", view.getPubDomain())
                        .deleted(AuditLog.ResourceType.EMBEDDED_VIEW);
                }

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
