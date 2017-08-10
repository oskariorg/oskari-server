package downloadbasket.actions;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.ResponseHelper;
import downloadbasket.helpers.SendDownloadDetailsToEmailThread;
import org.json.JSONArray;
import org.json.JSONObject;

@OskariActionRoute("DownloadAll")
public class DownloadAll extends ActionHandler {

	private final Logger LOGGER = LogFactory.getLogger(DownloadAll.class);
		
    private static final String PARAM_DOWNLOAD_DETAILS = "downloadDetails";
    private static final String PARAM_USER_DETAILS = "userDetails";

	@Override
    public void handleAction(final ActionParameters params) throws ActionException {
	     
		JSONObject job = new JSONObject();
		String downloadDetails = params.getHttpParam(PARAM_DOWNLOAD_DETAILS).toString();
		String strUserDetails = params.getHttpParam(PARAM_USER_DETAILS).toString();
					
		try {
			JSONObject userDetails = new JSONObject(strUserDetails);
			JSONArray ddArray = new JSONArray(downloadDetails);
			new SendDownloadDetailsToEmailThread(ddArray, userDetails).start();
			job.put("success", true);
		} catch (Exception e) {
			throw new ActionException("Could not handle DownloadAll request: ", e);
		}
		
		ResponseHelper.writeResponse(params, job);
	}
}
