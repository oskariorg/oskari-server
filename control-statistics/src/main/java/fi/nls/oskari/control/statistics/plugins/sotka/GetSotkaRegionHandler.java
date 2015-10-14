package fi.nls.oskari.control.statistics.plugins.sotka;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.ResponseHelper;
import org.json.JSONObject;

/**
 * Handler for getting Sotka region id's and codes
 * TODO: GetSotkaRegionHandler is a legacy OskariActionRoute. Move to using plugin API instead when that is implemented.
 */
@OskariActionRoute("GetSotkaRegion")
public class GetSotkaRegionHandler extends ActionHandler {
	
	private static final String PARAM_ID = "id";
	private static final String PARAM_CODE = "code";

    private SotkaRegionParser sotkaRegionParser;

	/**
	 * Handler method for HTTP requests
	 * @param params
	 * @return view identifier so the caller knows which view to show if any (null return means no view should be shown)
	 * @throws ActionException Exception is thrown if the action cannot be handled
	 */
    @Override
    public void handleAction(final ActionParameters params) throws ActionException  {
    	final int id = ConversionHelper.getInt(params.getHttpParam(PARAM_ID), -1);
    	final String code = params.getRequest().getParameter(PARAM_CODE);
        final JSONObject root = new JSONObject();
        
        sotkaRegionParser = new SotkaRegionParser();
        
    	if(id != -1) {
    		String resultCode = sotkaRegionParser.getCode(id);
    		if(resultCode == null) {
    			throw new ActionException("Requested id wasn't found");
    		} else {
    			JSONHelper.putValue(root, PARAM_CODE, resultCode);		
    		}
    	} else if(code != null) {
    		int resultId = sotkaRegionParser.getId(code);
    		if(resultId == -1) {
    			throw new ActionException("Requested code wasn't found");
    		} else {
    			JSONHelper.putValue(root, PARAM_ID, resultId);
    		}
    	}
        
        ResponseHelper.writeResponse(params, root);
    }   
}
