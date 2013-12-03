package fi.mml.portti.service.ogc.handler;

import fi.mml.portti.service.ogc.OgcFlowException;

public interface OGCActionHandler {

	public void handleAction(FlowModel flowModel) throws OgcFlowException;
}
