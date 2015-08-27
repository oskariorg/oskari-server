package fi.nls.oskari.control.myplaces;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.myplaces.MyPlacesService;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.ResponseHelper;

@OskariActionRoute("PublishMyPlaceLayer")
public class PublishMyPlaceLayerHandler extends ActionHandler  {

    private MyPlacesService myPlaceService = null;
    private static final Logger log = LogFactory.getLogger(PublishMyPlaceLayerHandler.class);

    public void init() {
        myPlaceService = OskariComponentManager.getComponentOfType(MyPlacesService.class);
    }
    @Override
    public void handleAction(ActionParameters params) throws ActionException {
        final User user = params.getUser();
        final long id = ConversionHelper.getLong(params.getRequest().getParameter("id"), -1);
        if(id == -1) {
            throw new ActionException("Missing id for category publish");
        }
        
        final String makePublicStr = params.getHttpParam("makePublic");
        if(makePublicStr == null) {
            throw new ActionException("Missing boolean for category publish");
        }
        
        final String uuid = user.getUuid();
        String name = null;
        if (ConversionHelper.getBoolean(makePublicStr, false)) {
            name = user.getScreenname();
        }
        
        // SQL update checks that the categories id matches the users
        // to unpublish the name is left as null
        final long updatedRows = myPlaceService.updatePublisherName(id, uuid, name);
        log.debug("Published category:", id, "- uuid:", uuid, "- name", name, " -> updated rows", updatedRows);
        
        ResponseHelper.writeResponse(params, updatedRows == 1);
    }
}
