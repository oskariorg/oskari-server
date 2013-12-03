package fi.nls.oskari.control.layer;

import fi.mml.map.mapwindow.service.db.MapLayerService;
import fi.mml.map.mapwindow.service.db.MapLayerServiceIbatisImpl;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionDeniedException;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.ConversionHelper;

/**
 * Admin WMS layer delete for single layer, for base/group layers -> use DeleteOrganizationHandler
 * 
 * 
 */
@OskariActionRoute("DeleteLayer")
public class DeleteLayerHandler extends ActionHandler {

    private static final Logger log = LogFactory.getLogger(DeleteLayerHandler.class);
    private MapLayerService mapLayerService = new MapLayerServiceIbatisImpl();
    private static final String PARM_LAYER_ID = "layer_id";

    public void handleAction(ActionParameters params) throws ActionException {

        final String layer_id = params.getHttpParam(PARM_LAYER_ID, "");
        final int layid = ConversionHelper.getInt(layer_id, 0);
        
        if(!mapLayerService.hasPermissionToUpdate(params.getUser(), layid)) {
            throw new ActionDeniedException("Unauthorized user tried to remove layer - id: " + layer_id);
        }

        try {
            mapLayerService.delete(layid);
        } catch (Exception e) {
            throw new ActionException("Couldn't delete map layer - id:" + layer_id,
                    e);
        }
    }

}
