package fi.mml.portti.service.ogc.handler.action.wfsservicewizard;

import fi.mml.portti.service.ogc.handler.FlowModel;
import fi.mml.portti.service.ogc.handler.OGCActionHandler;
import fi.nls.oskari.domain.map.wfs.WFSService;

public class WfsServiceStoreAction  implements OGCActionHandler{
	

	public void handleAction(FlowModel flowModel) {
		
		WFSService wfsService = new WFSService();
		//wfsService.setBboxParameterName(flowModel.getAsString(FlowModel.FLOW_PM_BBOX_PARAMETER_NAME));
		//wfsService.setOwsAbstractEn(flowModel.getAsString(FlowModel.FLOW_PM_OWS_ABSTRACT_EN));
		//wfsService.setOwsAbstractFi(flowModel.getAsString(FlowModel.FLOW_PM_OWS_ABSTRACT_FI));
		//wfsService.setOwsAbstractSv(flowModel.getAsString(FlowModel.FLOW_PM_OWS_ABSTRACT_SV));
		//wfsService.setPassword(flowModel.getAsString(FlowModel.FLOW_PM_PASSWORD));
		//wfsService.setTitleEn(flowModel.getAsString(FlowModel.FLOW_PM_TITLE_EN));
		wfsService.setTitle("fi", flowModel.getAsString(FlowModel.FLOW_PM_TITLE_FI));
		//wfsService.setTitleSv(flowModel.getAsString(FlowModel.FLOW_PM_TITLE_SV));
		wfsService.setUrl(flowModel.getAsString(FlowModel.FLOW_PM_URL));
		//wfsService.setUsername(flowModel.getAsString(FlowModel.FLOW_PM_USERNAME));
		System.out.println("wfsService stored");
		flowModel.put(FlowModel.WFS_SERVICE, wfsService);
	}

}
