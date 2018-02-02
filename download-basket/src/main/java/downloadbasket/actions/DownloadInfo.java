package downloadbasket.actions;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.layer.OskariLayerService;
import fi.nls.oskari.map.layer.OskariLayerServiceIbatisImpl;
import fi.nls.oskari.util.ResponseHelper;
import fi.nls.oskari.util.JSONHelper;
import downloadbasket.helpers.SendDownloadDetailsToEmailThread;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Checks all download- and user details of the download basket when starting
 * the download process. Returns boolean "success".
 */

@OskariActionRoute("DownloadInfo")
public class DownloadInfo extends ActionHandler {

	private final Logger LOGGER = LogFactory.getLogger(DownloadInfo.class);

	private static final String PARAM_DOWNLOAD_DETAILS = "downloadDetails";
	private static final String PARAM_USER_DETAILS = "userDetails";
	private OskariLayerService mapLayerService;

	@Override
	public void init() {
		mapLayerService = new OskariLayerServiceIbatisImpl();
	}

	@Override
	public void handleAction(final ActionParameters params) throws ActionException {

		String downloadDetails = params.getRequiredParam(PARAM_DOWNLOAD_DETAILS);
		String strUserDetails = params.getRequiredParam(PARAM_USER_DETAILS);
		try {
			JSONObject userDetails = new JSONObject(strUserDetails);
			JSONArray ddArray = new JSONArray(downloadDetails);
			new SendDownloadDetailsToEmailThread(mapLayerService, ddArray, userDetails, params.getLocale()).run();
		} catch (Exception e) {
			throw new ActionException("Could not handle DownloadInfo request: ", e);
		}

		ResponseHelper.writeResponse(params, JSONHelper.createJSONObject("success", true));

	}
}