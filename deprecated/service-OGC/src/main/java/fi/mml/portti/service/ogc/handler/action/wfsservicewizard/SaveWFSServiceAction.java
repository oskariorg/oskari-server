package fi.mml.portti.service.ogc.handler.action.wfsservicewizard;

import fi.mml.map.mapwindow.service.db.WFSDbService;
import fi.mml.map.mapwindow.service.db.WFSDbServiceIbatisImpl;
import fi.mml.portti.service.ogc.handler.FlowModel;
import fi.mml.portti.service.ogc.handler.OGCActionHandler;
import fi.nls.oskari.domain.map.wfs.WFSService;

public class SaveWFSServiceAction implements OGCActionHandler {

	private WFSDbService wfsDbService = new WFSDbServiceIbatisImpl();
	

	@Override
	public void handleAction(FlowModel flowModel) {
		WFSService wfsService = (WFSService) flowModel.get(FlowModel.WFS_SERVICE);
		getWFSDbService().insertWFSService(wfsService);		
	}
	
	
	protected WFSDbService getWFSDbService() {
		return wfsDbService;
	}
}
