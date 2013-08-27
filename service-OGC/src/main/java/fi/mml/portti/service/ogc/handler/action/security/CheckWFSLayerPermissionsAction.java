package fi.mml.portti.service.ogc.handler.action.security;

import java.util.List;

import fi.mml.portti.domain.permissions.Permissions;
import fi.mml.portti.service.db.permissions.PermissionsService;
import fi.mml.portti.service.db.permissions.PermissionsServiceIbatisImpl;
import fi.mml.portti.service.ogc.OgcFlowException;
import fi.mml.portti.service.ogc.handler.FlowModel;
import fi.mml.portti.service.ogc.handler.OGCActionHandler;
import fi.mml.portti.service.ogc.handler.action.asker.BaseAskerAction;
import fi.nls.oskari.domain.map.wfs.WFSLayer;

public class CheckWFSLayerPermissionsAction extends BaseAskerAction implements OGCActionHandler {
	
	/** Permissions Service */
	private PermissionsService permissionsService = new PermissionsServiceIbatisImpl();
	
	@Override
	public void handleAction(FlowModel flowModel) throws OgcFlowException {
		WFSLayer wfsLayer = findWFSLayer(flowModel);
		
		List<String> resources = permissionsService.getResourcesWithGrantedPermissions(
				Permissions.RESOUCE_TYPE_WMS_LAYER, 
				flowModel.getUser(), 
				Permissions.PERMISSION_TYPE_VIEW_LAYER);
		
		List<String> publishedView = permissionsService.getResourcesWithGrantedPermissions(
                Permissions.RESOUCE_TYPE_WMS_LAYER, 
                flowModel.getUser(), 
                Permissions.PERMISSION_TYPE_VIEW_PUBLISHED);
		
		if (!resources.contains(wfsLayer.getWmsUrl() + "+" + wfsLayer.getWmsName()) &&
		    !publishedView.contains(wfsLayer.getWmsUrl() + "+" + wfsLayer.getWmsName())    ) {
			throw new OgcFlowException("No VIEW permissions to layer: " + wfsLayer);
		}
	}
}
