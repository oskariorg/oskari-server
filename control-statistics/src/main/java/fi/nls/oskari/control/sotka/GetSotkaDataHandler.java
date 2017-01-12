package fi.nls.oskari.control.sotka;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.statistics.plugins.sotka.requests.SotkaRequest;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.ResponseHelper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Enumeration;

/**
 * Data request of Sotkanet and response to Oskari
 * 
 * - action_route=GetSotkaData + version={1.0|1.1} +
 * action={indicators|indicator_metadata|regions|data}
 * 
 * Extra parameters:
 * - action=indicator_meta + indicator=
 * - action=data + indicator= + years= + years= + gender=
 * 
 * eg.
 * <oskari url>&action_route=GetSotkaData&action=indicators&version=1.1
 * <oskari url>&action_route=GetSotkaData&action=regions&version=1.1
 * <oskari
 * url>&action_route=GetSotkaData&action=indicator_meta&indicator=127&version
 * =1.1
 * <oskari url>&action_route=GetSotkaData&action=data&version=1.1&indicator=127
 * &years=2010&years=2011&gender=female
 * <oskari url>action_route=GetSotkaData&action=data&version=1.0&indicator=127&years=2011&years=2010&genders=female
 * Sotkanet response - only json
 *
 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
 * For statsgrid backwards compatibility. fi.nls.oskari.control.sotka package as whole can be removed once
 * the new statsgrid frontend is ready for production
 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
 * @deprecated
 */
@OskariActionRoute("GetSotkaData")
public class GetSotkaDataHandler extends ActionHandler {

    private static final Logger log = LogFactory.getLogger(GetSotkaDataHandler.class);

    private static final String PARM_ACTION = "action";

    private static final String PARM_VERSION = "version";
    private static final String PARM_INDICATOR = "indicator";
    private static final String PARM_YEARS = "years"; // many
    private static final String PARM_GENDERS = "genders"; // total | male | female

    private static final String HEADER_CONTENT_TYPE = "Content-Type";
    private static final String VALUE_CONTENT_TYPE_JSON = "application/json";
    private static final String OSKARI_ENCODING = "UTF-8";

    public void handleAction(final ActionParameters params) throws ActionException {
        final SotkaRequest request = getRequest(params);
        final String data = request.getData();

        final HttpServletResponse response = params.getResponse();
        response.addHeader(HEADER_CONTENT_TYPE, VALUE_CONTENT_TYPE_JSON);
        response.setCharacterEncoding(OSKARI_ENCODING);
        ResponseHelper.writeResponse(params, data);
    }

    private SotkaRequest getRequest(final ActionParameters params) throws ActionException {

        // Sotkanet action must be in params
    	printParameters(params.getRequest());


        final SotkaRequest req = SotkaRequest.getInstance(params.getRequiredParam(PARM_ACTION));
        req.setGender(params.getHttpParam(PARM_GENDERS, ""));
        req.setVersion(params.getHttpParam(PARM_VERSION, "1.1"));
        req.setIndicator(params.getHttpParam(PARM_INDICATOR, ""));
        req.setYears(params.getRequest().getParameterValues(PARM_YEARS));
        return req;
    }

    
    private void printParameters(HttpServletRequest request){
    	Enumeration paramNames = request.getParameterNames();
    	
    	while( paramNames.hasMoreElements() ){
    		String paramName = (String)paramNames.nextElement();
    		
    		log.debug("paramName : " + paramName);
    		
    		String[] paramValues = request.getParameterValues(paramName);
    		
    		if(paramValues != null){
    			for(String paramValue : paramValues){
    				log.debug("paramValue : " + paramValue);
    			}
    		}
    	}
    	
    }	    
}
