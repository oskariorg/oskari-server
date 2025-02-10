package fi.nls.oskari.control.myplaces.handler;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.RestActionHandler;
import org.oskari.user.User;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.myplaces.MyPlacesService;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.ResponseHelper;
import org.oskari.log.AuditLog;

@OskariActionRoute("PublishMyPlaceLayer")
public class PublishMyPlaceLayerHandler extends RestActionHandler {

    private MyPlacesService myPlaceService = null;
    private static final Logger log = LogFactory.getLogger(PublishMyPlaceLayerHandler.class);

    public void init() {
        myPlaceService = OskariComponentManager.getComponentOfType(MyPlacesService.class);
    }
    @Override
    public void handlePost(ActionParameters params) throws ActionException {
        final User user = params.getUser();

        final long id = params.getRequiredParamLong("id");
        final boolean isPublic = ConversionHelper.getBoolean(
                params.getRequiredParam("makePublic"), false);
        
        final String uuid = user.getUuid();
        String name = null;
        if (isPublic) {
            name = user.getScreenname();
        }
        
        // SQL update checks that the categories id matches the users
        // to unpublish the name is left as null
        final long updatedRows = myPlaceService.updatePublisherName(id, uuid, name);
        log.debug("Published category:", id, "- uuid:", uuid, "- name", name, " -> updated rows", updatedRows);

        AuditLog.user(params.getClientIp(), params.getUser())
                .withParam("id", id)
                .withParam("public", isPublic)
                .updated(AuditLog.ResourceType.MYPLACES_LAYER);

        ResponseHelper.writeResponse(params, updatedRows == 1);
    }
}
