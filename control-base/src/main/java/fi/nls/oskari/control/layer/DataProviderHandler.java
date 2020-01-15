package fi.nls.oskari.control.layer;

import org.oskari.service.util.ServiceFactory;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.RestActionHandler;
import fi.nls.oskari.domain.map.DataProvider;
import fi.nls.oskari.map.layer.DataProviderService;
import fi.nls.oskari.util.ResponseHelper;

/**
 * Action route handler for single data provider related operations
 */
@OskariActionRoute("DataProvider")
public class DataProviderHandler extends RestActionHandler {

	private static final String PARAM_ID = "id";

	private DataProviderService service = ServiceFactory.getDataProviderService();

	@Override
	public void handleGet(ActionParameters params) throws ActionException {
		
		final int id = params.getRequiredParamInt(PARAM_ID);
		
		try {
			DataProvider d = service.find(id);
			ResponseHelper.writeResponse(params, d.getAsJSON());
		} catch (Exception e) {
			throw new ActionException("Data provider query failed", e);
		}
	}
}
