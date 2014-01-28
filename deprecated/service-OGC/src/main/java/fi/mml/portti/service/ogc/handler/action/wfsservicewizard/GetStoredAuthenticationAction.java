package fi.mml.portti.service.ogc.handler.action.wfsservicewizard;

import fi.mml.portti.service.ogc.handler.FlowModel;
import fi.mml.portti.service.ogc.handler.OGCActionHandler;
import fi.nls.oskari.domain.map.wfs.WFSService;

public class GetStoredAuthenticationAction implements OGCActionHandler {

	@Override
	public void handleAction(FlowModel flowModel) {
		WFSService service = (WFSService) flowModel.get(FlowModel.WFS_SERVICE);
		
		if (!"".equals(service.getPassword()) && !"".equals(service.getUsername())) {
			flowModel.putValueToRootJson(FlowModel.IS_AUTHENTICATION_STORED, "true");
			flowModel.putValueToRootJson(FlowModel.FLOW_PM_USERNAME, service.getUsername());
			flowModel.putValueToRootJson(FlowModel.FLOW_PM_PASSWORD, service.getPassword());
			
		} else {
			flowModel.putValueToRootJson(FlowModel.IS_AUTHENTICATION_STORED, "false");
		}
		
	}
}
