package fi.nls.oskari.control.layer;

import fi.nls.oskari.control.*;
import fi.nls.oskari.domain.map.DataProvider;
import fi.nls.oskari.util.ResponseHelper;
import org.oskari.service.util.ServiceFactory;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.map.layer.DataProviderService;
import fi.nls.oskari.util.ConversionHelper;
import static fi.nls.oskari.control.ActionConstants.PARAM_ID;

/**
 * For deleting a dataprovider/organization
 */
@OskariActionRoute("DeleteOrganization")
public class DeleteOrganizationHandler extends ActionHandler {

    private DataProviderService groupService = ServiceFactory.getDataProviderService();

    @Override
    public void handleAction(ActionParameters params) throws ActionException {

        final int groupId = params.getRequiredParamInt(PARAM_ID);
        
        if(!groupService.hasPermissionToUpdate(params.getUser(), groupId)) {
            throw new ActionDeniedException("Unauthorized user tried to remove layer group and its map layers - group id=" + groupId);
        }

        try {
            DataProvider organization = groupService.find(groupId);
            if(organization == null) {
                throw new ActionParamsException("Dataprovider not found with id " + groupId);
            }
            // cascade in db will handle that layers are deleted
            groupService.delete(groupId);

            // write deleted organization as response
            ResponseHelper.writeResponse(params, organization.getAsJSON());
        } catch (Exception e) {
            throw new ActionException("Couldn't delete layer group and its map layers - id:" + groupId, e);
        }
    }

}
