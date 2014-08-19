package fi.nls.oskari.control.statistics;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.layer.TestStatisticalIndicators;
import fi.nls.oskari.control.sotka.requests.SotkaRequest;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.ResponseHelper;

import org.json.JSONArray;

import static fi.nls.oskari.control.statistics.util.Constants.*;
/**
 * Returns indicators for statistical datasource registered to Oskari.
 */
@OskariActionRoute("StatisticalIndicators")
public class StatisticalIndicatorsHandler extends ActionHandler {
    private final static Logger log = LogFactory.getLogger(StatisticalIndicatorsHandler.class);

	private static final String PARAM_DATASOURCE = "datasource";
	private static final String INDICATORS = "indicators";
	// add data sources here:
	private static final int SOTKA_DATASOURCE = 1;
	private static final int TEST_DATASOURCE = 2;
	private static final int X_DATASOURCE = 3;

    public void handleAction(ActionParameters params) throws ActionException {

        log.debug("in StatisticalIndicators");
        final int datasource = params.getRequiredParamInt(PARAM_DATASOURCE);

        JSONArray responseData = null;
        // TODO: Think again!!!
        if(datasource == SOTKA_DATASOURCE){
        	responseData = getSotkaIndicators();
        }else if(datasource == TEST_DATASOURCE){
        	responseData = getDummyIndicators();
        }else if(datasource == X_DATASOURCE){
        	
        }
        
        // TODO: load indicators based on datasource, add abstraction and transforming
        // for result so client always gets the response in same format
        // indicator regions should match statslayers
        // (see StatisticalIndicatorRegionCategories/StatisticalIndicatorRegions action route)
        ResponseHelper.writeResponse(params, responseData);
    }

    private JSONArray getDummyIndicators() throws ActionException {

        final SotkaRequest req = SotkaRequest.getInstance(INDICATORS);
        req.setGender("");
        req.setVersion("1.1");
        req.setIndicator("");
        //req.setYears(params.getRequest().getParameterValues(PARM_YEARS));
        final String data = req.getData();
        return JSONHelper.createJSONArray(data);
    }

    private JSONArray getSotkaIndicators() throws ActionException {

        final SotkaRequest req = SotkaRequest.getInstance(INDICATORS);
        req.setGender("");
        req.setVersion("1.1");
        req.setIndicator("");
        //req.setYears(params.getRequest().getParameterValues(PARM_YEARS));
        final String data = req.getData();
        return JSONHelper.createJSONArray(data);
    }
    
}
