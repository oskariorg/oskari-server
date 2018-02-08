package fi.nls.oskari.control.layer;

import org.oskari.service.util.ServiceFactory;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionDeniedException;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.layer.DataProviderService;
import fi.nls.oskari.util.ConversionHelper;

/**
 * Admin WMS organization layer delete
 * 
 * 
 */
@OskariActionRoute("DeleteOrganization")
public class DeleteOrganizationHandler extends ActionHandler {

    private DataProviderService groupService = ServiceFactory.getDataProviderService();
    private static final Logger log = LogFactory.getLogger(DeleteOrganizationHandler.class);
    private static final String PARAM_GROUP_ID = "id";

    @Override
    public void handleAction(ActionParameters params) throws ActionException {

        final int groupId = ConversionHelper.getInt(params.getRequiredParam(PARAM_GROUP_ID), -1);
        
        if(!groupService.hasPermissionToUpdate(params.getUser(), groupId)) {
            throw new ActionDeniedException("Unauthorized user tried to remove layer group and its map layers - group id=" + groupId);
        }
       
        try {
            // cascade in db will handle that layers are deleted
            groupService.delete(groupId);
        } catch (Exception e) {
            throw new ActionException("Couldn't delete layer group and its map layers - id:" + groupId,
                    e);
        }
    }

}
