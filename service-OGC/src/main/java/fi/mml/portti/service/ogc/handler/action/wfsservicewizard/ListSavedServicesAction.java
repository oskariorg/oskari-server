package fi.mml.portti.service.ogc.handler.action.wfsservicewizard;

import java.util.List;

import fi.mml.map.mapwindow.service.db.WFSDbService;
import fi.mml.map.mapwindow.service.db.WFSDbServiceIbatisImpl;
import fi.mml.portti.service.ogc.handler.FlowModel;
import fi.mml.portti.service.ogc.handler.OGCActionHandler;
import fi.nls.oskari.domain.map.wfs.WFSService;
import flexjson.JSONSerializer;

public class ListSavedServicesAction implements OGCActionHandler {

	private WFSDbService wfsDbService = new WFSDbServiceIbatisImpl();
	
	@Override
	public void handleAction(FlowModel flowModel) {
		
		List<WFSService> wfsServices = getWFSDbService().findAll();
		flowModel.put(FlowModel.WFS_SERVICES_LIST, wfsServices);
		flowModel.putValueToRootJson("wfs_service_list_json", new JSONSerializer().serialize(wfsServices));
		
		
	}
	
	protected WFSDbService getWFSDbService() {
		return wfsDbService;
	}

}
