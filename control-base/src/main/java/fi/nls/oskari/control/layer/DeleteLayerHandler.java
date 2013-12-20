package fi.nls.oskari.control.layer;

import fi.mml.portti.service.db.permissions.PermissionsService;
import fi.mml.portti.service.db.permissions.PermissionsServiceIbatisImpl;
import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionDeniedException;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.layer.OskariLayerService;
import fi.nls.oskari.map.layer.OskariLayerServiceIbatisImpl;
import fi.nls.oskari.util.ConversionHelper;

/**
 * Admin WMS layer delete for single layer, for base/group layers -> use DeleteOrganizationHandler
 * 
 * 
 */
@OskariActionRoute("DeleteLayer")
public class DeleteLayerHandler extends ActionHandler {

    private static final Logger log = LogFactory.getLogger(DeleteLayerHandler.class);
    private static final OskariLayerService mapLayerService = new OskariLayerServiceIbatisImpl();
    private PermissionsService permissionsService = new PermissionsServiceIbatisImpl();

    private static final String PARM_LAYER_ID = "layer_id";

    public void handleAction(ActionParameters params) throws ActionException {

        final int layer_id = ConversionHelper.getInt(params.getRequiredParam(PARM_LAYER_ID), -1);

        if(!permissionsService.hasEditPermissionForLayerByLayerId(params.getUser(), layer_id)) {
            throw new ActionDeniedException("Unauthorized user tried to remove layer - id: " + layer_id);
        }

        try {
            mapLayerService.delete(layer_id);
        } catch (Exception e) {
            throw new ActionException("Couldn't delete map layer - id:" + layer_id,
                    e);
        }
    }

}
