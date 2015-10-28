package fi.nls.oskari.control.view.modifier.param;

import fi.nls.oskari.annotation.OskariViewModifier;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.view.modifier.ModifierException;
import fi.nls.oskari.view.modifier.ModifierParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@OskariViewModifier("statsgrid")
public class StatsgridParamHandler extends ParamHandler {

    private static final Logger log = LogFactory.getLogger(StatsgridParamHandler.class);
    private static final String PARAM_LAYERID = "layerId";
    private static final String PARAM_CURRENTCOLUMN = "currentColumn";
    private static final String PARAM_METHODID = "methodId";
    private static final String PARAM_MANUALBREAKSINPUT = "manualBreaksInput";
    private static final String PARAM_NUMBEROFCLASSES = "numberOfClasses";
    private static final String PARAM_CLASSIFICATIONMODE = "classificationMode";
    private static final String PARAM_ISACTIVE = "isActive";

    private static final String PARAM_INDICATORS = "indicators";
    private static final String KEY_INDICATOR = "indicator";
    private static final String KEY_YEAR = "year";
    private static final String KEY_GENDER = "gender";

    private static final String PARAM_COLORS = "colors";
    private static final String KEY_COLORSET = "set";
    private static final String KEY_COLORINDEX = "index";
    private static final String KEY_COLORFLIPPED = "flipped";


    public boolean handleParam(final ModifierParams params) throws ModifierException {
    	final String paramValues = params.getParamValue();
        if(paramValues == null) {
            return false;
        }
        log.debug("StatsgridParamHandler got:", paramValues, "\n");

    	final String[] parts = paramValues.split("-");

    	if (parts.length < 2) {
    		// We need both value and indicator list, if not provided we bail
            // optional colors part is the third one
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
        
       log.info("parts length: " + parts.length);
        try {
            final JSONObject statsgridState = getBundleState(params.getConfig(), BUNDLE_STATSGRID);
            statsgridState.put(PARAM_LAYERID, layerId);
            statsgridState.put(PARAM_CURRENTCOLUMN, currentColumn);
            statsgridState.put(PARAM_METHODID, methodId);
            statsgridState.put(PARAM_NUMBEROFCLASSES, numberOfClasses);
            if (values.length == 5) {
                final String classificationMode = values[4];
                statsgridState.put(PARAM_CLASSIFICATIONMODE, classificationMode);
            }
            if (values.length == 6) {
            	// parse optional parameters 
                final String manualBreaksInput = values[5];
            	statsgridState.put(PARAM_MANUALBREAKSINPUT, manualBreaksInput);
            }
            if (parts.length >= 3 && parts[2].length() > 0) {
                final String[] colors = parts[2].split(",");
                JSONObject colorsJson = getColorsJson(colors);
                if (colorsJson != null) {
                    statsgridState.put(PARAM_COLORS, colorsJson);
                }
            }
            if (parts.length >= 4) {
                final boolean isActive = "1".equals(parts[3]);
                statsgridState.put(PARAM_ISACTIVE, isActive);
            }
            statsgridState.put(PARAM_INDICATORS, indicatorsJson);
        } catch (JSONException je) {
            throw new ModifierException("Could not replace statsgrid state!");
        }
        return false;
    }

    public static JSONObject getColorsJson(final String[] colors) {
        if (colors.length == 3) {
            final JSONObject colorsJson = new JSONObject();
            final String colorSet = colors[0];
            final int colorIndex = ConversionHelper.getInt(colors[1], 0);
            final Boolean colorsFlipped = ConversionHelper.getBoolean(colors[2], false);

            try {
                colorsJson.put(KEY_COLORSET, colorSet);
                colorsJson.put(KEY_COLORINDEX, colorIndex);
                colorsJson.put(KEY_COLORFLIPPED, colorsFlipped);

                return colorsJson;
            } catch (JSONException je) {
                log.warn("Could not create colors JSON from params:", colors);
                return null;
            }
        } else {
            return null;
        }
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
