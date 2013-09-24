package fi.nls.oskari.control.layer;

import java.util.List;
import fi.mml.map.mapwindow.service.db.LayerClassService;
import fi.mml.map.mapwindow.service.db.MapLayerService;
import fi.mml.map.mapwindow.service.db.LayerClassServiceIbatisImpl;
import fi.mml.map.mapwindow.service.db.MapLayerServiceIbatisImpl;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionDeniedException;
import fi.nls.oskari.domain.map.Layer;

import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.ConversionHelper;

/**
 * Admin WMS organization layer delete
 * 
 * 
 */
@OskariActionRoute("DeleteOrganization")
public class DeleteOrganizationHandler extends ActionHandler {

    private LayerClassService layerClassService = new LayerClassServiceIbatisImpl();
    private MapLayerService mapLayerService = new MapLayerServiceIbatisImpl();
    private static final Logger log = LogFactory.getLogger(DeleteOrganizationHandler.class);
    private static final String PARM_LAYERCLASS_ID = "layercl_id";

    @Override
    public void handleAction(ActionParameters params) throws ActionException {

        final String layercl_id = params.getHttpParam(PARM_LAYERCLASS_ID, "");
        final int layclid = ConversionHelper.getInt(layercl_id, 0);
        
        if(!mapLayerService.hasPermissionToUpdate(params.getUser(), layclid)) {
            throw new ActionDeniedException("Unauthorized user tried to remove class layer and its map layers - layer id=" + layercl_id);
        }
       
        try {
            List<Layer> mls = mapLayerService.findWithLayerClass(layclid);
            
            for (Layer ml : mls) {
                mapLayerService.delete(ml.getId());
            }

            layerClassService.delete(layclid);

        } catch (Exception e) {
            throw new ActionException("Couldn't delete class layer and its map layers - id:" + layercl_id,
                    e);
        }
    }

}
