package fi.nls.oskari.control.layer;

import org.json.JSONObject;
import org.oskari.service.util.ServiceFactory;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.domain.map.DataProvider;
import fi.nls.oskari.map.layer.DataProviderService;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.ResponseHelper;

/**
 * Get single data provider registered in Oskari database
 */
@OskariActionRoute("GetDataProvider")
public class GetDataProviderHandler extends ActionHandler {

	private static final String PARAM_ID = "id";

	private DataProviderService service = ServiceFactory.getDataProviderService();

	@Override
	public void handleAction(ActionParameters params) throws ActionException {
		try {
			final int id = Integer.valueOf(params.getHttpParam(PARAM_ID));
			DataProvider d = service.find(id);
			final JSONObject result = new JSONObject();
			JSONHelper.putValue(result, "dataprovider", d.getAsJSON());
			ResponseHelper.writeResponse(params, result);
		} catch (NumberFormatException nfe) {
			throw new ActionException("Invalid id query parameter", nfe);
		} catch (Exception e) {
			throw new ActionException("Data provider query failed", e);
		}
	}
}
