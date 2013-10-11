package fi.mml.portti.service.ogc.handler.action.wfsservicewizard;

import fi.mml.portti.service.ogc.handler.FlowModel;
import fi.mml.portti.service.ogc.handler.OGCActionHandler;
import fi.nls.oskari.domain.map.wfs.WFSService;

/**
 * This action will store authentication information
 *
 */
public class AuthenticationInformationStoreAction implements OGCActionHandler {
	
	public void handleAction(FlowModel flowModel) {
			
		String username = flowModel.getAsString(FlowModel.FLOW_PM_USERNAME);
		String password = flowModel.getAsString(FlowModel.FLOW_PM_PASSWORD);
				
		WFSService service = (WFSService) flowModel.get(FlowModel.WFS_SERVICE);
		service.setUsername(username);
		service.setPassword(password);
	}
}
