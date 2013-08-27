package fi.mml.portti.service.ogc.handler.action.wfsservicewizard;

import fi.mml.map.mapwindow.service.db.WFSDbService;
import fi.mml.map.mapwindow.service.db.WFSDbServiceIbatisImpl;
import fi.mml.portti.service.ogc.handler.FlowModel;
import fi.mml.portti.service.ogc.handler.OGCActionHandler;

public class RemoveWFSServiceAction implements OGCActionHandler {

	private WFSDbService wfsDbService = new WFSDbServiceIbatisImpl();

	@Override
	public void handleAction(FlowModel flowModel) {		
		int id = Integer.parseInt(String.valueOf(flowModel.get("flow_pm_service_id")));
		getWFSDbService().delete(id);
	}
	
	protected WFSDbService getWFSDbService() {
		return wfsDbService;
	}
}
