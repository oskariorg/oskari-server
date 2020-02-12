package fi.nls.oskari.control.layer;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.cache.JedisManager;
import fi.nls.oskari.control.*;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.domain.map.wfs.WFSLayerConfiguration;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.layer.OskariLayerService;
import fi.nls.oskari.map.layer.OskariLayerServiceMybatisImpl;
import fi.nls.oskari.util.PropertyUtil;
import org.oskari.log.AuditLog;
import fi.nls.oskari.wfs.WFSLayerConfigurationService;
import fi.nls.oskari.wfs.WFSLayerConfigurationServiceIbatisImpl;

/**
 * Admin WMS layer delete for single layer, for base/group layers -> use DeleteOrganizationHandler
 * 
 * 
 */
@OskariActionRoute("DeleteLayer")
public class DeleteLayerHandler extends AbstractLayerAdminHandler {

    private static final Logger log = LogFactory.getLogger(DeleteLayerHandler.class);
    private static final OskariLayerService mapLayerService = new OskariLayerServiceMybatisImpl();
    WFSLayerConfigurationService wfsLayerService = new WFSLayerConfigurationServiceIbatisImpl();

    private static final String PARAM_LAYER_ID = "layer_id";

    public void handlePost(ActionParameters params) throws ActionException {

        final int id = params.getRequiredParamInt(PARAM_LAYER_ID);
        final OskariLayer layer = mapLayerService.find(id);
        if(layer == null) {
            throw new ActionParamsException("Layer not found - id: " + id);
        }

        if(!userHasEditPermission(params.getUser(), layer)) {
            throw new ActionDeniedException("Unauthorized user tried to remove layer - id: " + layer.getId());
        }

        try {
            mapLayerService.delete(layer.getId());

            AuditLog.user(params.getClientIp(), params.getUser())
                    .withParam("id", layer.getId())
                    .withParam("uiName", layer.getName(PropertyUtil.getDefaultLanguage()))
                    .withParam("url", layer.getUrl())
                    .withParam("name", layer.getName())
                    .deleted(AuditLog.ResourceType.MAPLAYER);

            if(layer.getType().equals(OskariLayer.TYPE_WFS))
            {
                wfsLayerService.delete(layer.getId());
            }
        } catch (Exception e) {
            throw new ActionException("Couldn't delete map layer - id:" + layer.getId(), e);
        }
    }

}
