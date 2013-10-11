package fi.nls.oskari.control.view.modifier.param;

import fi.nls.oskari.annotation.OskariViewModifier;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.view.modifier.ModifierException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import fi.nls.oskari.log.Logger;
import fi.nls.oskari.view.modifier.ModifierParams;

@OskariViewModifier("statsgrid")
public class StatsgridParamHandler extends ParamHandler {

    private static final Logger log = LogFactory.getLogger(StatsgridParamHandler.class);
    private static final String PARAM_LAYERID = "layerId";
    private static final String PARAM_CURRENTCOLUMN = "currentColumn";
    private static final String PARAM_METHODID = "methodId";
    private static final String PARAM_MANUALBREAKSINPUT = "manualBreaksInput";
    private static final String PARAM_NUMBEROFCLASSES = "numberOfClasses";
    private static final String PARAM_INDICATORS = "indicators";
    private static final String KEY_INDICATOR = "indicator";
    private static final String KEY_YEAR = "year";
    private static final String KEY_GENDER = "gender";

    
    public boolean handleParam(final ModifierParams params) throws ModifierException {
    	final String paramValues = params.getParamValue();
        if(paramValues == null) {
            return false;
        }
        log.debug("\n\n\nStatsgridParamHandler got:", paramValues, "\n\n\n\n\n");

    	final String[] parts = paramValues.split("-");
    	if (parts.length != 2) {
    		// We need both value and indicator list, if not provided we bail
    		return false;
    	}
    	// parse values and indicators
    	final String[] values = parts[0].split(" ");
        final String[] indicators = parts[1].split(",");

        // parse indicators
        final JSONArray indicatorsJson = new JSONArray();        
        for (String indicatorString : indicators) {
            final String[] indicatorProps = indicatorString.split(" ");
            final JSONObject indicator =  getIndicatorJson(indicatorProps, params.getReferer());
            if(indicator != null) {
            	indicatorsJson.put(indicator);
            }
        }

    	// parse values
    	if (values.length < 4) {
    		// We need all known values, if not provided we bail
    		// manualBreaksInput is optional, but we only use the first 5
    		return false;
    	}
    	
    	// parse required values
        final String layerId = values[0];
        final String currentColumn = values[1];
        final String methodId = values[2];
        final String numberOfClasses = values[3];
        
       
        try {
            final JSONObject statsgridState = getBundleState(params.getConfig(), BUNDLE_STATSGRID);
            statsgridState.put(PARAM_LAYERID, layerId);
            statsgridState.put(PARAM_CURRENTCOLUMN, currentColumn);
            statsgridState.put(PARAM_METHODID, methodId);
            statsgridState.put(PARAM_NUMBEROFCLASSES, numberOfClasses);
            if (values.length > 4) {
            	// parse optional parameters 
                final String manualBreaksInput = values[4];
            	statsgridState.put(PARAM_MANUALBREAKSINPUT, manualBreaksInput);
            }
            statsgridState.put(PARAM_INDICATORS, indicatorsJson);
        } catch (JSONException je) {
            throw new ModifierException("Could not replace statsgrid state!");
        }
        return false;
    }
    
    public static JSONObject getIndicatorJson(final String[] indicatorParam, final String referer) throws ModifierException {

		// Indicators consists of 3 parts, the indicator (id), year, and gender.
    	// All parts are needed to reliably identify indicators.
        if (indicatorParam.length != 2) {
            final String indicator = indicatorParam[0];
            final String year = indicatorParam[1];
            final String gender = indicatorParam[2];

            final JSONObject indicatorJson = new JSONObject();
            try {
            	indicatorJson.put(KEY_INDICATOR, indicator);
            	indicatorJson.put(KEY_YEAR, year);
            	indicatorJson.put(KEY_GENDER, gender);
                return indicatorJson;
            } catch (JSONException je) {
                log.warn("Could not create layer JSON from params:", indicatorParam);
                throw new ModifierException("Could not populate layer JSON");
            }
    	} else {
        	return null;
        }
    }
    
}
